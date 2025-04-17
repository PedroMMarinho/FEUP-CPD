package models;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Room {
    private final String name;
    private final Set<String> members = new HashSet<>(); // Store usernames of members
    private final Lock membersLock = new ReentrantLock();
    private String owner;

    public Room(String name) {
        this.name = name;
    }

    public Room(String name, String owner) {
        this.name = name;
        this.owner = owner;
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
            members.add(username);
        } finally {
            membersLock.unlock();
        }
    }

    public void removeMember(String username) {
        membersLock.lock();
        try {
            members.remove(username);
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