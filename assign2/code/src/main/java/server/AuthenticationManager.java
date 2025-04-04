package server;

import interfaces.CustomConcurrentMap;
import models.SimpleThreadSafeMapCustom;
import models.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthenticationManager {
    private String usersFilePath;
    private CustomConcurrentMap<String, String> userCredentials;

    public AuthenticationManager(String usersFilePath) {
        this.usersFilePath = usersFilePath;
        this.userCredentials = new SimpleThreadSafeMapCustom<>(); // Replace with your custom map
        loadUsers();
    }

    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    userCredentials.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading user data: " + e.getMessage());
        }
    }

    public boolean registerUser(String username, String password) {
        if (userCredentials.containsKey(username)) {
            return false; // Username already exists
        }
        String hashedPassword = hashPassword(password);
        if (hashedPassword != null) {
            userCredentials.put(username, hashedPassword);
            saveUser(username, hashedPassword);
            return true;
        }
        return false;
    }

    public User authenticate(String username, String password) {
        String storedHash = userCredentials.get(username);
        if (storedHash != null && verifyPassword(password, storedHash)) {
            return new User(username);
        }
        return null; // Authentication failed
    }

    private void saveUser(String username, String hashedPassword) {
        try (FileWriter writer = new FileWriter(usersFilePath, true);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.write(username + ":" + hashedPassword);
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error: SHA-256 algorithm not available.");
            return null;
        }
    }

    private boolean verifyPassword(String password, String hashedPassword) {
        String hashedInput = hashPassword(password);
        return hashedInput != null && hashedInput.equals(hashedPassword);
    }
}