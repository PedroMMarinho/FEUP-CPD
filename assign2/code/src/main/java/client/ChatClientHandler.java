package client;

import models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientHandler implements Runnable {
    private final String serverAddress;
    private final int serverPort;
    private User currentUser;
    protected volatile boolean running = true; // volatile ensures visibility across threads
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner = new Scanner(System.in);
    protected volatile String currentRoom = null; // volatile as it can be updated by receiver thread
    private ClientReceiver receiver; // Member variable
    private Thread receiverThread;    // Member variable

    public ChatClientHandler(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void setCurrentRoom(String roomName) {
        this.currentRoom = roomName;
        System.out.print(currentRoom != null ? "[" + currentRoom + "] > " : "> ");
    }

    public void clearCurrentRoom() {
        this.currentRoom = null;
        System.out.print("> ");
    }


    @Override
    public void run() {
        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connecting to the chat server at " + serverAddress + ":" + serverPort + "...");

            if (authenticate()) {
                startChatting();
            }

        } catch (IOException ex) {
            if (running) {
                System.out.println("I/O error occurred: " + ex.getMessage());
            } else {
                System.out.println("Connection closed as requested.");
            }
        } finally {
            cleanup();
        }
    }

    private boolean authenticate() throws IOException {

        while (currentUser == null && running) {
            System.out.print("Enter 'LOGIN <username> <password>' or 'REGISTER <username> <password>': ");
            String authInput = scanner.nextLine().trim();

            out.println(authInput);
            String response = in.readLine();

            if (response != null) {

                switch (response) {
                    case "LOGIN_SUCCESS":
                        String[] loginParts = authInput.split("\\s+");
                        if (loginParts.length >= 2) {
                            String username = loginParts[1];
                            currentUser = new User(username);
                            System.out.println("Authentication successful. Welcome, " + currentUser.getUsername() + "!");
                            return true;
                        } else {
                            System.out.println("Error parsing username from login input.");
                        }
                        break;
                }
            } else {
                System.out.println("Connection to server lost during authentication.");
                running = false;
                break;
            }
        }
        return false;
    }


    private void startChatting() {
        receiver = new ClientReceiver(in, this);
        receiverThread = Thread.ofVirtual()
                .name("client-receiver")
                .start(receiver);

        System.out.println("Enter commands (LIST_ROOMS, JOIN_ROOM <room>, CREATE_ROOM <room>, CREATE_AI_ROOM <room> <prompt>, SEND <message>, LOGOUT):");
        System.out.print("> ");
        String userInput;

        try {
            while (running && (userInput = scanner.nextLine()) != null) {
                String[] parts = userInput.split("\\s+", 2);
                String command = parts[0].toUpperCase();
                String data = parts.length > 1 ? parts[1] : "";

                switch (command) {
                    case "LIST_ROOMS":
                    case "CREATE_ROOM":
                    case "CREATE_AI_ROOM":
                        if (!data.isEmpty() || command.equals("LIST_ROOMS")) {
                            out.println(userInput);
                        } else {
                            System.out.println("Usage: " + command + " <arguments>");
                            System.out.print(currentRoom != null ? "[" + currentRoom + "] > " : "> ");
                        }
                        break;
                    case "JOIN_ROOM":
                        if (!data.isEmpty()) {
                            out.println(userInput);
                        } else {
                            System.out.println("Usage: JOIN_ROOM <room_name>");
                            System.out.print(currentRoom != null ? "[" + currentRoom + "] > " : "> "); // Reprint prompt
                        }
                        break;
                    case "SEND":
                        if (currentRoom != null && !data.isEmpty()) {
                            out.println("SEND " + data);
                        } else if (currentRoom == null) {
                            System.out.println("You must join a room first to send a message.");
                            System.out.print("> ");
                        } else {
                            System.out.println("Usage: SEND <message>");
                            System.out.print(currentRoom != null ? "[" + currentRoom + "] > " : "> ");
                        }
                        break;
                    case "LOGOUT":
                        running = false;
                        out.println("LOGOUT");
                        break;
                    default:
                        System.out.println("Invalid command.");
                        System.out.print(currentRoom != null ? "[" + currentRoom + "] > " : "> "); // Reprint prompt
                        break;
                }

                if (!running) {
                    break;
                }

            }
        } catch (Exception e) {
            if (running) {
                System.err.println("Error reading user input: " + e.getMessage());
            }
        } finally {
            running = false;
        }
    }

    private void cleanup() {
        running = false;

        if (receiver != null) {
            receiver.shutdown();
        }

        if (out != null) {
            out.close();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }

        if (receiverThread != null && receiverThread.isAlive()) {
            receiverThread.interrupt();
        }

        if (receiverThread != null) {
            try {
                receiverThread.join(2000);
                if (receiverThread.isAlive()) {
                    System.err.println("Receiver thread did not terminate gracefully.");
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for receiver thread to join.");
                Thread.currentThread().interrupt();
            }
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {

        }

        if (scanner != null) {
            scanner.close();
        }


        System.out.println("Chat client exited successfully.");
    }
}


