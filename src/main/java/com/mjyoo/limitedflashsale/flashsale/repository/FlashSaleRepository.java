package com.mjyoo.limitedflashsale.flashsale.repository;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {

    // 스케줄러 - 행사 자동 시작
    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = :status AND fs.startTime <= :currentTime")
    List<FlashSale> findByStatusAndStartTimeLessThanEqual(@Param("status") FlashSaleStatus status, @Param("currentTime") LocalDateTime currentTime);

    // 스케줄러 - 행사 자동 종료
    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = :status AND fs.endTime < :currentTime")
    List<FlashSale> findByStatusAndEndTimeLessThanEqual(@Param("status") FlashSaleStatus status, @Param("currentTime") LocalDateTime currentTime);

    // 행사 조회 + 상품 조회
    @Query("SELECT fs FROM FlashSale fs LEFT JOIN FETCH fs.flashSaleItemList WHERE fs.id = :flashSaleId")
    Optional<FlashSale> findByIdWithProducts(@Param("flashSaleId") Long flashSaleId);

    // 행사 목록 조회 - 페이징 no offset
    @Query(value = "SELECT DISTINCT fs FROM FlashSale  fs LEFT JOIN FETCH fs.flashSaleItemList WHERE (:cursor IS NULL OR fs.id < :cursor) ORDER BY fs.id DESC",
            countQuery = "SELECT COUNT(fs) FROM FlashSale fs")
    Slice<FlashSale> findAllWithProductsAndCursor(Long cursor, PageRequest pageRequest);
}