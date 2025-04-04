package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private int serverPort;
    private AuthenticationManager authenticationManager;
    private ExecutorService executorService;

    public static void main(String[] args) {
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

        ChatServer server = new ChatServer(port);
        server.start();
    }

    public ChatServer(int port) {
        this.serverPort = port;
        this.authenticationManager = new AuthenticationManager("src/main/java/server/data/users.txt");
        this.executorService = Executors.newCachedThreadPool();
        System.out.println("Chat server initializing on port: " + serverPort);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server is listening on port " + serverPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                ServerHandler handler = new ServerHandler(clientSocket, this, authenticationManager);
                executorService.submit(handler);
            }
        } catch (IOException e) {
            System.err.println("Could not start server or handle connection: " + e.getMessage());
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
        }
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

}