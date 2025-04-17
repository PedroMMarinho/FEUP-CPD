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