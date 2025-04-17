package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    private int serverPort;
    private AuthenticationManager authenticationManager;
    private final LoggedInUserManager loggedInUserManager;

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
        this.loggedInUserManager = new LoggedInUserManager();
        System.out.println("Chat server initializing on port: " + serverPort);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server is listening on port " + serverPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                Thread.startVirtualThread(() -> {
                    try {
                        ServerHandler handler = new ServerHandler(clientSocket, this);
                        handler.run();
                    } catch (Exception e) {
                        System.err.println("Error handling client in virtual thread: " + e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Could not start server or handle connection: " + e.getMessage());
        }
    }
    public boolean isUserLoggedIn(String username) {
        return loggedInUserManager.isUserLoggedIn(username);
    }

    public void userLoggedIn(String username) {
        loggedInUserManager.userLoggedIn(username);
    }

    public void userLoggedOut(String username) {
        loggedInUserManager.userLoggedOut(username);
    }
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }
}