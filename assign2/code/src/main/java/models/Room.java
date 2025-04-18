package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Room {
    private final String name;
    private final Set<String> members = new HashSet<>(); // Store usernames of members
    private final Lock membersLock = new ReentrantLock();
    private String owner;

    // Add message history with lock
    private final List<Message> messageHistory = new ArrayList<>();
    private final Lock messagesLock = new ReentrantLock();

    public Room(String name) {
        this.name = name;
    }

    public Room(String name, String owner) {
        this.name = name;
        this.owner = owner;
        addMember(owner);
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void addMember(String username) {
        membersLock.lock();
        try {
            boolean isNewMember = !members.contains(username);
            members.add(username);

            // Create a join message if this is a new member
            if (isNewMember) {
                addMessage(new Message(username, "", Message.MessageType.JOIN));
            }
        } finally {
            membersLock.unlock();
        }
    }

    public void removeMember(String username) {
        membersLock.lock();
        try {
            boolean wasMember = members.remove(username);

            // Create a leave message if the user was a member
            if (wasMember) {
                addMessage(new Message(username, "", Message.MessageType.LEAVE));
            }
        } finally {
            membersLock.unlock();
        }
    }

    public boolean isMember(String username) {
        membersLock.lock();
        try {
            return members.contains(username);
        } finally {
            membersLock.unlock();
        }
    }

    public Set<String> getMembers() {
        membersLock.lock();
        try {
            return new HashSet<>(members); // Return a copy to avoid external modification without locking
        } finally {
            membersLock.unlock();
        }
    }

    public void addMessage(Message message) {
        messagesLock.lock();
        try {
            messageHistory.add(message);
            // Keep message history manageable (e.g., last 100 messages)
            if (messageHistory.size() > 100) {
                messageHistory.remove(0);
            }
        } finally {
            messagesLock.unlock();
        }
    }

    public List<Message> getRecentMessages(int count) {
        messagesLock.lock();
        try {
            int size = messageHistory.size();
            int startIndex = Math.max(0, size - count);
            return new ArrayList<>(messageHistory.subList(startIndex, size));
        } finally {
            messagesLock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return name.equals(room.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                (owner != null ? ", owner='" + owner + '\'' : "") +
                '}';
    }
}