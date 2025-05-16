package server;

import client.ChatClientHandler;
import javax.net.ssl.*;
import java.io.IOException;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

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

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("code/server.keystore"), "password".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, "password".toCharArray());

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), null, null);

            SSLServerSocketFactory ssf = context.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);

            ChatServer server = new ChatServer(port, serverSocket);
            server.start();

        } catch (Exception e){
            e.printStackTrace();
        }

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