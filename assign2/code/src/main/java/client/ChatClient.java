// client/ChatClient.java
package client;

public class ChatClient {
    public static void main(String[] args) {
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

        ChatClientHandler clientHandler = new ChatClientHandler(serverAddress, serverPort);

        Thread clientThread = Thread.ofVirtual()
                .name("chat-client-handler")
                .start(clientHandler);

        try {
            clientThread.join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted while waiting for client: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}