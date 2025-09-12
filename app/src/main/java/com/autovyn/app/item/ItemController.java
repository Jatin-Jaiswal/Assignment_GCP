package com.autovyn.app.item;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/v1/items")
public class ItemController {
    private final ItemService service;

    private final com.autovyn.app.events.EventPublisher eventPublisher;

    public ItemController(ItemService service, com.autovyn.app.events.EventPublisher eventPublisher) {
        this.service = service;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping
    public List<Item> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> get(@PathVariable String id) {
        return service.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Item> create(@Valid @RequestBody Item request) {
        Item created = service.create(request);
        eventPublisher.publish("item.created", java.util.Map.of("id", created.getId(), "name", created.getName()));
        return ResponseEntity.created(URI.create("/v1/items/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable String id, @Valid @RequestBody Item request) {
        return service.update(id, request)
                .map(updated -> {
                    eventPublisher.publish("item.updated", java.util.Map.of("id", updated.getId(), "name", updated.getName()));
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean removed = service.delete(id);
        if (removed) {
            eventPublisher.publish("item.deleted", java.util.Map.of("id", id));
        }
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}


