package models;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ThreadSafeRoomManager {
    private final Set<Room> rooms = new HashSet<>();
    private final Lock lock = new ReentrantLock();
    private final AIManager aiManager = new AIManager();


    public void addRoom(Room room) {
        lock.lock();
        try {
            rooms.add(room);
        } finally {
            lock.unlock();
        }
    }
    public AIManager getAIManager() {
        return aiManager;
    }
    public String getAIResponse(String roomName, String username, String message) {
        if (!isAIRoom(roomName)) {
            return null;
        }

        aiManager.addUserMessage(roomName, username, message);

        return aiManager.getAIResponse(roomName);
    }

    public void createAIRoom(String name, String owner, String prompt) {
        lock.lock();
        try {
            Room room = new Room(name, owner);
            room.setAiRoom(true);
            rooms.add(room);
            aiManager.createAIRoom(name, prompt);
        } finally {
            lock.unlock();
        }
    }

    public boolean isAIRoom(String roomName) {
        lock.lock();
        try {
            Room room = getRoomByName(roomName);
            return room != null && room.isAiRoom();
        } finally {
            lock.unlock();
        }
    }

    public void removeRoom(String roomName) {
        lock.lock();
        try {
            rooms.removeIf(room -> room.getName().equals(roomName));
        } finally {
            lock.unlock();
        }
    }

    public boolean roomExists(String roomName) {
        lock.lock();
        try {
            return rooms.stream().anyMatch(room -> room.getName().equals(roomName));
        } finally {
            lock.unlock();
        }
    }

    public Room getRoomByName(String roomName) {
        lock.lock();
        try {
            return rooms.stream()
                    .filter(room -> room.getName().equals(roomName))
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.unlock();
        }
    }

    public String getAvailableRooms() {
        lock.lock();
        try {
            return rooms.stream()
                    .map(Room::getName)
                    .sorted()
                    .collect(Collectors.joining(", "));
        } finally {
            lock.unlock();
        }
    }

    public Set<Room> getAllRooms() {
        lock.lock();
        try {
            return new HashSet<>(rooms);
        } finally {
            lock.unlock();
        }
    }
}