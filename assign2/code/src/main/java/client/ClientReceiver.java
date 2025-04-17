// client/ClientReceiver.java
package client;

import enums.ServerResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;

public class ClientReceiver implements Runnable {
    private final BufferedReader in;
    private volatile boolean running = true;
    private final ChatClientHandler clientHandler;

    public ClientReceiver(BufferedReader in, ChatClientHandler handler) {
        this.in = in;
        this.clientHandler = handler;
    }

    @Override
    public void run() {
        try {
            String serverMessage;

            while (running && !Thread.currentThread().isInterrupted() && (serverMessage = in.readLine()) != null) {

                if (serverMessage.startsWith(ServerResponse.JOINED_ROOM.getResponse())) {
                    String[] parts = serverMessage.split("\\s+", 2);
                    if (parts.length > 1) {
                        clientHandler.setCurrentRoom(parts[1]);
                        System.out.println("Successfully joined room: " + parts[1]);
                    }
                } else if (serverMessage.startsWith("LEFT_ROOM")) { // Example
                    clientHandler.clearCurrentRoom();
                    System.out.println("You have left the room.");
                    System.out.print("> ");
                } else if (serverMessage.startsWith("ERROR") || serverMessage.contains("FAILED")) {
                    System.err.println("Server Error: " + serverMessage);
                    System.out.print(clientHandler.currentRoom != null ? "[" + clientHandler.currentRoom + "] > " : "> ");
                } else {
                    System.out.println(serverMessage); // Display other messages (chat, notifications)
                    System.out.print(clientHandler.currentRoom != null ? "[" + clientHandler.currentRoom + "] > " : "> ");
                }

            }
        } catch (SocketException e) {

            if (running) {
                System.err.println("ClientReceiver: Socket closed unexpectedly. " + e.getMessage());
                clientHandler.running = false;
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("ClientReceiver: I/O error reading from server: " + e.getMessage());
                clientHandler.running = false;
            }
        } finally {
            running = false;
        }
    }

    public void shutdown() {
        running = false;
    }
}