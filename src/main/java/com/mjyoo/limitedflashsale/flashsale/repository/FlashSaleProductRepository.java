package com.mjyoo.limitedflashsale.flashsale.repository;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleProduct;
import com.mjyoo.limitedflashsale.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FlashSaleProductRepository extends JpaRepository<FlashSaleProduct, Long> {

    @Query("SELECT fsp FROM FlashSaleProduct fsp JOIN FETCH fsp.flashSale fs WHERE fsp.product.id = :productId AND fs.status = 'ONGOING'")
    Optional<FlashSaleProduct> findByProductIdAndFlashSaleStatus(@Param("productId") Long productId);

    boolean existsByFlashSaleAndProduct(FlashSale flashSale, Product product);
}