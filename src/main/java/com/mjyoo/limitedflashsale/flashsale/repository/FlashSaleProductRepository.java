package com.mjyoo.limitedflashsale.flashsale.repository;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlashSaleProductRepository extends JpaRepository<FlashSaleProduct, Long> {

    Optional<FlashSaleProduct> findByProductId(Long id);

}