package com.vit.sales.security;

import com.vit.sales.db.DatabaseManager;
import java.sql.*;
import java.util.Optional;

/** DAO for authentication data. All SQL uses PreparedStatement. */
public final class UserDao {
    private final DatabaseManager database;
    public UserDao(DatabaseManager database) { this.database = database; }

    public record User(int id, String username, String hash, String salt, int iterations,
                       String role, boolean active, int failedAttempts, Timestamp lockedUntil) {}

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, password_salt, iterations, `role`, " +
                "is_active, failed_attempts, locked_until FROM Users WHERE username = ?";
        try (Connection c = database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new User(rs.getInt("user_id"), rs.getString("username"),
                        rs.getString("password_hash"), rs.getString("password_salt"), rs.getInt("iterations"),
                        rs.getString("role"), rs.getBoolean("is_active"), rs.getInt("failed_attempts"),
                        rs.getTimestamp("locked_until")));
            }
        }
    }

    public int countUsers() throws SQLException {
        try (Connection c = database.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM Users");
             ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
    }

    public void createUser(String username, char[] password, String role) throws SQLException {
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) throw new IllegalArgumentException("Role must be ADMIN or STAFF.");
        PasswordHasher.Hash hash = PasswordHasher.hash(password);
        String sql = "INSERT INTO Users (username, password_hash, password_salt, iterations, `role`) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username); ps.setString(2, hash.encodedHash()); ps.setString(3, hash.encodedSalt());
            ps.setInt(4, hash.iterations()); ps.setString(5, role); ps.executeUpdate();
        }
    }

    public void recordFailure(int userId) throws SQLException {
        String sql = "UPDATE Users SET " +
                "locked_until = CASE WHEN failed_attempts + 1 >= 5 THEN DATE_ADD(NOW(), INTERVAL 10 MINUTE) ELSE locked_until END, " +
                "failed_attempts = failed_attempts + 1 " +
                "WHERE user_id = ?";
        try (Connection c = database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.executeUpdate();
        }
    }

    public void recordSuccess(int userId) throws SQLException {
        String sql = "UPDATE Users SET failed_attempts = 0, locked_until = NULL, last_login = NOW() WHERE user_id = ?";
        try (Connection c = database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.executeUpdate();
        }
    }
}
