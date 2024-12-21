package com.mjyoo.limitedflashsale.repository;

import com.mjyoo.limitedflashsale.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}