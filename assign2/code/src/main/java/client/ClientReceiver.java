// client/ClientReceiver.java
package client;

import java.io.BufferedReader;
import java.io.IOException;

public class ClientReceiver implements Runnable {
    private final BufferedReader in;
    private volatile boolean running = true; // Optional: Add a running flag

    public ClientReceiver(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String serverMessage;
            while (running && !Thread.currentThread().isInterrupted() && (serverMessage = in.readLine()) != null) {
                System.out.println("Server: " + serverMessage);
            }
        } catch (IOException e) {
            System.err.println("ClientReceiver: Connection closed by server or I/O error.");
        } finally {
            System.out.println("ClientReceiver: Exiting.");
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}