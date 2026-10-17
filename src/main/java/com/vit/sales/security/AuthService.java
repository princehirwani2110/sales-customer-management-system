package com.vit.sales.security;

import java.sql.SQLException;
import java.time.Instant;

/** Authentication service with generic failure messages and temporary lockout. */
public final class AuthService {
    public static final String GENERIC_FAILURE = "Login failed. Check your credentials or contact an administrator.";
    private final UserDao users;
    public AuthService(UserDao users) { this.users = users; }

    public record AuthenticatedUser(int id, String username, String role) {}

    public AuthenticatedUser login(String username, char[] password) throws SQLException {
        try {
            var found = users.findByUsername(username == null ? "" : username.trim());
            if (found.isEmpty()) {
                // Keep the same user-facing error for unknown usernames and incorrect passwords.
                throw new AuthenticationException(GENERIC_FAILURE);
            }
            UserDao.User user = found.get();
            if (!user.active()) throw new AuthenticationException(GENERIC_FAILURE);
            if (user.lockedUntil() != null && user.lockedUntil().toInstant().isAfter(Instant.now())) {
                throw new AuthenticationException(GENERIC_FAILURE);
            }
            if (!PasswordHasher.verify(password, user.hash(), user.salt(), user.iterations())) {
                users.recordFailure(user.id());
                throw new AuthenticationException(GENERIC_FAILURE);
            }
            users.recordSuccess(user.id());
            return new AuthenticatedUser(user.id(), user.username(), user.role());
        } finally {
            if (password != null) java.util.Arrays.fill(password, '\0');
        }
    }

    public static final class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) { super(message); }
    }
}
