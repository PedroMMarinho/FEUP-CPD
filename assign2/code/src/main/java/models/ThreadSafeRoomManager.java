package models;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ThreadSafeRoomManager {
    private final Set<Room> rooms = new HashSet<>();
    private final Lock lock = new ReentrantLock();

    public void addRoom(Room room) {
        lock.lock();
        try {
            rooms.add(room);
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