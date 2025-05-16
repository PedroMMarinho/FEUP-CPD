package client;

import enums.ClientState;
import enums.Command;
import enums.ServerResponse;
import models.Room;
import models.User;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;

public class ChatClient {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private User user;
    private ClientState clientState = ClientState.AUTHENTICATING;
    private Scanner scanner;

    private final ReentrantReadWriteLock leaveLock = new ReentrantReadWriteLock();
    private final Lock leaveWriteLock = leaveLock.writeLock();
    private final Condition leaveCondition = leaveWriteLock.newCondition();
    private boolean leaveCommandConfirmed = false;

    public ChatClient(Socket socket) {
        try{
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.scanner = new Scanner(System.in);
        }catch (IOException e){
            closeEverything(socket,bufferedReader, bufferedWriter);
        }
    }


    private void updateClientState(){

        while(clientState != ClientState.DISCONNECTED){
            switch(clientState){
                case AUTHENTICATING:
                    authenticate();
                    break;
                case IN_LOBBY:
                    inLobby();
                    break;
                case IN_CHAT_ROOM:
                    inChatRoom();
                    break;
                default:
                    break;
            }
        }

        closeEverything(socket,bufferedReader,bufferedWriter);
    }


    public String sendCommand() throws IOException{
        String command = scanner.nextLine();

        bufferedWriter.write(command);
        bufferedWriter.newLine();
        bufferedWriter.flush();
        return command;
    }

    private void printUntilEnd() throws IOException{
        String line;
        while ((line = bufferedReader.readLine()) != null && !line.equals("END")) {
            System.out.println(line);
        }
    }

    private void sendMessageToChat(String message) throws IOException{
        bufferedWriter.write(message);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }


    public void inChatRoom(){
        try {
            String welcomeMessage = bufferedReader.readLine();
            System.out.println(welcomeMessage);
            listenForMessages();

            while (clientState == ClientState.IN_CHAT_ROOM) {
                String messageToSend = scanner.nextLine();

                if (messageToSend.equalsIgnoreCase("/leave")) {
                    sendMessageToChat("/leave");

                    leaveWriteLock.lock();
                    try {
                        while (!leaveCommandConfirmed) {
                            try {
                                leaveCondition.await();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }

                        clientState = ClientState.IN_LOBBY;
                        leaveCommandConfirmed = false;
                    } finally {
                        leaveWriteLock.unlock();
                    }
                }  else if (messageToSend.equalsIgnoreCase("/help")) {
                    sendMessageToChat("/help");

                } else if (messageToSend.equalsIgnoreCase("/list")) {
                    sendMessageToChat("/list");

                }
                else {
                    sendMessageToChat(messageToSend);
                }
            }
        }catch (Exception e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    public void inLobby() {
        try {
            printUntilEnd();

            while (clientState == ClientState.IN_LOBBY) {
                System.out.print("> ");
                String command = sendCommand();

                String response = bufferedReader.readLine();
                ServerResponse serverResponse = ServerResponse.fromString(response);

                switch (serverResponse){
                    case LOGOUT_USER:
                        clientState = ClientState.DISCONNECTED;
                        break;
                    case OK:
                        clientState = ClientState.IN_CHAT_ROOM;
                        printSuccess();
                        break;
                    case LISTING_ROOMS:
                        printUntilEnd();
                        break;
                    case ERROR:
                        printError();
                        break;
                }
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void printError() throws IOException {
        String errorMessage = bufferedReader.readLine();
        System.out.println(errorMessage);
    }

    public void authenticate(){
        try {
            String welcomeMessage = bufferedReader.readLine();
            System.out.println(welcomeMessage);

            String instructionMessage = bufferedReader.readLine();
            System.out.println(instructionMessage);

            while (clientState == ClientState.AUTHENTICATING){
                System.out.print("> ");
                String command = sendCommand();


                String response = bufferedReader.readLine();
                ServerResponse serverResponse = ServerResponse.fromString(response);

                if (serverResponse == ServerResponse.OK) {
                    clientState = ClientState.IN_LOBBY;
                    String[] parts = command.split(" ", 3);
                    if (parts.length >= 2) {
                        String username = parts[1];
                        this.user = new User(username);
                    }
                    printSuccess();
                } else if (serverResponse == ServerResponse.ERROR) {
                    printError();
                }
            }
        }catch (IOException e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    private void printSuccess() throws  IOException{
        String successMessage = bufferedReader.readLine();
        System.out.println(successMessage);
    }


    public void listenForMessages() {
        Thread.ofVirtual().start(() -> {
            String msgFromChat;

            while (socket.isConnected() && clientState == ClientState.IN_CHAT_ROOM && !leaveCommandConfirmed) {
                try {
                    msgFromChat = bufferedReader.readLine();
                    ServerResponse serverResponse = ServerResponse.fromString(msgFromChat);
                    if (serverResponse == ServerResponse.CHAT_COMMAND) {
                        printUntilEnd();
                    } else if (serverResponse == ServerResponse.LEAVING_ROOM) {
                        printUntilEnd();

                        leaveWriteLock.lock();
                        try {
                            leaveCommandConfirmed = true;
                            leaveCondition.signal();
                        } finally {
                            leaveWriteLock.unlock();
                        }
                    } else {
                        System.out.println(msgFromChat);
                    }
                } catch (IOException e){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        });
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try
        {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: <server_address> <port>");
            return;
        }
        String serverAddress = args[0];
        int serverPort;
        try {
            serverPort = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[1]);
            return;
        }
        Socket socket = new Socket(serverAddress, serverPort);
        ChatClient client = new ChatClient(socket);
        client.updateClientState();
    }
}