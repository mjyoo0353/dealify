package com.mjyoo.limitedflashsale.flashsale.repository;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {

    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = :status AND fs.startTime <= :currentTime")
    List<FlashSale> findByStatusAndStartTimeLessThanEqual(@Param("status") FlashSaleStatus status, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = :status AND fs.endTime < :currentTime")
    List<FlashSale> findByStatusAndEndTimeLessThanEqual(@Param("status") FlashSaleStatus status, @Param("currentTime") LocalDateTime currentTime);

}