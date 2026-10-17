package com.vit.sales.security;

import java.util.Arrays;

public final class PasswordHasherSmokeTest {
    public static void main(String[] args) {
        char[] password = "Stage2-Test-Password!".toCharArray();
        PasswordHasher.Hash hash = PasswordHasher.hash(password);
        if (!PasswordHasher.verify("Stage2-Test-Password!".toCharArray(), hash.encodedHash(), hash.encodedSalt(), hash.iterations()))
            throw new AssertionError("Correct password did not verify");
        if (PasswordHasher.verify("Wrong-Password!".toCharArray(), hash.encodedHash(), hash.encodedSalt(), hash.iterations()))
            throw new AssertionError("Wrong password was accepted");
        if (hash.encodedSalt().isBlank() || hash.encodedHash().isBlank() || hash.iterations() < 100_000)
            throw new AssertionError("Hash metadata missing");
        Arrays.fill(password, '\0');
        System.out.println("PASS: password hash verifies correct password and rejects incorrect password.");
    }
}
