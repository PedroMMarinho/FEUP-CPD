package interfaces;

import java.util.Set;
import java.util.Collection;

public interface CustomConcurrentMap<K, V> {
    void put(K key, V value);
    V get(K key);
    boolean containsKey(K key);
    V remove(K key);
    int size();
    boolean isEmpty();
    Set<K> keySet();
    Collection<V> values();
}