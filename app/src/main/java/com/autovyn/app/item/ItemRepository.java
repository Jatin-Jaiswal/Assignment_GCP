package com.autovyn.app.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
    List<Item> findByNameContainingIgnoreCase(String name);
    List<Item> findByOrderByCreatedAtDesc();
}
