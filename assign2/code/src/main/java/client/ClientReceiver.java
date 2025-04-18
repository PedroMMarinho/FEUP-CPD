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
                ServerResponse response = ServerResponse.fromString(serverMessage);

                // Only process specific chat-related responses
                if (response == ServerResponse.CHAT_MESSAGE ||
                        response == ServerResponse.USER_JOINED ||
                        response == ServerResponse.USER_LEFT ||
                        response == ServerResponse.SYSTEM_MESSAGE) {

                    handleServerResponse(response);
                } else {
                    // Skip other responses - they might be for the main thread
                }
            }
        } catch (SocketException e) {
            if (running) {
                // Only report if we weren't intentionally shutdown
                System.err.println("ClientReceiver: Socket closed unexpectedly.");
                clientHandler.notifyDisconnect();
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("ClientReceiver: I/O error reading from server: " + e.getMessage());
                clientHandler.notifyDisconnect();
            }
        } finally {
            running = false;
        }
    }


    private void handleServerResponse(ServerResponse response) throws IOException {
        switch (response) {
            case USER_JOINED:
                String username = in.readLine();
                System.out.println("[" + username + " enters the room]");
                break;

            case USER_LEFT:
                username = in.readLine();
                System.out.println("[" + username + " left the room]");
                break;

            case CHAT_MESSAGE:
                String sender = in.readLine();
                String message = in.readLine();
                System.out.println(sender + ": " + message);
                break;

            case SYSTEM_MESSAGE:
                String systemMsg = in.readLine();
                System.out.println("[System] " + systemMsg);
                break;

            default:
                System.out.println("Received server response: " + response);
                break;
        }
    }

    public void shutdown() {
        running = false;
    }
}