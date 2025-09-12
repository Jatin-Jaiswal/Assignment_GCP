package com.autovyn.app.item;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ItemService {
    private final Map<String, Item> items = new LinkedHashMap<>();

    public List<Item> list() {
        return new ArrayList<>(items.values());
    }

    public Optional<Item> get(String id) {
        return Optional.ofNullable(items.get(id));
    }

    public Item create(Item request) {
        Item item = new Item(request.getName(), request.getDescription());
        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> update(String id, Item request) {
        Item existing = items.get(id);
        if (existing == null) return Optional.empty();
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setUpdatedAt(Instant.now());
        return Optional.of(existing);
    }

    public boolean delete(String id) {
        return items.remove(id) != null;
    }
}


