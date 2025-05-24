package client;

import enums.ClientState;
import enums.ServerResponse;


import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class ChatClient {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private ClientState clientState = ClientState.AUTHENTICATING;

    private final ReentrantReadWriteLock leaveLock = new ReentrantReadWriteLock();
    private final Lock leaveWriteLock = leaveLock.writeLock();
    private final Condition leaveCondition = leaveWriteLock.newCondition();
    private boolean leaveCommandConfirmed = false;
    private static final Lock printLock = new ReentrantLock();
    private final StringBuilder inputBuffer = new StringBuilder();

    public ChatClient(Socket socket) {
        try{
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
        String command = readLineWithPersistentPrompt("");

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
                String messageToSend = readLineWithPersistentPrompt("");

                if (messageToSend.equalsIgnoreCase("/leave")) {
                    clearConsole();
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
                    clearLastLine();
                    if(!messageToSend.isBlank()){
                        System.out.println("You: " + messageToSend);
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
                        clearConsole();
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
            clearConsole();
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

            String leaveMessage = bufferedReader.readLine();
            System.out.println(leaveMessage);

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
                else if( serverResponse == ServerResponse.EXIT_USER){
                    String line = bufferedReader.readLine();
                    System.out.println(line);
                    clientState = ClientState.DISCONNECTED;
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
        clearConsole();
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
                        printIncomingMessage(msgFromChat);
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

    public String readLineWithPersistentPrompt(String prompt) throws IOException {
        inputBuffer.setLength(0);

        // Enable raw mode (this would need JNI or ProcessBuilder to call stty)
        // For now, we'll work with what we have

        System.out.print(prompt);
        System.out.flush();

        int c;
        while ((c = System.in.read()) != -1) {
            if (c == '\n' || c == '\r') {
                if (c == '\r') {
                    System.in.mark(1);
                    int next = System.in.read();
                    if (next != '\n' && next != -1) {
                        System.in.reset();
                    }
                }
                System.out.print("\n");
                break;
            } else if (c == 127 || c == 8) {
                if (!inputBuffer.isEmpty()) {
                    inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                    System.out.print("\b \b");
                    System.out.flush();
                }
            } else if (c >= 32 && c <= 126) {
                inputBuffer.append((char) c);
                System.out.print((char) c);
                System.out.flush();
            }
        }

        return inputBuffer.toString();
    }

    private void printIncomingMessage(String message) {
        printLock.lock();
        try {
            System.out.print("\033[2K"); // Clear entire current line
            System.out.print("\r");      // Return to beginning of line

            System.out.println(message);

            System.out.print(inputBuffer);
            System.out.flush();
        } finally {
            printLock.unlock();
        }
    }

    private void clearConsole() {
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
    }

    private void clearLastLine(){
        System.out.print("\033[1A"); // Move up
        System.out.print("\033[2K"); // Clear line
        System.out.print("\033[2K");
        System.out.flush();
    }
}