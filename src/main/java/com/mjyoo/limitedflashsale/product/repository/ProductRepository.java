package com.mjyoo.limitedflashsale.product.repository;

import com.mjyoo.limitedflashsale.product.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 단건 상품 조회 (재고 정보 포함)
    @Query("SELECT p FROM Product p JOIN FETCH p.inventory WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithInventory(@Param("id") Long id);

    // Active 상품 목록 조회 - 페이징 no offset, 1:1 관계에서 항상 필요한 데이터는 fetch join 사용
    @Query("SELECT p FROM Product p JOIN FETCH p.inventory WHERE p.isDeleted = false AND (:cursor = 0 OR p.id < :cursor) ORDER BY p.id DESC")
    Slice<Product> findActiveProductsAndCursor(@Param("cursor") Long cursor, Pageable pageable);

    // 삭제된 상품 목록 조회 - 페이징 no offset
    @Query("SELECT p FROM Product p JOIN FETCH p.inventory WHERE p.isDeleted = true AND (:cursor = 0 OR p.id < :cursor) ORDER BY p.id DESC")
    Slice<Product> findDeletedProductsAndCursor(@Param("cursor") Long cursor, Pageable pageable);

    // ACTIVE 상품만 카운트
    @Query("select count(p) from Product p where p.isDeleted = false")
    Long countActiveProducts();

    // 삭제된 상품만 카운트
    @Query("select count(p) from Product p where p.isDeleted = true")
    Long countDeletedProducts();

}