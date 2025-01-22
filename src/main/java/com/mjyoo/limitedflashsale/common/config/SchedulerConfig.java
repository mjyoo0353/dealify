package com.mjyoo.limitedflashsale.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Getter
public class SchedulerConfig {

    @Value("${scheduler.order.sync-time}")
    private String orderSyncTime;

    @Value("${scheduler.flash-sale.open-time}")
    private String flashSaleOpenTime;

    @Value("${scheduler.flash-sale.close-time}")
    private String flashSaleCloseTime;
}
