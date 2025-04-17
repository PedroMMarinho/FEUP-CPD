package client;

import enums.Command;
import enums.ServerResponse;
import models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import static enums.ServerResponse.LOGIN_SUCCESS;

public class ChatClientHandler implements Runnable {
    private final String serverAddress;
    private final int serverPort;
    private User currentUser;
    protected volatile boolean running = true;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner = new Scanner(System.in);
    protected volatile String currentRoom = null;
    private ClientReceiver receiver;
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
            if (running) {
                System.out.println("I/O error occurred: " + ex.getMessage());
            } else {
                System.out.println("Connection closed as requested.");
            }
        } finally {
            cleanup();
        }
    }

    private boolean authenticate() throws IOException {

        while (currentUser == null && running) {
            System.out.print("Enter 'LOGIN <username> <password>' or 'REGISTER <username> <password>': ");
            String authInput = scanner.nextLine().trim();

            out.println(authInput);
            String response = in.readLine();
            ServerResponse serverResponse = ServerResponse.fromString(response);


            if (serverResponse != null) {
                switch (serverResponse) {

                    case LOGIN_SUCCESS:
                        String[] loginParts = authInput.split("\\s+");
                        if (loginParts.length >= 2) {
                            String username = loginParts[1];
                            currentUser = new User(username);
                            System.out.println("Authentication successful. Welcome, " + currentUser.getUsername() + "!");
                            return true;
                        } else {
                            System.out.println("Error parsing username from login input.");
                        }
                        break;

                    case REGISTER_SUCCESS:
                        String[] registerParts = authInput.split("\\s+");
                        if (registerParts.length >= 2) {
                            String username = registerParts[1];
                            currentUser = new User(username);
                            System.out.println("Registration successful. Welcome, " + currentUser.getUsername() + "!");
                            return true;
                        }
                        break;

                    case LOGIN_FAILED:
                        System.out.println("Invalid username or password.");
                        break;

                    case REGISTER_FAILED:
                        System.out.println("Invalid Register entry.");
                        break;
                    case LOGIN_FAILED_ALREADY_LOGGED_IN:
                        System.out.println("User is already logged in.");
                        break;

                    case UNKNOWN_COMMAND:
                        System.out.println("Invalid command.");
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


    private void startChatting() {

        out.println(Command.REFRESH);
        try {
            String _ = in.readLine();
            handleRoomListResponse();
        }
        catch (IOException ex) {
            System.out.println("I/O error occurred: " + ex.getMessage());
        }

        String userInput;

        try {
            while (running ) {
                System.out.println("Enter commands (JOIN <room>, LOGOUT, NEXT_PAGE, PREVIOUS_PAGE, REFRESH):");
                System.out.print("> ");

                userInput = scanner.nextLine().trim();
                out.println(userInput);
                String response = in.readLine();
                ServerResponse serverResponse = ServerResponse.fromString(response);

                if (serverResponse != null) {
                    switch (serverResponse) {
                        case JOINED_ROOM:
                            String[] parts = userInput.split("\\s+", 2);
                            if (parts.length > 1) {
                                String joinedRoomName = parts[1].trim();
                                String ownerUsername = in.readLine();
                                System.out.println("Joined room: " + joinedRoomName + " [owner: " + ownerUsername + "]");
                                handleJoinedRoomResponse(joinedRoomName);
                            } else {
                                System.out.println("Error: Room name not specified after JOIN command.");
                                System.out.print("> ");
                            }
                            break;
                        case JOIN_FAILED:
                            System.out.println("Failed to join the room.");
                            System.out.print("> ");
                            break;
                        case CREATED_ROOM:
                            String[] input = userInput.split("\\s+", 2);
                            if (input.length > 1) {
                                String createdRoomName = input[1].trim();
                                handleRoomCreatedResponse(createdRoomName);
                            } else {
                                System.out.println("Error: Room name not found after CREATED_ROOM response.");
                                System.out.print("> ");
                            }
                            break;
                        case AI_ROOM_CREATED:
                            System.out.println("AI room created successfully.");
                            break;
                        case LIST_ROOMS_RESPONSE:
                            handleRoomListResponse();
                            break;
                        case LOGOUT_SUCCESS:
                            running = false;
                            System.out.println("Logged out successfully.");
                            break;
                        case UNKNOWN_COMMAND:
                            System.out.println("Unknown command.");
                            break;
                    }
                } else {
                    System.out.println("Connection to server lost.");
                    running = false;
                    break;
                }
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("Error reading user input or server response: " + e.getMessage());
            }
        } finally {
            running = false;
        }
    }

    private void handleJoinedRoomResponse(String roomName) {
        receiver = new ClientReceiver(in, this);
        receiverThread = Thread.ofVirtual()
                .name("client-receiver")
                .start(receiver);
        currentRoom = roomName;
    }

    private void handleRoomCreatedResponse(String roomName) {
        System.out.println("Created room: " + roomName + " successfully.");
        handleJoinedRoomResponse(roomName);
    }

    private void handleRoomListResponse() {
        try {
            String response = in.readLine();
            System.out.println("Available Rooms:");
            if (response != null && !response.trim().isEmpty()) {
                Arrays.stream(response.split(","))
                        .map(String::trim)
                        .sorted()
                        .forEach(room -> System.out.println("- " + room));
            } else {
                System.out.println("No rooms available.");
            }
        } catch (IOException e) {
            System.err.println("Error reading room list response: " + e.getMessage());
            running = false;
        }
    }

    private void cleanup() {
        running = false;

        if (receiver != null) {
            receiver.shutdown();
        }

        if (out != null) {
            out.close();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }

        if (receiverThread != null && receiverThread.isAlive()) {
            receiverThread.interrupt();
        }

        if (receiverThread != null) {
            try {
                receiverThread.join(2000);
                if (receiverThread.isAlive()) {
                    System.err.println("Receiver thread did not terminate gracefully.");
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for receiver thread to join.");
                Thread.currentThread().interrupt();
            }
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {

        }

        if (scanner != null) {
            scanner.close();
        }


        System.out.println("Chat client exited successfully.");
    }
}


