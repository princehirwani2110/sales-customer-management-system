package com.vit.sales.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** JDK-only password hashing using PBKDF2-HMAC-SHA256 and a unique random salt. */
public final class PasswordHasher {
    public static final int DEFAULT_ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    private PasswordHasher() {}

    public record Hash(String encodedHash, String encodedSalt, int iterations) {}

    public static Hash hash(char[] password) {
        if (password == null || password.length < 10) {
            throw new IllegalArgumentException("Password must contain at least 10 characters.");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password, salt, DEFAULT_ITERATIONS);
        return new Hash(Base64.getEncoder().encodeToString(derived),
                Base64.getEncoder().encodeToString(salt), DEFAULT_ITERATIONS);
    }

    public static boolean verify(char[] password, String encodedHash, String encodedSalt, int iterations) {
        try {
            byte[] expected = Base64.getDecoder().decode(encodedHash);
            byte[] salt = Base64.getDecoder().decode(encodedSalt);
            byte[] actual = derive(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        if (iterations < 100_000 || iterations > 2_000_000) {
            throw new IllegalArgumentException("Unsupported password iteration count.");
        }
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("PBKDF2WithHmacSHA256 is unavailable in this JDK.", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
