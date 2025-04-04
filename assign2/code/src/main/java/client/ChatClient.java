// client/ChatClient.java
package client;

public class ChatClient {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: <server_address> <port>");
            return;
        }

        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);

        ChatClientHandler clientHandler = new ChatClientHandler(serverAddress, serverPort);
        Thread clientThread = new Thread(clientHandler);
        clientThread.start();
    }
}