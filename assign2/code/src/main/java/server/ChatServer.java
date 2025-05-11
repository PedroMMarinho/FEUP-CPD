package server;

import client.ChatClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    private int serverPort;
    private ServerSocket serverSocket;


    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: Missing <port>");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number specified: " + args[0]);
            return;
        }

        ServerSocket serverSocket = new ServerSocket(port);
        ChatServer server = new ChatServer(port, serverSocket);
        server.start();
    }

    public ChatServer(int port, ServerSocket serverSocket) {
        this.serverPort = port;
        this.serverSocket = serverSocket;
        System.out.println("Chat server initializing on port: " + serverPort);
    }

    public void start() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                ChatClientHandler client = new ChatClientHandler(clientSocket);

                Thread.startVirtualThread(() -> {
                    try {
                        client.run();
                    }catch (Exception e) {
                        closeServerSocket();
                    }
                });
            }
        }catch (IOException e){
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try{
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}