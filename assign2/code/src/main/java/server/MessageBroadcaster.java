package server;

import enums.ServerResponse;
import models.Message;
import models.Room;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageBroadcaster {
    private final Map<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();
    private final Lock clientWritersLock = new ReentrantLock();

    public void registerClient(String username, PrintWriter writer) {
        clientWritersLock.lock();
        try {
            clientWriters.put(username, writer);
        } finally {
            clientWritersLock.unlock();
        }
    }

    public void unregisterClient(String username) {
        clientWritersLock.lock();
        try {
            clientWriters.remove(username);
        } finally {
            clientWritersLock.unlock();
        }
    }

    public void broadcastMessage(Room room, Message message) {
        Set<String> roomMembers = room.getMembers();

        clientWritersLock.lock();
        try {
            for (String member : roomMembers) {
                PrintWriter writer = clientWriters.get(member);
                if (writer != null) {
                    switch (message.getType()) {
                        case JOIN:
                            writer.println(ServerResponse.USER_JOINED);
                            writer.println(message.getSender());
                            break;
                        case LEAVE:
                            writer.println(ServerResponse.USER_LEFT);
                            writer.println(message.getSender());
                            break;
                        case CHAT:
                            writer.println(ServerResponse.CHAT_MESSAGE);
                            writer.println(message.getSender());
                            writer.println(message.getContent());
                            break;
                        case SYSTEM:
                            writer.println(ServerResponse.SYSTEM_MESSAGE);
                            writer.println(message.getContent());
                            break;
                    }
                }
            }
        } finally {
            clientWritersLock.unlock();
        }
    }
}