package com.autovyn.app.item;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ItemService {
    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public List<Item> list() {
        return repository.findByOrderByCreatedAtDesc();
    }

    public Optional<Item> get(String id) {
        return repository.findById(id);
    }

    public Item create(Item request) {
        Item item = new Item(request.getName(), request.getDescription());
        return repository.save(item);
    }

    public Optional<Item> update(String id, Item request) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(request.getName());
                    existing.setDescription(request.getDescription());
                    return repository.save(existing);
                });
    }

    public boolean delete(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}


