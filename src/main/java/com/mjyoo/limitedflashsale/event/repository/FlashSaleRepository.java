package com.mjyoo.limitedflashsale.event.repository;

import com.mjyoo.limitedflashsale.event.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {
}