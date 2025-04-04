// server/ServerHandler.java
package server;

import enums.Command;
import enums.ServerResponse;
import models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ChatServer server;
    private User currentUser;
    private AuthenticationManager authenticationManager;

    public ServerHandler(Socket socket, ChatServer server, AuthenticationManager authManager) {
        this.clientSocket = socket;
        this.server = server;
        this.authenticationManager = authManager;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String clientInput;
            while ((clientInput = in.readLine()) != null) { // Keep reading as long as the connection is open
                System.out.println("Received from client [" + clientSocket.getInetAddress().getHostAddress() + "]: " + clientInput);
                processClientInput(clientInput);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
        } finally {
            cleanup();
        }
    }

    private void processClientInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            out.println(ServerResponse.INVALID_REQUEST);
            return;
        }

        String[] parts = input.split("\\s+", 3);
        Command command = Command.fromString(parts[0]);

        switch (command) {
            case REGISTER:
                if (parts.length == 3) {
                    String regUsername = parts[1];
                    String regPassword = parts[2];
                    if (authenticationManager.registerUser(regUsername, regPassword)) {
                        out.println(ServerResponse.REGISTER_SUCCESS);
                    } else {
                        out.println(ServerResponse.REGISTER_FAILED);
                    }
                } else {
                    out.println(ServerResponse.REGISTER_FAILED.getResponse() + " Invalid registration format. Use REGISTER <username> <password>");
                }
                break;
            case LOGIN:
                if (parts.length == 3) {
                    String loginUsername = parts[1];
                    String loginPassword = parts[2];
                    User user = authenticationManager.authenticate(loginUsername, loginPassword);
                    if (user != null) {
                        currentUser = user;
                        out.println(ServerResponse.LOGIN_SUCCESS);
                    } else {
                        out.println(ServerResponse.LOGIN_FAILED);
                    }
                } else {
                    out.println(ServerResponse.LOGIN_FAILED.getResponse() + " Invalid login format. Use LOGIN <username> <password>");
                }
                break;
            case LIST_ROOMS:
                if (currentUser != null) {
                    out.println(ServerResponse.LIST_ROOMS_RESPONSE.getResponse() + "[Room1], [Room2]"); // Placeholder
                } else {
                    out.println(ServerResponse.JOIN_FAILED.getResponse() + " You must be logged in to list rooms.");
                }
                break;
            case JOIN_ROOM:
                if (currentUser != null && parts.length == 2) {
                    String roomToJoin = parts[1];
                    // Implement logic to join the room
                    System.out.println(currentUser.getUsername() + " requested to join room: " + roomToJoin);
                    out.println(ServerResponse.JOINED_ROOM.getResponse() + roomToJoin); // Send confirmation
                } else {
                    out.println(ServerResponse.JOIN_FAILED.getResponse() + " You must be logged in and provide a room name to join.");
                }
                break;
            case CREATE_ROOM:
                if (currentUser != null && parts.length == 2) {
                    String newRoomName = parts[1];
                    // Implement logic to create a new room
                    System.out.println(currentUser.getUsername() + " requested to create room: " + newRoomName);
                    out.println(ServerResponse.ROOM_CREATED.getResponse() + newRoomName); // Send confirmation
                } else {
                    out.println(ServerResponse.CREATE_FAILED.getResponse() + " You must be logged in and provide a room name to create.");
                }
                break;
            case SEND:
                if (currentUser != null && parts.length == 2) {
                    String message = parts[1];
                    // Implement logic to send the message to the current room
                    System.out.println(currentUser.getUsername() + " sent message: " + message);
                    // You'll need to know which room the user is in to forward the message
                    out.println(ServerResponse.SEND_SUCCESS); // Placeholder
                } else {
                    out.println(ServerResponse.SEND_FAILED.getResponse() + " You must be logged in and (later) in a room to send a message.");
                }
                break;
            case LOGOUT:
                System.out.println("Client " + (currentUser != null ? currentUser.getUsername() : "Guest") + " logged out.");
                currentUser = null;
                out.println(ServerResponse.LOGOUT_SUCCESS);
                break;
            default:
                out.println(ServerResponse.UNKNOWN_COMMAND.getResponse());
        }
    }

    private void cleanup() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}