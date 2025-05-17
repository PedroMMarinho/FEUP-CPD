package client;

import enums.ClientState;
import enums.Command;
import enums.ServerResponse;
import models.Room;
import models.ThreadSafeRoomManager;
import models.User;
import models.UserSession;
import server.AuthenticationManager;
import server.LoggedInUserManager;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private UserSession session;

    static {
        authManager = new AuthenticationManager("code/data/serverData/users.txt");
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
        String firstInput = bufferedReader.readLine();
        if (firstInput == null) {
            clientState = ClientState.DISCONNECTED;
            return;
        }

        if (firstInput.startsWith("TOKEN ")) {
            String token = firstInput.substring(6).trim();
            session = authManager.getUserSessionByToken(token);
            if (session != null && !session.isExpired() && session.isActive()) {
                if(loggedInManager.isUserLoggedIn(session.getUsername())) {
                    sendResponse(ServerResponse.INVALID_TOKEN, "User already logged in");
                }
                else {
                    this.currentUser = new User(session.getUsername());
                    loggedInManager.userLoggedIn(session.getUsername());
                    if(session.getRoom() != null && roomManager.roomExists(session.getRoom().getName())){
                        roomManager.getRoomByName(session.getRoom().getName()).addMember(session.getUsername());
                        clientState = ClientState.IN_CHAT_ROOM;
                        this.currentRoomName = session.getRoom().getName();
                        sendResponse(ServerResponse.VALID_TOKEN, "Room");
                    }
                    else {
                        session.removeRoom();
                        clientState = ClientState.IN_LOBBY;
                        sendResponse(ServerResponse.VALID_TOKEN, "Lobby");
                    }
                    sendSuccess(currentUser.getUsername());
                    return;
                }
            } else {
                sendResponse(ServerResponse.INVALID_TOKEN, "Invalid token. Proceeding to manual login.");
            }
        }

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
                    session = authManager.authenticate(username, password);
                    if (session != null) {
                        this.currentUser = new User(username);
                        loggedInManager.userLoggedIn(username);
                        sendResponse(ServerResponse.NEW_TOKEN, session.getToken());
                        sendSuccess("Login successful! Welcome " + username);
                        break;
                    }else {
                        sendError("Invalid username or password. Please try again.");
                    }
                } else if (command.equals(Command.REGISTER)) {
                    if(username.contains(":")){
                        sendError("Invalid username");
                        continue;
                    }
                    session = authManager.registerUser(username, password);
                    if (session != null) {
                        this.currentUser = new User(username);
                        loggedInManager.userLoggedIn(username);
                        sendResponse(ServerResponse.NEW_TOKEN, session.getToken());
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
                            if (roomManager.isAIRoom(room)) {
                                bufferedWriter.write("- AI: " + room);
                            }else{
                                bufferedWriter.write("- Normal: " + room);
                            }
                            bufferedWriter.newLine();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }else {
            bufferedWriter.write("No rooms available");
            bufferedWriter.newLine();
        }
        bufferedWriter.write("===========================");
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
        bufferedWriter.write("JOIN_AI <room>    - Join a chat room or create it with an ai chat bot.");
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
                        Room newRoom = new Room(roomName, currentUser.getUsername());
                        roomManager.addRoom(newRoom);
                        session.setRoom(newRoom);
                        sendSuccess("Created and joined Room: " + roomName);
                    }else{
                        if (!roomManager.isAIRoom(roomName)) {
                            Room room = roomManager.getRoomByName(roomName);
                            room.addMember(currentUser.getUsername());
                            session.setRoom(room);
                            sendSuccess("Joined Room: " + roomName);
                        }else {
                            sendError("Can't join room using this command. Use JOIN_AI to enter.");
                            break;
                        }
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
                case Command.JOIN_AI:
                    if (parts.length < 2) {
                        sendError("Please specify a room name.");
                        break;
                    }
                    String aiRoomName = parts[1];
                    String aiPrompt = "You are a helpful assistant named "+ roomManager.getAIManager().getBOT_NAME() + " in a chat room. Keep your responses concise and helpful.";
                    this.currentRoomName = aiRoomName;
                    if (!roomManager.roomExists(aiRoomName)) {
                        aiPrompt += "Room was created by " + currentUser.getUsername();
                        roomManager.createAIRoom(aiRoomName, currentUser.getUsername(), aiPrompt);
                        session.setRoom(roomManager.getRoomByName(aiRoomName));
                        sendSuccess("Created and joined AI Room: " + aiRoomName);
                    } else if (roomManager.isAIRoom(aiRoomName)) {
                        aiPrompt = currentUser.getUsername() + " has joined the chat room.";
                        roomManager.getRoomByName(aiRoomName).addMember(currentUser.getUsername());
                        roomManager.getAIManager().addUserMessage(currentUser.getUsername(), aiRoomName, aiPrompt);
                        session.setRoom(roomManager.getRoomByName(aiRoomName));
                        sendSuccess("Joined AI Room: " + aiRoomName);
                    } else {
                        sendError("Room exists but is not an AI room. Use JOIN command instead.");
                        break;
                    }
                    clientState = ClientState.IN_CHAT_ROOM;
                    break;
                default:
                    sendError("Unknown command.");
                    break;
            }
        }
    }
    private void handleLogout()  {
        session.closeSession();
        closeEverything(socket, bufferedReader, bufferedWriter);
    }
    private void sendChatRoomInstructions() throws IOException {
        String instructions = "Type messages to chat. Use commands /leave, /list or /help to see a list of commands.";
        if (roomManager.isAIRoom(currentRoomName)) {
            instructions = "Type messages to chat. Use commands /leave, /list, /ai <prompt> ,/help  to see a list of commands.";
        }
        bufferedWriter.write(instructions);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private void sendChatRoomHistory() throws IOException {
        List<String> history = roomManager.getRoomByName(this.currentRoomName).getMessageHistory();
        bufferedWriter.write(ServerResponse.CHAT_COMMAND.toString());
        bufferedWriter.newLine();
        for (String message : history) {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
        }
        bufferedWriter.write("END");
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
        if (roomManager.isAIRoom(currentRoomName)) {
            bufferedWriter.write("/ai <message> - Send a message directly to the Bot");
            bufferedWriter.newLine();
        }
        bufferedWriter.write("===================================");
        bufferedWriter.newLine();
        bufferedWriter.write("END");
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private void handleLeave() throws IOException {
        if (currentRoomName != null && roomManager.roomExists(currentRoomName)) {
            roomManager.removeUserFromRoom(currentRoomName, currentUser.getUsername());
            session.removeRoom();
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

        if (roomManager.isAIRoom(currentRoomName)) {
            bufferedWriter.write("- Bot [AI]");
            bufferedWriter.newLine();
        }

        bufferedWriter.write("==============================");
        bufferedWriter.newLine();
        bufferedWriter.write("END");
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private void handleChatRoom() throws IOException {
        sendChatRoomInstructions();
        addClient();
        sendChatRoomHistory();
        while (clientState == ClientState.IN_CHAT_ROOM) {
            String message = bufferedReader.readLine();
            if (message == null) {
                clientState = ClientState.DISCONNECTED;
                return;
            }

            if (message.equalsIgnoreCase("/leave")) {
                bufferedWriter.write(ServerResponse.LEAVING_ROOM.toString());
                bufferedWriter.newLine();
                roomManager.getAIManager().addUserMessage(currentRoomName, currentUser.getUsername(), currentUser.getUsername() + " left the chat room");
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
            }else if (message.startsWith("/ai ") && roomManager.isAIRoom(currentRoomName)) {
                String aiMessage = message.substring(4);
                handleAIMessage(aiMessage);
            }
            else {
                String formattedMessage = currentUser.getUsername() + ": " + message;
                roomManager.getRoomByName(currentRoomName).addMessage(formattedMessage);
                broadCastMessage(formattedMessage);
            }

        }

    }

    private void broadCastMessageToAll(String message) {
        for (ChatClientHandler chatClientHandler : clientHandlers) {
            try {
                if (chatClientHandler.clientState == ClientState.IN_CHAT_ROOM && chatClientHandler.currentRoomName.equals(this.currentRoomName)) {
                    chatClientHandler.bufferedWriter.write(message);
                    chatClientHandler.bufferedWriter.newLine();
                    chatClientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                chatClientHandler.closeEverything(chatClientHandler.socket, chatClientHandler.bufferedReader, chatClientHandler.bufferedWriter);
            }
        }
    }

    private void handleAIMessage(String message) throws IOException {
        if (!roomManager.isAIRoom(currentRoomName)) {
            sendError("This room doesn't have an AI assistant.");
            return;
        }

        String aiResponse = roomManager.getAIResponse(currentRoomName, currentUser.getUsername(), message);

        if (aiResponse != null) {
            String output = currentUser.getUsername() + ": prompted the following to the ai " + message;
            roomManager.getRoomByName(currentRoomName).addMessage(output);
            roomManager.getRoomByName(currentRoomName).addMessage(aiResponse);
            broadCastMessage(output);
            broadCastMessageToAll(aiResponse);
        } else {
            sendError("The AI assistant couldn't process your request.");
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
        authManager.updateUserSession(session.getToken(), session);
        loggedInManager.userLoggedOut(currentUser.getUsername());
        if(currentRoomName != null){
            roomManager.removeUserFromRoom(currentRoomName, currentUser.getUsername());
        }
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