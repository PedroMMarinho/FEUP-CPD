package models;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ThreadSafeRoomList {
    private final Set<String> rooms = new HashSet<>();
    private final Lock lock = new ReentrantLock();

    public void addRoom(String roomName) {
        lock.lock();
        try {
            rooms.add(roomName);
        } finally {
            lock.unlock();
        }
    }

    public boolean roomExists(String roomName) {
        lock.lock();
        try {
            return rooms.contains(roomName);
        } finally {
            lock.unlock();
        }
    }

    public String getAvailableRooms() {
        lock.lock();
        try {
            return rooms.stream().collect(Collectors.joining(", "));
        } finally {
            lock.unlock();
        }
    }

}