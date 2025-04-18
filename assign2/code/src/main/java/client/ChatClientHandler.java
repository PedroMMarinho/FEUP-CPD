package client;

import enums.Command;
import enums.ServerResponse;
import models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieManager;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import enums.ClientState;


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


    private ClientState currentState = ClientState.DISCONNECTED;


    public ChatClientHandler(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            connectToServer();
            if (authenticate()) {
                currentState = ClientState.IN_LOBBY;
                handleLobby();
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

    private void connectToServer() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Connecting to the chat server at " + serverAddress + ":" + serverPort + "...");
        currentState = ClientState.AUTHENTICATING;
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

    private void handleLobby() {
        // Initial room list refresh
        refreshRoomList();

        while (running && currentState == ClientState.IN_LOBBY) {

            displayLobbyCommands();
            String userInput = scanner.nextLine().trim();

            // Extract command part (first word)
            String commandStr = userInput.split("\\s+", 2)[0].toUpperCase();
            Command command = Command.fromString(commandStr);
            switch (command) {
                case JOIN:
                    handleJoinCommand(userInput);
                    break;
                case REFRESH:
                    refreshRoomList();
                    break;
                case LOGOUT:
                    handleLogout();
                    break;
                case NEXT_PAGE, PREVIOUS_PAGE:
                    handlePagination(userInput);
                    break;
                default:
                    sendCommandAndProcessResponse(userInput);
            }

        }
    }

    private void displayLobbyCommands() {
        System.out.println("\n==== LOBBY COMMANDS ====");
        System.out.println("JOIN <room>     - Join a chat room or create if does not exist.");
        System.out.println("REFRESH         - Refresh room list");
        System.out.println("NEXT_PAGE       - View next page of rooms");
        System.out.println("PREVIOUS_PAGE   - View previous page of rooms");
        System.out.println("LOGOUT          - Log out from the server");
        System.out.print("> ");
    }

    private void refreshRoomList() {
        out.println(Command.REFRESH);
        try {
            String response = in.readLine();
            if (ServerResponse.fromString(response) == ServerResponse.LIST_ROOMS_RESPONSE) {
                handleRoomListResponse();
            }
        } catch (IOException ex) {
            System.out.println("Error refreshing room list: " + ex.getMessage());
        }
    }

    private void handleJoinCommand(String userInput) {
        out.println(userInput);
        try {
            String response = in.readLine();
            ServerResponse serverResponse = ServerResponse.fromString(response);

            if (serverResponse == ServerResponse.JOINED_ROOM || serverResponse == ServerResponse.CREATED_ROOM) {
                String[] parts = userInput.split("\\s+");
                if (parts.length > 1) {
                    String roomName = parts[1].trim();

                    String ownerInfo = "";
                    if (serverResponse == ServerResponse.JOINED_ROOM) {
                        String ownerUsername = in.readLine();
                        ownerInfo = " [owner: " + ownerUsername + "]";
                    }

                    // Display appropriate message based on response type
                    if (serverResponse == ServerResponse.JOINED_ROOM) {
                        System.out.println("Joined room: " + roomName + ownerInfo);
                    } else {
                        System.out.println("Created and joined new room: " + roomName);
                    }

                    currentRoom = roomName;

                    currentState = ClientState.IN_CHAT_ROOM;
                    handleChatRoom();
                }
            } else if (serverResponse == ServerResponse.JOIN_FAILED) {
                System.out.println("Failed to join the room.");
            }
        } catch (IOException e) {
            System.out.println("Error processing join command: " + e.getMessage());
        }
    }

    private void handleLogout() {
        out.println("LOGOUT");
        try {
            String response = in.readLine();
            ServerResponse serverResponse = ServerResponse.fromString(response);

            if (serverResponse == ServerResponse.LOGOUT_SUCCESS) {
                System.out.println("Logged out successfully.");
                running = false;
            }
        } catch (IOException e) {
            System.out.println("Error processing logout command: " + e.getMessage());
        }
    }

    private void handlePagination(String userInput) {
        sendCommandAndProcessResponse(userInput);
    }

    private void sendCommandAndProcessResponse(String command) {
        out.println(command);
        try {
            String response = in.readLine();
            ServerResponse serverResponse = ServerResponse.fromString(response);

            if (serverResponse == ServerResponse.LIST_ROOMS_RESPONSE) {
                handleRoomListResponse();
            } else if (serverResponse == ServerResponse.UNKNOWN_COMMAND) {
                System.out.println("Unknown command.");
            }
        } catch (IOException e) {
            System.out.println("Error sending command: " + e.getMessage());
        }
    }

    private void handleChatRoom() {
        System.out.println("\n==== You're now in chat room: " + currentRoom + " ====");
        System.out.println("Type messages to chat. Use command /leave or /help to see a list of commands.");

        receiver = new ClientReceiver(in, this);
        receiverThread = Thread.ofVirtual()
                .name("client-receiver")
                .start(receiver);

        while (running && currentState == ClientState.IN_CHAT_ROOM) {
            String message = scanner.nextLine().trim();

            if (message.equals("/leave")) {
                leaveRoom();
                break;
            } else if (message.equals("/help")) {
                displayChatHelp();
            } else {
                sendChatMessage(message);
            }
        }

        if (receiver != null) {
            receiver.shutdown();
            receiver = null;
        }

        if (receiverThread != null) {
            receiverThread.interrupt();
            try {
                receiverThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            receiverThread = null;
        }

        if (running) {
            currentState = ClientState.IN_LOBBY;
        }
    }
    private void displayChatHelp() {
        System.out.println("\n==== CHAT COMMANDS ====");
        System.out.println("/leave - Leave the current room and return to lobby");
        System.out.println("/help  - Display this help message");
        System.out.println("Anything else will be sent as a message to the room");
    }

    private void sendChatMessage(String message) {
        out.println(Command.MESSAGE);
        out.println(message);
    }

    private void leaveRoom() {
        if (socket == null || socket.isClosed()) {
            System.out.println("Connection already closed.");
            currentRoom = null;
            currentState = ClientState.IN_LOBBY;

            // Clean up receiver thread
            cleanupReceiver();
            return;
        }

        try {
            // 1. First tell the receiver to stop processing new messages
            if (receiver != null) {
                receiver.shutdown();
            }

            // 2. Send the leave command
            out.println(Command.LEAVE_ROOM);

            // 3. Read the response - with a timeout to prevent blocking forever
            String response = null;
            if (in.ready() || socket.getInputStream().available() > 0) {
                response = in.readLine();
            } else {
                // Give a little time for the response to arrive
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < 2000) { // 2 second timeout
                    if (in.ready() || socket.getInputStream().available() > 0) {
                        response = in.readLine();
                        break;
                    }
                    Thread.sleep(100);
                }
            }

            // 4. Process the response if we got one
            if (response != null) {
                ServerResponse serverResponse = ServerResponse.fromString(response);
                if (serverResponse == ServerResponse.LEFT_ROOM) {
                    System.out.println("Left room: " + currentRoom);
                } else {
                    System.out.println("Unexpected response when leaving room: " + serverResponse);
                }
            } else {
                System.out.println("No response received when leaving room.");
            }
        } catch (IOException e) {
            System.out.println("Error leaving room: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // 5. Always clean up, regardless of what happened
            currentRoom = null;
            currentState = ClientState.IN_LOBBY;

            // Clean up receiver thread
            cleanupReceiver();

            // 6. Refresh the room list if still connected
            if (running && socket != null && !socket.isClosed()) {
                try {
                    refreshRoomList();
                } catch (Exception e) {
                    System.out.println("Could not refresh room list: " + e.getMessage());
                }
            }
        }
    }

    // Helper method to clean up the receiver thread
    private void cleanupReceiver() {
        if (receiverThread != null) {
            receiverThread.interrupt();
            try {
                receiverThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            receiver = null;
            receiverThread = null;
        }
    }


    private void handleRoomListResponse() {
        try {
            String response = in.readLine();
            System.out.println("\n===== Available Rooms =====");
            if (response != null && !response.trim().isEmpty()) {
                Arrays.stream(response.split(","))
                        .map(String::trim)
                        .sorted()
                        .forEach(room -> System.out.println("- " + room));
            } else {
                System.out.println("No rooms available.");
            }
            System.out.println("=========================");
        } catch (IOException e) {
            System.err.println("Error reading room list response: " + e.getMessage());
            running = false;
        }
    }


    public void notifyDisconnect() {
        if (running) {
            running = false;
            System.out.println("\nLost connection to server.");
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
            try {
                receiverThread.join(2000);
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
            // Ignore
        }

        if (scanner != null) {
            scanner.close();
        }

        System.out.println("Chat client exited successfully.");
    }

    public boolean isRunning() {
        return running;
    }

}