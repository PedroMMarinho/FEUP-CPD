package client;

import enums.ClientState;
import enums.ServerResponse;
import models.Room;
import models.User;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private User user;
    private Room room;
    private ClientState clientState = ClientState.AUTHENTICATING;
    private Scanner scanner;

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
                    case JOIN_ROOM:
                        break;
                    case CREATED_ROOM:
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

    public void sendMessage() {
        try{
            bufferedWriter.write(user.getUsername());
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(user.getUsername() + ": " + messageToSend);
            }
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessages() {
        Thread.ofVirtual().start(() -> {
            String msgFromChat;

            while (socket.isConnected()) {
                try {
                    msgFromChat = bufferedReader.readLine();
                    System.out.println(msgFromChat);
                }catch (IOException e){
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