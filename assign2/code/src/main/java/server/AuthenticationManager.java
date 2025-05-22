package server;

import interfaces.CustomConcurrentMap;
import models.SimpleThreadSafeMapCustom;
import models.UserSession;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class AuthenticationManager {
    private final String usersFilePath;
    private final CustomConcurrentMap<String, String> userCredentials;
    private final CustomConcurrentMap<String, UserSession> userSessions;

    public AuthenticationManager(String usersFilePath) {
        this.usersFilePath = usersFilePath;
        this.userCredentials = new SimpleThreadSafeMapCustom<>();
        this.userSessions = new SimpleThreadSafeMapCustom<>();
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

    public UserSession registerUser(String username, String password) {
        if (userCredentials.containsKey(username)) {
            return null;
        }
        String hashedPassword = hashPassword(password);
        if (hashedPassword != null) {
            userCredentials.put(username, hashedPassword);
            saveUser(username, hashedPassword);
            return createSession(username);
        }
        return null;
    }

    public UserSession authenticate(String username, String password) {
        String storedHash = userCredentials.get(username);
        if (storedHash != null && verifyPassword(password, storedHash)) {
            return createSession(username);
        }
        return null;
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

    private UserSession createSession(String username){
        String token = UUID.randomUUID().toString();
        UserSession session = new UserSession(token, username);
        userSessions.put(token, session);
        return session;
    }

    public UserSession getUserSessionByToken(String token){
        if(userSessions.containsKey(token))
            return userSessions.get(token);
        return null;
    }

    public void updateUserSession(String token, UserSession newSession){
        userSessions.remove(token);
        userSessions.put(newSession.getToken(), newSession);
    }
}