package client;

import enums.ClientState;
import enums.Command;
import enums.ServerResponse;
import models.Room;
import models.ThreadSafeRoomManager;
import models.User;
import server.AuthenticationManager;
import server.LoggedInUserManager;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ChatClientHandler implements Runnable {
    public static ArrayList<ChatClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private User currentUser;
    private ClientState clientState = ClientState.AUTHENTICATING;
    private static final AuthenticationManager authManager;
    private static final LoggedInUserManager loggedInManager;
    private static final ThreadSafeRoomManager roomManager;
    private String currentRoomName;

    static {
        authManager = new AuthenticationManager("src/main/java/server/data/users.txt");
        loggedInManager = new LoggedInUserManager();
        roomManager = new ThreadSafeRoomManager();
    }

    public ChatClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected() && clientState != ClientState.DISCONNECTED) {
                switch (clientState) {
                    case AUTHENTICATING:
                        handleAuthentication();
                        break;
                    case IN_LOBBY:
                        handleLobby();
                        break;
                    case IN_CHAT_ROOM:
                        handleChatRoom();
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error in client handler: " + e.getMessage());
        } finally {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void handleAuthentication() throws IOException {
            bufferedWriter.write("Welcome to the chat server! Please login or register.");
            bufferedWriter.newLine();
            bufferedWriter.write("Enter commands LOGIN <username> <password> or REGISTER <username> <password>");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            while (clientState == ClientState.AUTHENTICATING) {
                String input = bufferedReader.readLine();
                if (input == null) {
                    clientState = ClientState.DISCONNECTED;
                    return;
                }
                System.out.println(input);
                String[] parts = input.split(" ", 4);
                if (parts.length != 3) {
                    sendError("Invalid command format. Use: COMMAND username password");
                    continue;
                }

                String action = parts[0].toUpperCase();
                String username = parts[1];
                String password = parts[2];
                Command command = Command.fromString(action);
                if (command.equals(Command.LOGIN) && loggedInManager.isUserLoggedIn(username)) {
                    sendError("User already logged in. Please use a different account.");
                    continue;
                }

                if (command.equals(Command.LOGIN)) {
                    User user = authManager.authenticate(username, password);
                    if (user != null) {
                        this.currentUser = user;
                        loggedInManager.userLoggedIn(username);
                        sendSuccess("Login successful! Welcome " + username);
                        break;
                    }else {
                        sendError("Invalid username or password. Please try again.");
                    }
                } else if (command.equals(Command.REGISTER)) {
                    boolean registered = authManager.registerUser(username, password);
                    if (registered) {
                        this.currentUser = new User(username);
                        loggedInManager.userLoggedIn(username);
                        sendSuccess("Registration successful! Welcome " + username);
                        break;
                    } else {
                        sendError("Username already exists");
                    }
                } else {
                    sendError("Unknown command. Use LOGIN or REGISTER");
                }
            }
            clientState = ClientState.IN_LOBBY;

    }
    private void sendResponse(ServerResponse serverResponse, String message) throws  IOException{
        bufferedWriter.write(serverResponse.toString());
        bufferedWriter.newLine();
        bufferedWriter.write(message);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private void listRooms() throws IOException {
        bufferedWriter.write("===== Available Rooms =====");
        bufferedWriter.newLine();
        if (!roomManager.getAvailableRooms().isEmpty()) {
            Arrays.stream(roomManager.getAvailableRooms().split(","))
                    .map(String::trim)
                    .forEach(room -> {
                        try {
                            bufferedWriter.write("- " + room);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }else {
            bufferedWriter.write("No rooms available");
        }
        bufferedWriter.newLine();
        bufferedWriter.write("=========================");
        bufferedWriter.newLine();
        bufferedWriter.write("END");
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }


    private void handleLobby() throws IOException {
        bufferedWriter.write("==== LOBBY COMMANDS ====");
        bufferedWriter.newLine();
        bufferedWriter.write("JOIN <room>     - Join a chat room or create if does not exist.");
        bufferedWriter.newLine();
        bufferedWriter.write("REFRESH         - Refresh room list");
        bufferedWriter.newLine();
        bufferedWriter.write("LOGOUT          - Log out from the server");
        bufferedWriter.newLine();
        listRooms();

        while (clientState == ClientState.IN_LOBBY) {
            String input = bufferedReader.readLine();

            if (input == null) {
                clientState = ClientState.DISCONNECTED;
                return;
            }

            String[] parts = input.split(" ", 3);

            Command command = Command.fromString(parts[0]);

            switch (command) {
                case Command.JOIN:
                    if (parts.length < 2) {
                        sendError("Please specify a room name.");
                        break;
                    }
                    String roomName = parts[1];
                    this.currentRoomName = roomName;
                    if (!roomManager.roomExists(roomName)) {
                        roomManager.addRoom(new Room(roomName, currentUser.getUsername()));
                        sendSuccess("Created and joined Room: " + roomName);
                    }else{
                        roomManager.getRoomByName(roomName).addMember(currentUser.getUsername());
                        sendSuccess("Joined Room: " + roomName);
                    }
                    clientState = ClientState.IN_CHAT_ROOM;
                    break;
                case Command.LOGOUT:
                    sendResponse(ServerResponse.LOGOUT_USER, "Logged out from the server successfully.");
                    handleLogout();
                    break;
                case Command.REFRESH:
                    bufferedWriter.write(ServerResponse.LISTING_ROOMS.toString());
                    bufferedWriter.newLine();
                    listRooms();
                    break;
                default:
                    sendError("Unknown command.");
                    break;
            }
        }
    }
    private void handleLogout()  {
        loggedInManager.userLoggedOut(currentUser.getUsername());
        closeEverything(socket, bufferedReader, bufferedWriter);
    }
    private void sendChatRoomInstructions() throws IOException {
        bufferedWriter.write("Type messages to chat. Use commands /leave, /list or /help to see a list of commands.");
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private void printHelpInstructions() throws IOException {
        bufferedWriter.write("======= Available commands =======");
        bufferedWriter.newLine();
        bufferedWriter.write("/leave - Leave the chat room and return to lobby");
        bufferedWriter.newLine();
        bufferedWriter.write("/help - Show this help message");
        bufferedWriter.newLine();
        bufferedWriter.write("/list - List people in the chat room");
        bufferedWriter.newLine();
        bufferedWriter.write("===================================");
        bufferedWriter.newLine();
        bufferedWriter.write("END");
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private void handleLeave() throws IOException {
        if (currentRoomName != null && roomManager.roomExists(currentRoomName)) {
            Room room = roomManager.getRoomByName(currentRoomName);
            room.removeMember(currentUser.getUsername());
        }
        clientState = ClientState.IN_LOBBY;
        removeClientHandler();
        bufferedWriter.write("You have left the chat room and returned to the lobby.");
        bufferedWriter.newLine();
        bufferedWriter.write("END");
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }


    private void addClient()  {
        clientHandlers.add(this);
        broadCastMessage("[" + currentUser.getUsername() + " joined the chat room]");
    }

    private void listPeopleInRoom() throws IOException {
        bufferedWriter.write("======= People In Room =======");
        bufferedWriter.newLine();
        roomManager.getRoomByName(currentRoomName).getMembers().forEach(member -> {
            try {
                bufferedWriter.write("- ");
                if(!member.equals(currentUser.getUsername())) {
                    bufferedWriter.write(member);
                    bufferedWriter.newLine();
                }
            else {
                bufferedWriter.write("You");
                bufferedWriter.newLine();
            }
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        bufferedWriter.write("==============================");
        bufferedWriter.newLine();
        bufferedWriter.write("END");
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private void handleChatRoom() throws IOException {
        sendChatRoomInstructions();
        addClient();

        while (clientState == ClientState.IN_CHAT_ROOM) {
            String message = bufferedReader.readLine();
            if (message == null) {
                clientState = ClientState.DISCONNECTED;
                return;
            }




            if (message.equalsIgnoreCase("/leave")) {
                bufferedWriter.write(ServerResponse.LEAVING_ROOM.toString());
                bufferedWriter.newLine();
                handleLeave();
                return;
            } else if (message.equalsIgnoreCase("/help")) {
                bufferedWriter.write(ServerResponse.CHAT_COMMAND.toString());
                bufferedWriter.newLine();
                printHelpInstructions();
            } else if (message.equalsIgnoreCase("/list")) {
                bufferedWriter.write(ServerResponse.CHAT_COMMAND.toString());
                bufferedWriter.newLine();
                listPeopleInRoom();
            }
            else {
                String formattedMessage = currentUser.getUsername() + ": " + message;
                broadCastMessage(formattedMessage);
            }

        }

    }

    private void sendSuccess(String message) throws IOException {
        bufferedWriter.write(ServerResponse.OK.toString());
        bufferedWriter.newLine();
        bufferedWriter.write(message);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private void sendError(String message) throws IOException {
        bufferedWriter.write(ServerResponse.ERROR.toString());
        bufferedWriter.newLine();
        bufferedWriter.write(message);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public void broadCastMessage(String message) {
        for (ChatClientHandler chatClientHandler : clientHandlers) {
            try {
                if (chatClientHandler != this && chatClientHandler.clientState == ClientState.IN_CHAT_ROOM && chatClientHandler.currentRoomName.equals(this.currentRoomName)) {
                    chatClientHandler.bufferedWriter.write(message);
                    chatClientHandler.bufferedWriter.newLine();
                    chatClientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                chatClientHandler.closeEverything(chatClientHandler.socket, chatClientHandler.bufferedReader, chatClientHandler.bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadCastMessage("[" + currentUser.getUsername() + " left the chat room]");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }

            clientState = ClientState.DISCONNECTED;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}