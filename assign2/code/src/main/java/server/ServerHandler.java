package server;

import enums.Command;
import enums.ServerResponse;
import models.Message;
import models.Room;
import models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ServerHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ChatServer server;
    private User currentUser = null;
    private String currentRoom = null;
    private AuthenticationManager authenticationManager;

    public ServerHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.authenticationManager = server.getAuthenticationManager();
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
            while ((clientInput = in.readLine()) != null) {
                System.out.println("Received from client [" + clientSocket.getInetAddress().getHostAddress() + "]: " + clientInput);
                processClientInput(clientInput);
                if (clientSocket.isClosed()) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress() + (currentUser != null ? " (" + currentUser.getUsername() + ")" : ""));
        } finally {
            cleanup();
        }
    }

    private void processClientInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            out.println(ServerResponse.INVALID_REQUEST);
            return;
        }

        String[] parts = input.split("\\s+", 2); // Split into command and rest
        Command command = Command.fromString(parts[0]);

        switch (command) {
            case REGISTER:
                handleRegister(input);
                break;
            case LOGIN:
                handleLogin(input);
                break;
            case JOIN:
                handleJoin(parts.length > 1 ? parts[1] : "");
                break;
            case LEAVE_ROOM:
                handleLeaveRoom();
                break;
            case MESSAGE:
                handleMessage();
                break;
            case LOGOUT:
                handleLogout();
                break;
            case REFRESH:
                handleRefresh();
                break;
            default:
                out.println(ServerResponse.UNKNOWN_COMMAND);
        }
    }

    private void handleRegister(String input) {
        if (currentUser != null) {
            out.println(ServerResponse.UNKNOWN_COMMAND);
            return;
        }

        String[] parts = input.split("\\s+");
        if (parts.length == 3) {
            String regUsername = parts[1];
            String regPassword = parts[2];
            if (authenticationManager.registerUser(regUsername, regPassword)) {
                out.println(ServerResponse.REGISTER_SUCCESS);
                currentUser = new User(regUsername);
                server.userLoggedIn(currentUser.getUsername());

                // Register with message broadcaster
                server.getMessageBroadcaster().registerClient(currentUser.getUsername(), out);
            } else {
                out.println(ServerResponse.REGISTER_FAILED);
            }
        } else {
            out.println(ServerResponse.REGISTER_FAILED);
        }
    }

    private void handleLogin(String input) {
        if (currentUser != null) {
            out.println(ServerResponse.UNKNOWN_COMMAND);
            return;
        }

        String[] parts = input.split("\\s+");
        if (parts.length == 3) {
            String loginUsername = parts[1];
            String loginPassword = parts[2];

            if (server.isUserLoggedIn(loginUsername)) {
                out.println(ServerResponse.LOGIN_FAILED_ALREADY_LOGGED_IN);
                return;
            }

            User user = authenticationManager.authenticate(loginUsername, loginPassword);

            if (user != null) {
                currentUser = user;
                server.userLoggedIn(currentUser.getUsername());

                // Register with message broadcaster
                server.getMessageBroadcaster().registerClient(currentUser.getUsername(), out);

                out.println(ServerResponse.LOGIN_SUCCESS);
            } else {
                out.println(ServerResponse.LOGIN_FAILED);
            }
        } else {
            out.println(ServerResponse.LOGIN_FAILED);
        }
    }

    private void handleJoin(String roomName) {
        if (currentUser == null || roomName.isEmpty()) {
            out.println(ServerResponse.UNKNOWN_COMMAND);
            return;
        }

        if (currentRoom != null) {
            Room oldRoom = server.getRoomManager().getRoomByName(currentRoom);
            if (oldRoom != null) {
                oldRoom.removeMember(currentUser.getUsername());

                Message leaveMessage = new Message(
                        currentUser.getUsername(),
                        "",
                        Message.MessageType.LEAVE
                );
                server.getMessageBroadcaster().broadcastMessage(oldRoom, leaveMessage);
            }
        }

        if (server.createRoom(roomName, currentUser.getUsername())) {
            currentRoom = roomName;
            out.println(ServerResponse.CREATED_ROOM);
            return;
        }

        Room room = server.getRoomManager().getRoomByName(roomName);
        if (room != null) {
            currentRoom = roomName;
            room.addMember(currentUser.getUsername());
            out.println(ServerResponse.JOINED_ROOM);
            out.println(room.getOwner());

            // Send message to other users that this user joined
            Message joinMessage = new Message(
                    currentUser.getUsername(),
                    "",
                    Message.MessageType.JOIN
            );
            server.getMessageBroadcaster().broadcastMessage(room, joinMessage);
        } else {
            out.println(ServerResponse.JOIN_FAILED);
        }
    }

    private void handleLeaveRoom() {
        if (currentUser == null || currentRoom == null) {
            out.println(ServerResponse.UNKNOWN_COMMAND);
            return;
        }

        Room room = server.getRoomManager().getRoomByName(currentRoom);
        if (room != null) {
            // Send message to other users that this user left
            Message leaveMessage = new Message(
                    currentUser.getUsername(),
                    "",
                    Message.MessageType.LEAVE
            );
            server.getMessageBroadcaster().broadcastMessage(room, leaveMessage);

            room.removeMember(currentUser.getUsername());
            currentRoom = null;
            out.println(ServerResponse.LEFT_ROOM);
        } else {
            out.println(ServerResponse.UNKNOWN_COMMAND);
        }
    }

    private void handleMessage() {
        if (currentUser == null || currentRoom == null) {
            out.println(ServerResponse.UNKNOWN_COMMAND);
            return;
        }

        try {
            String messageContent = in.readLine();
            if (messageContent != null && !messageContent.isEmpty()) {
                Room room = server.getRoomManager().getRoomByName(currentRoom);
                if (room != null) {
                    Message chatMessage = new Message(
                            currentUser.getUsername(),
                            messageContent,
                            Message.MessageType.CHAT
                    );

                    room.addMessage(chatMessage);
                    server.getMessageBroadcaster().broadcastMessage(room, chatMessage);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading message: " + e.getMessage());
        }
    }

    private void handleLogout() {
        if (currentUser != null) {
            // Leave any current room
            if (currentRoom != null) {
                Room room = server.getRoomManager().getRoomByName(currentRoom);
                if (room != null) {
                    room.removeMember(currentUser.getUsername());
                }
                currentRoom = null;
            }

            // Unregister from message broadcaster
            server.getMessageBroadcaster().unregisterClient(currentUser.getUsername());

            System.out.println("Client " + currentUser.getUsername() + " logged out.");
            server.userLoggedOut(currentUser.getUsername());
            currentUser = null;
            out.println(ServerResponse.LOGOUT_SUCCESS);
        } else {
            out.println(ServerResponse.UNKNOWN_COMMAND);
        }
    }

    private void handleRefresh() {
        if (currentUser != null) {
            out.println(ServerResponse.LIST_ROOMS_RESPONSE);
            String availableRooms = server.getAvailableRoomsString();
            out.println(availableRooms);
        } else {
            out.println(ServerResponse.UNKNOWN_COMMAND);
        }
    }

    private void cleanup() {
        try {
            if (currentUser != null) {
                // Leave current room if in one
                if (currentRoom != null) {
                    Room room = server.getRoomManager().getRoomByName(currentRoom);
                    if (room != null) {
                        // Send message to other users that this user left
                        Message leaveMessage = new Message(
                                currentUser.getUsername(),
                                "",
                                Message.MessageType.LEAVE
                        );
                        server.getMessageBroadcaster().broadcastMessage(room, leaveMessage);

                        room.removeMember(currentUser.getUsername());
                    }
                }

                // Unregister from message broadcaster
                server.getMessageBroadcaster().unregisterClient(currentUser.getUsername());

                server.userLoggedOut(currentUser.getUsername());
            }

            // Close resources
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