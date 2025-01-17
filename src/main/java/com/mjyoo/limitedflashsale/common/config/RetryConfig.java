
package com.mjyoo.limitedflashsale.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry // Spring Retry AOP 기능 활성화
public class RetryConfig {

}

