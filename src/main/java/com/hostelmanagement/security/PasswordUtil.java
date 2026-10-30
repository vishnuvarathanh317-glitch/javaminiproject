package com.hostelmanagement.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * PBKDF2-based password hashing.
 * Stores: iterations:base64(salt):base64(hash)
 * Never stores or logs plaintext passwords.
 */
public class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {}

    /** Hash a plaintext password. Returns a storable string. */
    public static String hashPassword(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        return ITERATIONS + ":"
             + Base64.getEncoder().encodeToString(salt) + ":"
             + Base64.getEncoder().encodeToString(hash);
    }

    /** Verify a plaintext password against a stored hash. */
    public static boolean verifyPassword(String password, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 3) return false;

        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

        byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations, expectedHash.length * 8);

        // Constant-time comparison to prevent timing attacks
        return slowEquals(expectedHash, actualHash);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
