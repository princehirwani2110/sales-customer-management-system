package com.vit.sales.security;

import com.vit.sales.db.DatabaseManager;
import java.io.Console;
import java.util.Arrays;

/** First-run console utility; create the initial administrator without embedding a password. */
public final class SetupAdmin {
    public static void main(String[] args) throws Exception {
        Console console = System.console();
        if (console == null) throw new IllegalStateException("Run this utility in a real terminal so passwords are not echoed.");
        UserDao dao = new UserDao(DatabaseManager.fromLocalConfig());
        if (dao.countUsers() > 0) {
            System.out.println("Users already exist. Initial administrator setup is disabled.");
            return;
        }
        String username = console.readLine("Choose admin username: ").trim();
        if (!username.matches("[A-Za-z0-9._-]{3,50}")) throw new IllegalArgumentException("Use 3-50 letters, digits, dots, underscores or hyphens.");
        char[] first = console.readPassword("Choose password (minimum 10 characters): ");
        char[] second = console.readPassword("Confirm password: ");
        try {
            if (!Arrays.equals(first, second)) throw new IllegalArgumentException("Passwords do not match.");
            dao.createUser(username, first, "ADMIN");
            System.out.println("Initial administrator created. Keep the password private.");
        } finally { Arrays.fill(first, '\0'); Arrays.fill(second, '\0'); }
    }
}
