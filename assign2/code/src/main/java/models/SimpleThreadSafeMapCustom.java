package models;


import interfaces.CustomConcurrentMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleThreadSafeMapCustom<K, V> implements CustomConcurrentMap<K, V> {
    private final Map<K, V> internalMap = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    @Override
    public void put(K key, V value) {
        lock.lock();
        try {
            internalMap.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V get(K key) {
        lock.lock();
        try {
            return internalMap.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsKey(K key) {
        lock.lock();
        try {
            return internalMap.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V remove(K key) {
        lock.lock();
        try {
            return internalMap.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return internalMap.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.lock();
        try {
            return internalMap.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        lock.lock();
        try {
            return new HashMap<>(internalMap).keySet(); // Return a copy to avoid external modification issues
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<V> values() {
        lock.lock();
        try {
            return new HashMap<>(internalMap).values(); // Return a copy
        } finally {
            lock.unlock();
        }
    }

}