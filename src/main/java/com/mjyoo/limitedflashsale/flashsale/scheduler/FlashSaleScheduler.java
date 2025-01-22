package com.mjyoo.limitedflashsale.flashsale.scheduler;

import com.mjyoo.limitedflashsale.common.config.SchedulerConfig;
import com.mjyoo.limitedflashsale.flashsale.service.FlashSaleSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlashSaleScheduler {

    private final FlashSaleSchedulerService flashSaleSchedulerService;
    private final SchedulerConfig schedulerConfig;

    /**
     * Flash Sale 오픈 스케줄러
     * 실행 주기: 매일 오후 2시
     */
    @Scheduled(cron = "${scheduler.flash-sale.open-time}")
    public void openScheduledFlashSales() {
        log.info("Opening flash sale...: {}", schedulerConfig.getFlashSaleOpenTime());
        List<Long> scheduledFlashSales = flashSaleSchedulerService.getScheduledFlashSales();

        if(scheduledFlashSales.isEmpty()) {
            log.info("No scheduled flash sales found.");
            return;
        }

        log.info("Scheduled flash sales found: {}", scheduledFlashSales.size());
        for (Long flashSaleId : scheduledFlashSales) {
            try {
                flashSaleSchedulerService.openFlashSale(flashSaleId);
                log.info("Flash sale successfully opened - flashSaleId: {}", flashSaleId);
            } catch (Exception e) {
                log.error("Failed to open scheduled flash sale - flashSaleId: {}", flashSaleId, e);
            }
        }
    }

    /**
     * Flash Sale 종료 스케줄러
     * 실행 주기: 매일 오후 3시
     */
    @Scheduled(cron = "${scheduler.flash-sale.close-time}")
    public void closeScheduledFlashSales() {
        log.info("Closing flash sale...{}", schedulerConfig.getFlashSaleCloseTime());
        List<Long> scheduledFlashSales = flashSaleSchedulerService.getClosedFlashSales();

        if(scheduledFlashSales.isEmpty()) {
            log.info("No scheduled flash sales for closing found.");
            return;
        }

        for (Long flashSaleId : scheduledFlashSales) {
            try {
                flashSaleSchedulerService.closeFlashSale(flashSaleId);
                log.info("Flash sale successfully closed - flashSaleId: {}", flashSaleId);
            } catch (Exception e) {
                log.error("Failed to close scheduled flash sale - flashSaleId: {}", flashSaleId, e);
            }
        }
    }
}