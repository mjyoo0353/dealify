package com.mjyoo.limitedflashsale.flashsale.repository;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {
}