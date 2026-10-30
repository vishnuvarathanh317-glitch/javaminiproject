package com.hostelmanagement.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Utility (PBKDF2 Salted Hashing & Constant-time Comparison)")
class PasswordUtilTest {

    @Test
    @DisplayName("Password hash generates 3-part format: iterations:salt:hash")
    void testHashFormat() {
        String hash = PasswordUtil.hashPassword("Secret123!");
        assertNotNull(hash);

        String[] parts = hash.split(":");
        assertEquals(3, parts.length);
        assertEquals("65536", parts[0]); // Default PBKDF2 iterations
        assertFalse(parts[1].isEmpty()); // Base64 salt
        assertFalse(parts[2].isEmpty()); // Base64 hash
    }

    @Test
    @DisplayName("Correct password verifies successfully")
    void testPasswordVerificationSuccess() {
        String plain = "MyP@ssw0rd2024";
        String hash = PasswordUtil.hashPassword(plain);

        assertTrue(PasswordUtil.verifyPassword(plain, hash));
    }

    @Test
    @DisplayName("Incorrect password fails verification")
    void testPasswordVerificationFailure() {
        String plain = "MyP@ssw0rd2024";
        String hash = PasswordUtil.hashPassword(plain);

        assertFalse(PasswordUtil.verifyPassword("WrongPassword", hash));
        assertFalse(PasswordUtil.verifyPassword("myp@ssw0rd2024", hash)); // Case sensitivity check
    }

    @Test
    @DisplayName("Salts ensure identical passwords generate completely different hashes")
    void testUniqueSalts() {
        String plain = "IdenticalPassword";
        String hash1 = PasswordUtil.hashPassword(plain);
        String hash2 = PasswordUtil.hashPassword(plain);

        assertNotEquals(hash1, hash2);
        assertTrue(PasswordUtil.verifyPassword(plain, hash1));
        assertTrue(PasswordUtil.verifyPassword(plain, hash2));
    }
}
