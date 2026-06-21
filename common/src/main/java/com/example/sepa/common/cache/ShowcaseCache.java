package com.example.sepa.common.cache;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory cache used for showcase profile instead of Redis.
 */
@Component
@Profile("showcase")
public class ShowcaseCache {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    public void put(String key, String value) {
        store.put(key, value);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(store.get(key));
    }

    public void delete(String key) {
        store.remove(key);
    }

    public boolean exists(String key) {
        return store.containsKey(key);
    }

    public void clear() {
        store.clear();
    }
}