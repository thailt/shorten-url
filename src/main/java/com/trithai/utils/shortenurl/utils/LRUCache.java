package com.trithai.utils.shortenurl.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LRUCache(int capacity) {
        // Initial capacity, load factor, and accessOrder=true for LRU behavior
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        // This method is called after a put() operation.
        // Return true to remove the eldest entry (LRU item) if size exceeds capacity.
        return size() > capacity;
    }

    // You can add specific get() and put() methods if desired,
    // but LinkedHashMap's methods can be used directly.
    public V getEntry(K key) {
        return super.get(key);
    }

    public void putEntry(K key, V value) {
        super.put(key, value);
    }
}
