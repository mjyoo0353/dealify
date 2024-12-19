package com.mjyoo.limitedflashsale.repository;

import com.mjyoo.limitedflashsale.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}