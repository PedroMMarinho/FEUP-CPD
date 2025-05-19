package models;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AIManager {
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final String DEFAULT_MODEL = "llama3";
    private static final int MAX_HISTORY_MESSAGES = 20;
    private final String BOT_NAME = "Bot";

    private final Map<String, String> roomPrompts = new HashMap<>();
    private final Map<String, List<Map<String, String>>> roomMessageHistory = new HashMap<>();

    private final Lock roomPromptsLock = new ReentrantLock();
    private final Lock roomMessageHistoryLock = new ReentrantLock();

    public String getBOT_NAME() {
        return BOT_NAME;
    }

    public void createAIRoom(String roomName, String prompt) {
        roomPromptsLock.lock();
        try {
            roomPrompts.put(roomName, prompt);
        } finally {
            roomPromptsLock.unlock();
        }

        roomMessageHistoryLock.lock();
        try {
            roomMessageHistory.put(roomName, new ArrayList<>());

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", prompt);
            roomMessageHistory.get(roomName).add(systemMessage);
        } finally {
            roomMessageHistoryLock.unlock();
        }
    }

    public void addUserMessage(String roomName, String username, String message) {
        roomMessageHistoryLock.lock();
        try {
            if (!roomMessageHistory.containsKey(roomName)) {
                return;
            }

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", username + ": " + message);

            List<Map<String, String>> messages = roomMessageHistory.get(roomName);
            messages.add(userMessage);

            if (messages.size() > MAX_HISTORY_MESSAGES + 1) {
                List<Map<String, String>> trimmedHistory = new ArrayList<>();
                trimmedHistory.add(messages.get(0));
                for (int i = messages.size() - MAX_HISTORY_MESSAGES; i < messages.size(); i++) {
                    trimmedHistory.add(messages.get(i));
                }
                roomMessageHistory.put(roomName, trimmedHistory);
            }
        } finally {
            roomMessageHistoryLock.unlock();
        }
    }

    public void addNonUserMessage(String roomName, String username, String message) {
        roomMessageHistoryLock.lock();
        try {
            if (!roomMessageHistory.containsKey(roomName)) {
                return;
            }

            Map<String, String> chatMessage = new HashMap<>();
            chatMessage.put("role", "system");
            chatMessage.put("content", "Chat message: " + username + " said: " + message);
            roomMessageHistory.get(roomName).add(chatMessage);

            List<Map<String, String>> messages = roomMessageHistory.get(roomName);
            if (messages.size() > MAX_HISTORY_MESSAGES + 1) {
                List<Map<String, String>> trimmedHistory = new ArrayList<>();
                trimmedHistory.add(messages.get(0));
                for (int i = messages.size() - MAX_HISTORY_MESSAGES; i < messages.size(); i++) {
                    trimmedHistory.add(messages.get(i));
                }
                roomMessageHistory.put(roomName, trimmedHistory);
            }
        } finally {
            roomMessageHistoryLock.unlock();
        }
    }

    public String getAIResponse(String roomName) {
        roomMessageHistoryLock.lock();
        List<Map<String, String>> messages;
        try {
            if (!roomMessageHistory.containsKey(roomName)) {
                return "AI error: Room not found";
            }
            messages = new ArrayList<>(roomMessageHistory.get(roomName));
        } finally {
            roomMessageHistoryLock.unlock();
        }

        try {
            URL url = new URL(OLLAMA_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String prompt = buildConversationPrompt(messages);

            String jsonRequest = "{" +
                    "\"model\": \"" + DEFAULT_MODEL + "\"," +
                    "\"prompt\": \"" + escapeJson(prompt) + "\"," +
                    "\"stream\": false" +
                    "}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine);
                    }
                    System.err.println("Error response from Ollama: " + errorResponse);
                    return "AI error: Received error code " + responseCode + " from LLM service";
                }
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            String aiResponse = parseResponse(response.toString());

            String formattedResponse = BOT_NAME + ": " + aiResponse;

            roomMessageHistoryLock.lock();
            try {
                Map<String, String> assistantMessage = new HashMap<>();
                assistantMessage.put("role", "assistant");
                assistantMessage.put("content", aiResponse);
                roomMessageHistory.get(roomName).add(assistantMessage);
            } finally {
                roomMessageHistoryLock.unlock();
            }

            return formattedResponse;

        } catch (IOException e) {
            System.err.println("Error connecting to Ollama: " + e.getMessage());
            return "AI error: Could not connect to the LLM service. Make sure Ollama is running.";
        }
    }

    private String buildConversationPrompt(List<Map<String, String>> messages) {
        StringBuilder prompt = new StringBuilder();

        for (Map<String, String> message : messages) {
            String role = message.get("role");
            String content = message.get("content");

            if ("system".equals(role)) {
                if (content.startsWith("Chat message:")) {
                    prompt.append(content.substring("Chat message: ".length())).append("\n");
                } else {
                    prompt.append("Instructions: ").append(content).append("\n\n");
                }
            } else if ("user".equals(role)) {
                prompt.append(content).append("\n");
            } else if ("assistant".equals(role)) {
                prompt.append(BOT_NAME).append(": ").append(content).append("\n");
            }
        }

        prompt.append(BOT_NAME).append(": ");

        return prompt.toString();
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }

        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String parseResponse(String jsonResponse) {
        int responseIndex = jsonResponse.indexOf("\"response\":");
        if (responseIndex == -1) {
            return "AI error: Invalid response format";
        }

        int startIndex = jsonResponse.indexOf("\"", responseIndex + 11) + 1;
        if (startIndex == 0) {
            return "AI error: Could not parse response";
        }

        int endIndex = jsonResponse.indexOf("\"", startIndex);
        while (endIndex > 0 && jsonResponse.charAt(endIndex - 1) == '\\') {
            endIndex = jsonResponse.indexOf("\"", endIndex + 1);
        }

        if (endIndex == -1) {
            return "AI error: Could not parse response";
        }

        return jsonResponse.substring(startIndex, endIndex)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public boolean isAIRoom(String roomName) {
        roomPromptsLock.lock();
        try {
            return roomPrompts.containsKey(roomName);
        } finally {
            roomPromptsLock.unlock();
        }
    }

    public void removeAIRoom(String roomName) {
        roomPromptsLock.lock();
        try {
            roomPrompts.remove(roomName);
        } finally {
            roomPromptsLock.unlock();
        }

        roomMessageHistoryLock.lock();
        try {
            roomMessageHistory.remove(roomName);
        } finally {
            roomMessageHistoryLock.unlock();
        }
    }
}