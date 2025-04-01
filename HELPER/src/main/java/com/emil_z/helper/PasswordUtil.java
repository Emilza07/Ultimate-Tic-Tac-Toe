package com.emil_z.helper;

import android.os.Build;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

//import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    private static final int SALT_LENGTH = 16; // 128 bits

    public static String hashPassword(String password){
//        Log.d("qqq", "Enter hashPassword");
//        String h = BCrypt.hashpw(password, BCrypt.gensalt());
//        Log.d("qqq", "Hash = " + h);
//        return BCrypt.hashpw(password, BCrypt.gensalt());

        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Create SHA-256 hasher
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Add salt to the password and hash
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // Combine salt and hashed password
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            // Encode as Base64 for storage
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String h = Base64.getEncoder().encodeToString(combined);
                return Base64.getEncoder().encodeToString(combined);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }

        return "";
    }

//    public static boolean checkPassword(String password, String hashedPassword){
//        //return BCrypt.checkpw(password, hashedPassword);
//    }

    public static boolean verifyPassword(String password, String storedPasswordHash) {
        try {
            // Extract the salt and hashed password from the stored hash
            byte[] combinedBytes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                combinedBytes = Base64.getDecoder().decode(storedPasswordHash);
            }
            byte[] salt = new byte[SALT_LENGTH];
            byte[] hashedPassword = new byte[combinedBytes.length - SALT_LENGTH];
            System.arraycopy(combinedBytes, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combinedBytes, SALT_LENGTH, hashedPassword, 0, hashedPassword.length);

            // Rehash the entered password with the extracted salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] newHashedPassword = md.digest(password.getBytes());

            // Compare the new hashed password with the stored hashed password
            return MessageDigest.isEqual(hashedPassword, newHashedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Error verifying password", e);
        }
    }
}