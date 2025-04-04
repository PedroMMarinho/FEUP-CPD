// client/ChatClientHandler.java
package client;

import enums.ServerResponse;
import models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientHandler implements Runnable {
    private final String serverAddress;
    private final int serverPort;
    private User currentUser;
    private volatile boolean running = true;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final Scanner scanner = new Scanner(System.in);
    private String currentRoom = null;
    private Thread receiverThread;

    public ChatClientHandler(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connecting to the chat server at " + serverAddress + ":" + serverPort + "...");

            if (authenticate()) {
                startChatting();
            }

        } catch (IOException ex) {
            System.out.println("I/O error occurred: " + ex.getMessage());
        } finally {
            cleanup();
        }
    }

    private boolean authenticate() throws IOException {
        while (currentUser == null && running) {
            System.out.print("Enter 'LOGIN <username> <password>' or 'REGISTER <username> <password>': ");
            String authInput = scanner.nextLine();
            out.println(authInput);
            String response = in.readLine();

            if (response != null) {
                String[] responseParts = response.split("\\s+", 2);
                String firstWord = responseParts[0];

                switch (firstWord) {
                    case "LOGIN_SUCCESS":
                        String[] loginParts = authInput.split("\\s+");
                        if (loginParts.length >= 2) {
                            String username = loginParts[1];
                            currentUser = new User(username);
                            System.out.println("Authentication successful. Welcome, " + currentUser.getUsername() + "!");
                            return true;
                        }
                        break;
                    case "REGISTER_SUCCESS":
                        System.out.println("Registration successful. You can now log in.");
                        break;
                    case "LOGIN_FAILED", "REGISTER_FAILED":
                        System.out.println(response);
                        break;
                    default:
                        System.out.println("Unknown authentication response from server" );
                        break;
                }
            } else {
                System.out.println("Connection to server lost during authentication.");
                running = false;
                break;
            }
        }
        return false;
    }

    private void startChatting() throws IOException {
        ClientReceiver receiver = new ClientReceiver(in);
        receiverThread = new Thread(receiver);
        receiverThread.start();

        System.out.println("Enter commands (LIST_ROOMS, JOIN_ROOM <room>, CREATE_ROOM <room>, CREATE_AI_ROOM <room> <prompt>, LOGOUT):");
        String userInput;

        while (running && (userInput = scanner.nextLine()) != null) {
            String[] parts = userInput.split("\\s+", 2);
            String command = parts[0].toUpperCase();
            String data = parts.length > 1 ? parts[1] : "";

            switch (command) {
                case "LIST_ROOMS":
                case "LOGOUT":
                    out.println(userInput);
                    if (command.equals("LOGOUT")) {
                        running = false;
                    }
                    break;
                case "JOIN_ROOM":
                    if (!data.isEmpty()) {
                        out.println(userInput);
                        String joinResponse = in.readLine();
                        System.out.println("Server response: " + joinResponse);
                        if (joinResponse != null && joinResponse.startsWith(ServerResponse.JOINED_ROOM.getResponse())) {
                            currentRoom = data;
                        } else {
                            currentRoom = null;
                        }
                    } else {
                        System.out.println("Usage: JOIN_ROOM <room_name>");
                    }
                    break;
                case "CREATE_ROOM":
                    if (!data.isEmpty()) {
                        out.println(userInput);
                        String createResponse = in.readLine();
                        System.out.println("Server response: " + createResponse);
                    } else {
                        System.out.println("Usage: CREATE_ROOM <room_name>");
                    }
                    break;
                case "CREATE_AI_ROOM":
                    String[] aiParts = data.split("\\s+", 2);
                    if (aiParts.length == 2 && !aiParts[0].isEmpty() && !aiParts[1].isEmpty()) {
                        out.println(userInput);
                        String createAIResponse = in.readLine();
                        System.out.println("Server response: " + createAIResponse);
                    } else {
                        System.out.println("Usage: CREATE_AI_ROOM <room_name> <prompt>");
                    }
                    break;
                case "SEND":
                    if (currentRoom != null) {
                        out.println(userInput);
                    } else {
                        System.out.println("You must join a room first to send a message.");
                    }
                    break;
                default:
                    System.out.println("Invalid command.");
            }

            if (!running) {
                break;
            }
            System.out.print(currentRoom != null ? "[" + currentRoom + "] > " : "> ");
        }
    }

    private void cleanup() {
        System.out.println("Exiting chat client.");
        running = false;
        if (receiverThread != null) {
            receiverThread.interrupt();
            try {
                receiverThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (scanner != null) scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}