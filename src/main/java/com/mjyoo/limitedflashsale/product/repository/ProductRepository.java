package com.mjyoo.limitedflashsale.product.repository;

import com.mjyoo.limitedflashsale.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdOptimisticLock(@Param("id") Long id);

    //상품 최신 목록 조회 (Active 상품만 조회)
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    Slice<Product> findAllActiveProducts(Pageable pageable);

    //Cursor 기준 이전 상품 목록 조회 (Active 상품만 조회)
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.id < :cursor ORDER BY p.createdAt DESC")
    Slice<Product> findAllActiveProductsAndIdLessThan(@Param("cursor") Long cursor, Pageable pageable);

    //상품 리스트 조회 (삭제된 데이터)
    @Query("SELECT p FROM Product p WHERE p.isDeleted = true AND p.id < :cursor ORDER BY p.createdAt DESC")
    Slice<Product> findDeletedProducts(@Param("cursor") Long cursor, Pageable pageable);

    //삭제되지 않은 상품만 카운트
    @Query("select count(p) from Product p where p.isDeleted = false")
    Long countActiveProducts();

    //삭제된 상품만 카운트
    @Query("select count(p) from Product p where p.isDeleted = true")
    Long countDeletedProducts();

}