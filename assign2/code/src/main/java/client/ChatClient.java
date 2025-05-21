package client;

import enums.ClientState;
import enums.ServerResponse;


import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import javax.net.ssl.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class ChatClient {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
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


    public void sendCommand() throws IOException{
        String command = scanner.nextLine();

        bufferedWriter.write(command);
        bufferedWriter.newLine();
        bufferedWriter.flush();
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
                    if(!messageToSend.isBlank()){
                        sendMessageToChat(messageToSend);
                    }
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
                sendCommand();

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
            sendToken();

            String tokenResponse = bufferedReader.readLine();
            if (ServerResponse.fromString(tokenResponse) == ServerResponse.VALID_TOKEN) {
                String type = bufferedReader.readLine();
                if(type.equals("Room")){
                    clientState = ClientState.IN_CHAT_ROOM;
                }
                else {
                    clientState = ClientState.IN_LOBBY;
                }

                bufferedReader.readLine();
                String username = bufferedReader.readLine();

                System.out.println("Resumed session. Welcome back " + username + "!");
                return;
            } else {
                printError();
            }

            String welcomeMessage = bufferedReader.readLine();
            System.out.println(welcomeMessage);

            String instructionMessage = bufferedReader.readLine();
            System.out.println(instructionMessage);

            while (clientState == ClientState.AUTHENTICATING){
                System.out.print("> ");
                sendCommand();


                String response = bufferedReader.readLine();
                ServerResponse serverResponse = ServerResponse.fromString(response);

                if (serverResponse == ServerResponse.ERROR) {
                    printError();
                }
                else if(serverResponse == ServerResponse.NEW_TOKEN){
                    String token = bufferedReader.readLine();
                    Path path = Paths.get("data");
                    Files.createDirectories(path);
                    Path filePath = Paths.get("data/client.token");
                    if (!Files.exists(filePath)) {
                        Files.createFile(filePath);
                    }
                    try (FileWriter writer = new FileWriter("data/client.token", false)) {
                        writer.write(token);
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    bufferedReader.readLine();

                    clientState = ClientState.IN_LOBBY;
                    printSuccess();
                }
            }
        }catch (IOException e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    private void sendToken(){
        Path tokenPath = Paths.get("data/client.token");
        if (Files.exists(tokenPath)) {
            try {
                String token = Files.readString(tokenPath).trim();
                bufferedWriter.write("TOKEN " + token);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // No token file exists, so send empty token or nothing
            try {
                bufferedWriter.write("TOKEN ");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public static void main(String[] args)  {
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

        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            InputStream tsFile = ChatClient.class.getClassLoader().getResourceAsStream("clienttruststore.jks");
            if (tsFile == null) {
                throw new FileNotFoundException("Could not find clienttruststore.jks in classpath");
            }
            trustStore.load(tsFile, "trustpassword".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(serverAddress, serverPort);

            ChatClient client = new ChatClient(socket);
            client.updateClientState();

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}