package server;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LoggedInUserManager {

    private final Set<String> loggedInUsers = new HashSet<>();
    private final Lock lock = new ReentrantLock();

    public boolean isUserLoggedIn(String username) {
        lock.lock();
        try {
            return loggedInUsers.contains(username);
        } finally {
            lock.unlock();
        }
    }

    public void userLoggedIn(String username) {
        lock.lock();
        try {
            loggedInUsers.add(username);
        } finally {
            lock.unlock();
        }
    }

    public void userLoggedOut(String username) {
        lock.lock();
        try {
            loggedInUsers.remove(username);
        } finally {
            lock.unlock();
        }
    }
}