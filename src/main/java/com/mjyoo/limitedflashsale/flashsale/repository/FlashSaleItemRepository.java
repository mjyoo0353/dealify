package com.mjyoo.limitedflashsale.flashsale.repository;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleItem;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleStatus;
import com.mjyoo.limitedflashsale.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Long> {

    // 특정 상품이 진행중인 할인행사에 포함되어 있는지 조회
    @Query("SELECT fsi FROM FlashSaleItem fsi JOIN FETCH fsi.flashSale fs JOIN FETCH fsi.product p WHERE fsi.product.id = :productId AND fs.status = :status")
    Optional<FlashSaleItem> findByProductIdAndFlashSaleStatus(@Param("productId") Long productId, @Param("status") FlashSaleStatus status);

    // 특정 할인행사에 특정 상품이 포함되어 있는지 조회
    @Query("SELECT fsp FROM FlashSaleItem fsp JOIN FETCH fsp.flashSale fs WHERE fsp.product.id = :productId AND fs.id = :flashSaleId")
    Optional<FlashSaleItem> findByIdAndFlashSaleId(@Param("productId") Long productId, @Param("flashSaleId") Long flashSaleId);

    // 행사 상품 존재 여부 조회
    boolean existsByFlashSaleAndProduct(FlashSale flashSale, Product product);


}