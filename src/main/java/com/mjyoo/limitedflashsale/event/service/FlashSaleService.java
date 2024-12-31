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
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private final ProductRepository productRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleProductRepository flashSaleProductRepository;

    //TODO 행사 조회
    //TODO 행사 목록 조회

    //행사 생성
    @Transactional
    public Long createFlashSale(@Valid FlashSaleRequestDto requestDto, User user) {
        // 관리자 권한 확인
        checkAdminRole(user);

        // 상품 조회
        Product product = getProduct(requestDto);

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

    // TODO 행사 수정
    public void updateFlashSale(Long eventId, FlashSaleRequestDto requestDto, User user) {
    }

    //행사 시작
    @Transactional
    public void openFlashSale(Long eventId, User user) {
        // 관리자 권한 확인
        checkAdminRole(user);

        // 행사 상품 조회
        FlashSaleProduct flashSaleProduct = getFlashSaleProduct(eventId);

        // 행사 조회
        FlashSale flashSale = flashSaleProduct.getFlashSale();
        // 행사 시작 시간이 현재 시간보다 미래인지 확인하고 미래라면 예외 발생
        if(LocalDateTime.now().isBefore(flashSale.getStartTime())) {
            throw new CustomException(ErrorCode.FLASH_SALE_NOT_STARTED);
        }
        flashSaleProduct.getFlashSale().setStatus(FlashSaleStatus.ACTIVE);
        flashSaleProductRepository.save(flashSaleProduct);
    }

    //행사 종료
    @Transactional
    public void closeFlashSale(Long eventId, User user) {
        // 관리자 권한 확인
        checkAdminRole(user);

        // 행사 상품 조회
        FlashSaleProduct flashSaleProduct = getFlashSaleProduct(eventId);

        flashSaleProduct.getFlashSale().setStatus(FlashSaleStatus.ENDED);
        flashSaleProductRepository.save(flashSaleProduct);
    }

    private Product getProduct(FlashSaleRequestDto requestDto) {
        return productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private FlashSaleProduct getFlashSaleProduct(Long eventId) {
        return flashSaleProductRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void checkAdminRole(User user) {
        if (user == null && !user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

}
