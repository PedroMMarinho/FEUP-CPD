package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Room {
    private final String name;
    private final Set<String> members = new HashSet<>();
    private final Lock membersLock = new ReentrantLock();
    private String owner;
    private List<String> messageHistory = new ArrayList<>();
    private final Lock messageHistoryLock = new ReentrantLock();
    boolean isAiRoom = false;
    private final Lock deletionLock = new ReentrantLock();
    private ScheduledFuture<?> deletionTask;
    private final int ROOM_TIMEOUT = 60;

    public Room(String name) {
        this.name = name;
    }

    public boolean isAiRoom() {
        return isAiRoom;
    }

    public Room(String name, String owner) {
        this.name = name;
        this.owner = owner;
        addMember(owner);
    }

    public List<String> getMessageHistory() {
        messageHistoryLock.lock();
        try {
            return new ArrayList<>(messageHistory);
        }
        finally {
            messageHistoryLock.unlock();
        }
    }

    public void addMessage(String message) {
        messageHistoryLock.lock();
        try {
            messageHistory.add(message);
        }
        finally {
            messageHistoryLock.unlock();
        }
    }

    public void setAiRoom(boolean isAiRoom) {
        this.isAiRoom = isAiRoom;
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
            cancelRoomDeletion();
        } finally {
            membersLock.unlock();
        }
    }

    public void removeMember(String username, ScheduledExecutorService scheduler, Runnable onDelete) {
        membersLock.lock();
        try {
            members.remove(username);
            if(members.isEmpty()){
                scheduleRoomDeletion(scheduler,onDelete);
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
            return new HashSet<>(members);
        } finally {
            membersLock.unlock();
        }
    }

    public void scheduleRoomDeletion(ScheduledExecutorService scheduler, Runnable onDelete){
        deletionLock.lock();
        try{
            if (deletionTask != null && !deletionTask.isDone()) {
                deletionTask.cancel(false);
            }
            deletionTask = scheduler.schedule(onDelete, ROOM_TIMEOUT, TimeUnit.SECONDS);
            System.out.println("Room '" + name + "' scheduled for deletion.");

        } finally {
            deletionLock.unlock();
        }
    }

    public void cancelRoomDeletion(){
        deletionLock.lock();
        try{
            if (deletionTask != null && !deletionTask.isDone()) {
                deletionTask.cancel(false);
                System.out.println("Deletion of room '" + name + "' canceled.");
            }
            deletionTask = null;
        } finally {
            deletionLock.unlock();
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