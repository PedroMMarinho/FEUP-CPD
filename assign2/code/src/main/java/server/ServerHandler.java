package server;

import enums.Command;
import enums.ServerResponse;
import models.Room;
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
    private User currentUser = null;
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

        String[] parts = input.split("\\s+");
        Command command = Command.fromString(parts[0]);

        switch (command) {
            case REGISTER:
                if ( currentUser != null) {
                    out.println(ServerResponse.UNKNOWN_COMMAND);
                    break;
                }
                if (parts.length == 3) {
                    String regUsername = parts[1];
                    String regPassword = parts[2];
                    if (authenticationManager.registerUser(regUsername, regPassword)) {
                        out.println(ServerResponse.REGISTER_SUCCESS);
                        currentUser = new User(regUsername);
                        server.userLoggedIn(currentUser.getUsername());
                    } else {
                        out.println(ServerResponse.REGISTER_FAILED);
                    }
                } else {
                    out.println(ServerResponse.REGISTER_FAILED);
                }
                break;
            case LOGIN:
                if (currentUser != null) {
                    out.println(ServerResponse.UNKNOWN_COMMAND);
                    break;
                }
                if (parts.length == 3) {
                    String loginUsername = parts[1];
                    String loginPassword = parts[2];

                    if (server.isUserLoggedIn(loginUsername)) {
                        out.println(ServerResponse.LOGIN_FAILED_ALREADY_LOGGED_IN);
                        break;
                    }

                    User user = authenticationManager.authenticate(loginUsername, loginPassword);

                    if (user != null) {
                        currentUser = user;
                        server.userLoggedIn(currentUser.getUsername());
                        out.println(ServerResponse.LOGIN_SUCCESS);
                    } else {
                        out.println(ServerResponse.LOGIN_FAILED);
                    }
                } else {
                    out.println(ServerResponse.LOGIN_FAILED);
                }
                break;
            case JOIN:
                if (currentUser != null) {
                    String roomToJoin = parts[1];

                    if (server.createRoom(roomToJoin, currentUser.getUsername())) {
                        out.println(ServerResponse.CREATED_ROOM);
                        break;
                    }

                    Room room = server.getRoomManager().getRoomByName(roomToJoin);
                    if (room != null) {
                        room.addMember(currentUser.getUsername());
                        out.println(ServerResponse.JOINED_ROOM);
                        out.println(room.getOwner());
                    } else {
                        out.println(ServerResponse.JOIN_FAILED);
                    }

                } else {
                    out.println(ServerResponse.UNKNOWN_COMMAND);
                }
                break;
            case LOGOUT:
                if (currentUser != null) {
                    System.out.println("Client " + currentUser.getUsername() + " logged out.");
                    server.userLoggedOut(currentUser.getUsername());
                    currentUser = null;
                    out.println(ServerResponse.LOGOUT_SUCCESS);
                }else{
                    out.println(ServerResponse.UNKNOWN_COMMAND);
                }
                break;
            case REFRESH:
                if (currentUser != null) {
                    out.println(ServerResponse.LIST_ROOMS_RESPONSE);
                    String availableRooms = server.getAvailableRoomsString();
                    out.println(availableRooms);
                }
                break;
            default:
                out.println(ServerResponse.UNKNOWN_COMMAND);
        }
    }

    private void cleanup() {
        try {
            if (currentUser != null) {
                server.userLoggedOut(currentUser.getUsername());
            }
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