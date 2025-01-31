package com.mjyoo.limitedflashsale.flashsale.service;


import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.flashsale.dto.*;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleItem;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleItemStatus;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleStatus;
import com.mjyoo.limitedflashsale.flashsale.repository.FlashSaleItemRepository;
import com.mjyoo.limitedflashsale.flashsale.repository.FlashSaleRepository;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private final ProductRepository productRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;

    //행사 조회
    public FlashSaleResponseDto getFlashSaleDetail(Long flashSaleId) {
        FlashSale flashSale = findFlashSaleWithProduct(flashSaleId);
        return new FlashSaleResponseDto(flashSale);
    }

    //행사 목록 조회
    public FlashSaleListResponseDto getFlashSaleList(Long cursor, int size) {
        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<FlashSale> flashSaleList = flashSaleRepository.findAllWithProductsAndCursor(cursor, pageRequest);

        List<FlashSaleResponseDto> flashSaleInfoList = new ArrayList<>();
        for (FlashSale flashSale : flashSaleList) {
            flashSaleInfoList.add(new FlashSaleResponseDto(flashSale));
        }

        Long nextCursor = flashSaleList.hasNext() && !flashSaleList.isEmpty() ? flashSaleInfoList.get(flashSaleInfoList.size() - 1).getId() : null;
        return new FlashSaleListResponseDto(flashSaleInfoList, nextCursor);
    }

    //행사 생성
    @Transactional
    public Long createFlashSale(@Valid FlashSaleRequestDto requestDto, User user) {
        // 관리자 권한 확인
        checkAdminRole(user);

        // 행사 생성 및 저장
        FlashSale flashSale = FlashSale.builder()
                .name(requestDto.getName())
                .startTime(requestDto.getStartTime()) // 관리자가 지정한 시작 시간
                .endTime(requestDto.getEndTime()) // 관리자가 지정한 종료 시간
                .status(FlashSaleStatus.SCHEDULED)
                .build();

        flashSaleRepository.save(flashSale);
        return flashSale.getId();
    }

    //행사 수정
    @Transactional
    public FlashSaleResponseDto updateFlashSale(Long flashSaleId, FlashSaleUpdateRequestDto requestDto, User user) {
        checkAdminRole(user); // 관리자 권한 확인
        FlashSale flashSale = findFlashSale(flashSaleId); // 행사 조회

        // 행사 종료 시 수정 불가
        if (flashSale.getStatus().equals(FlashSaleStatus.ENDED)) {
            throw new CustomException(ErrorCode.INVALID_UPDATE_FLASH_SALE);
        }
        flashSale.update(requestDto);

        return new FlashSaleResponseDto(flashSale);
    }

    //행사 삭제
    @Transactional
    public void deleteFlashSale(Long flashSaleId, User user) {
        // 관리자 권한 확인
        checkAdminRole(user);
        // 행사 조회
        FlashSale flashSale = findFlashSale(flashSaleId);
        // 행사 진행중 또는 종료 시 삭제 불가
        if (flashSale.getStatus().equals(FlashSaleStatus.ENDED) || flashSale.getStatus().equals(FlashSaleStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.INVALID_DELETE_FLASH_SALE);
        }
        flashSaleRepository.delete(flashSale);
    }

    //행사 상품 추가
    @Transactional
    public void addFlashSaleItem(Long flashSaleId, @Valid FlashSaleItemRequestDto requestDto, User user) {
        // 관리자 권한 확인
        checkAdminRole(user);
        // 행사 조회
        FlashSale flashSale = findFlashSale(flashSaleId);
        // 행사 시작 전에만 상품 추가 가능
        if (flashSale.getStatus() != FlashSaleStatus.SCHEDULED) {
            throw new CustomException(ErrorCode.FLASH_SALE_NOT_SCHEDULED);
        }
        // 상품 조회
        Product product = findByIdWithInventory(requestDto.getProductId());

        // 행사 상품 중복 확인
        boolean exists = flashSaleItemRepository.existsByFlashSaleAndProduct(flashSale, product);
        if (exists) {
            throw new CustomException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }

        // 재고 확인
        if (requestDto.getStock() > product.getInventory().getStock()) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }

        BigDecimal originalPrice = product.getPrice();
        BigDecimal discountRate = requestDto.getDiscountRate();

        // 행사상품 스냅샷 생성 및 저장
        FlashSaleItem flashSaleItem = FlashSaleItem.builder()
                .flashSale(flashSale)
                .product(product)
                .originalPrice(originalPrice)
                .discountRate(discountRate)
                .discountedPrice(originalPrice.subtract(originalPrice.multiply(discountRate)))
                .initialStock(requestDto.getStock())
                .status(FlashSaleItemStatus.AVAILABLE)
                .build();
        flashSaleItemRepository.save(flashSaleItem);
    }

    //행사 상품 삭제
    @Transactional
    public void deleteFlashSaleItem(Long flashSaleId, Long productId, User user) {
        // 관리자 권한 확인
        checkAdminRole(user);
        // 행사 조회
        FlashSale flashSale = findFlashSale(flashSaleId);
        // 행사 시작 전에만 상품 삭제 가능
        if (flashSale.getStatus() != FlashSaleStatus.SCHEDULED) {
            throw new CustomException(ErrorCode.FLASH_SALE_NOT_SCHEDULED);
        }
        // 행사 상품 조회
        FlashSaleItem flashSaleItem = flashSaleItemRepository.findByIdAndFlashSaleId(productId, flashSaleId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        // 상품 삭제
        flashSaleItemRepository.delete(flashSaleItem);
    }

    //행사 시작 (관리자 수동 제어용)
    @Transactional
    public void openFlashSale(Long flashSaleId, User user) {
        // 관리자 권한 확인
        checkAdminRole(user);
        // DB 행사 상태 변경
        updateFlashSaleStatus(flashSaleId, FlashSaleStatus.ACTIVE);
        log.info("Flash sale manually opened by admin: {}", flashSaleId);
    }

    //행사 종료 (관리자 수동 제어용)
    @Transactional
    public void closeFlashSale(Long flashSaleId, User user) {
        // 관리자 권한 확인
        checkAdminRole(user);
        // DB 행사 상태 변경
        updateFlashSaleStatus(flashSaleId, FlashSaleStatus.ENDED);
        log.info("Flash sale manually closed by admin: {}", flashSaleId);
    }

    /// -------------------------------------------- private method -------------------------------------------- ///

    // 행사 조회용 - 행사정보만 필요할때 사용
    private FlashSale findFlashSale(Long flashSaleId) {
        return flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new CustomException(ErrorCode.FLASH_SALE_NOT_FOUND));
    }

    // 행사 조회용 - 행사정보+상품정보가 둘다 필요할때 사용
    private FlashSale findFlashSaleWithProduct(Long flashSaleId) {
        return flashSaleRepository.findByIdWithProducts(flashSaleId)
                .orElseThrow(() -> new CustomException(ErrorCode.FLASH_SALE_NOT_FOUND));
    }

    // 상품 조회용 - 재고정보까지 필요할때 사용
    private Product findByIdWithInventory(Long productId) {
        return productRepository.findByIdWithInventory(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // 관리자 권한 확인
    private void checkAdminRole(User user) {
        if (user == null && !user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }


    // DB 행사 상태 업데이트
    private void updateFlashSaleStatus(Long flashSaleId, FlashSaleStatus status) {
        // 행사 상품 조회
        FlashSale flashSale = findFlashSale(flashSaleId);
        // 상태 변경
        flashSale.updateStatus(status);
        flashSaleRepository.save(flashSale);
    }
}
