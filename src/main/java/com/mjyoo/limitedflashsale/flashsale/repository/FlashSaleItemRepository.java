package com.mjyoo.limitedflashsale.flashsale.repository;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleItem;
import com.mjyoo.limitedflashsale.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Long> {

    @Query("SELECT fsi FROM FlashSaleItem fsi JOIN FETCH fsi.flashSale fs WHERE fsi.product.id = :productId AND fs.status = 'ACTIVE'")
    Optional<FlashSaleItem> findByProductIdAndFlashSaleStatus(@Param("productId") Long productId);

    boolean existsByFlashSaleAndProduct(FlashSale flashSale, Product product);

    @Query("SELECT fsp FROM FlashSaleItem fsp JOIN FETCH fsp.flashSale fs WHERE fsp.product.id = :productId AND fs.id = :flashSaleId")
    Optional<FlashSaleItem> findByIdAndFlashSaleId(@Param("productId") Long productId, @Param("flashSaleId") Long flashSaleId);

}