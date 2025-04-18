package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    public enum MessageType {
        CHAT,       // Normal chat message
        JOIN,       // User joined the room
        LEAVE,      // User left the room
        SYSTEM      // System notification
    }

    private final String sender;
    private final String content;
    private final MessageType type;
    private final String timestamp;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public Message(String sender, String content, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now().format(formatter);
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        switch (type) {
            case CHAT:
                return String.format("%s (%s): %s", sender, timestamp, content);
            case JOIN:
                return String.format("[%s enters the room]", sender);
            case LEAVE:
                return String.format("[%s left the room]", sender);
            case SYSTEM:
                return String.format("[System] %s", content);
            default:
                return content;
        }
    }
}