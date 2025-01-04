package com.mjyoo.limitedflashsale.flashsale.service;


import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.flashsale.dto.FlashSaleListResponseDto;
import com.mjyoo.limitedflashsale.flashsale.dto.FlashSaleRequestDto;
import com.mjyoo.limitedflashsale.flashsale.dto.FlashSaleResponseDto;
import com.mjyoo.limitedflashsale.flashsale.dto.FlashSaleUpdateRequestDto;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleProduct;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleProductStatus;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleStatus;
import com.mjyoo.limitedflashsale.flashsale.repository.FlashSaleProductRepository;
import com.mjyoo.limitedflashsale.flashsale.repository.FlashSaleRepository;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private final ProductRepository productRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleProductRepository flashSaleProductRepository;

    //행사 조회
    public FlashSaleResponseDto getFlashSaleDetail(Long eventId) {
        FlashSale flashSale = getFlashSale(eventId);
        return new FlashSaleResponseDto(flashSale);
    }

    //행사 목록 조회
    public FlashSaleListResponseDto getFlashSaleList() {
        List<FlashSale> flashSaleList = flashSaleRepository.findAll();

        List<FlashSaleResponseDto> flashSaleInfoList = new ArrayList<>();
        for(FlashSale flashSale : flashSaleList) {
            FlashSaleResponseDto flashSaleResponseDto = new FlashSaleResponseDto(flashSale);
            flashSaleInfoList.add(flashSaleResponseDto);
        }
        return new FlashSaleListResponseDto(flashSaleInfoList);
    }

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
                .startTime(requestDto.getStartTime()) // 관리자가 지정한 시작 시간
                .endTime(requestDto.getEndTime()) // 관리자가 지정한 종료 시간
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
                .build( );
        flashSaleProductRepository.save(flashSaleProduct);

        return flashSale.getId();
    }

    //행사 수정
    @Transactional
    public FlashSaleResponseDto updateFlashSale(Long eventId, FlashSaleUpdateRequestDto requestDto, User user) {
        checkAdminRole(user); // 관리자 권한 확인
        FlashSale flashSale = getFlashSale(eventId); // 행사 조회
        // 행사 종료 시 수정 불가
        if(flashSale.getStatus().equals(FlashSaleStatus.ENDED)) {
            throw new CustomException(ErrorCode.INVALID_UPDATE_FLASH_SALE);
        }
        flashSale.update(requestDto);

        // 특정 상품의 FlashSaleProduct 업데이트
        FlashSaleProduct flashSaleProduct = flashSale.getFlashSaleProductList().stream()
                .filter(fsp -> fsp.getId().equals(flashSale.getId()))
                .findAny()
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 할인율 업데이트
        flashSaleProduct.updateDiscountRate(requestDto.getDiscountRate());

        return new FlashSaleResponseDto(flashSale);
    }

    //행사 시작
    @Transactional
    public void openFlashSale(Long eventId, User user) {
        // 관리자 권한 확인
        checkAdminRole(user);
        // 행사 상품 조회
        FlashSaleProduct flashSaleProduct = getFlashSaleProduct(eventId);
        //Active로 상태 변경
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
        // Ended로 상태 변경
        flashSaleProduct.getFlashSale().setStatus(FlashSaleStatus.ENDED);
        flashSaleProductRepository.save(flashSaleProduct);
    }

    public FlashSale getFlashSale(Long eventId) {
        return flashSaleRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.FLASH_SALE_NOT_FOUND));
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
