package com.mjyoo.limitedflashsale.product.repository;

import com.mjyoo.limitedflashsale.product.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}