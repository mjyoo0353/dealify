package com.mjyoo.limitedflashsale.event.service;


import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.event.dto.FlashSaleRequestDto;
import com.mjyoo.limitedflashsale.event.entity.FlashSale;
import com.mjyoo.limitedflashsale.event.entity.FlashSaleProduct;
import com.mjyoo.limitedflashsale.event.entity.FlashSaleProductStatus;
import com.mjyoo.limitedflashsale.event.entity.FlashSaleStatus;
import com.mjyoo.limitedflashsale.event.repository.FlashSaleProductRepository;
import com.mjyoo.limitedflashsale.event.repository.FlashSaleRepository;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private final ProductRepository productRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleProductRepository flashSaleProductRepository;

    //행사 생성
    @Transactional
    public Long createEvent(@Valid FlashSaleRequestDto requestDto) {
        // 상품 조회
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 행사 생성 및 저장
        FlashSale flashSale = FlashSale.builder()
                .name(requestDto.getName())
                .startTime(requestDto.getStartTime()) // 사용자가 지정한 시작 시간
                .endTime(requestDto.getEndTime()) // 사용자가 지정한 종료 시간
                .status(FlashSaleStatus.SCHEDULED)
                .build();
        flashSaleRepository.save(flashSale);

        BigDecimal originalPrice = product.getPrice();
        BigDecimal discountRate = requestDto.getDiscountRate();

        // 행사상품 스냅샷 생성 및 저장
        FlashSaleProduct flashSaleProduct = FlashSaleProduct.builder()
                .flashSale(flashSale)
                .product(product)
                .originalPrice(originalPrice)
                .discountRate(discountRate)
                .discountedPrice(originalPrice.subtract(originalPrice.multiply(discountRate)))
                .initialStock(product.getInventory().getStock())
                .status(FlashSaleProductStatus.AVAILABLE)
                .build();
        flashSaleProductRepository.save(flashSaleProduct);

        return flashSale.getId();
    }

    //TODO 행사 삭제 - Soft Delete
    public void deleteEvent(Long eventId) {

    }

}
