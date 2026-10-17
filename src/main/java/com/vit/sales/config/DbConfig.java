package com.vit.sales.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Loads local database settings without embedding credentials in source code. */
public record DbConfig(String url, String username, String password) {
    public static DbConfig load() throws IOException {
        Path path = Path.of("config", "db.properties");
        if (!Files.isRegularFile(path)) {
            throw new IOException("Missing config/db.properties. Copy config/db.properties.example and set your local values.");
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        }
        String url = required(properties, "db.url");
        String username = required(properties, "db.username");
        String password = required(properties, "db.password");
        if (password.startsWith("REPLACE_WITH_")) {
            throw new IOException("Set db.password in config/db.properties before launching the application.");
        }
        return new DbConfig(url, username, password);
    }

    private static String required(Properties properties, String key) throws IOException {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) throw new IOException("Missing property: " + key);
        return value.trim();
    }
}
