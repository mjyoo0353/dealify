package com.mjyoo.limitedflashsale.product.repository;

import com.mjyoo.limitedflashsale.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    //상품 리스트 조회 (기본 조회 - 삭제되지 않은 데이터)
    @Query("select p from Product p where p.isDeleted = false")
    List<Product> findAllActive();

    //상품 리스트 조회 (삭제된 데이터)
    @Query("select p from Product p where p.isDeleted = true")
    List<Product> findDeletedProducts();

    //삭제되지 않은 상품만 카운트
    @Query("select count(p) from Product p where p.isDeleted = false")
    Long countActiveProducts();

    //삭제된 상품만 카운트
    @Query("select count(p) from Product p where p.isDeleted = true")
    Long countDeletedProducts();

}