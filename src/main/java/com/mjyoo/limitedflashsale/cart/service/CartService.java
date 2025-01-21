package com.mjyoo.limitedflashsale.cart.service;

import com.mjyoo.limitedflashsale.cart.dto.CartRequestDto;
import com.mjyoo.limitedflashsale.cart.dto.CartProductResponseDto;
import com.mjyoo.limitedflashsale.cart.dto.CartListResponseDto;
import com.mjyoo.limitedflashsale.cart.entity.Cart;
import com.mjyoo.limitedflashsale.cart.entity.CartProduct;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.cart.repository.CartProductRepository;
import com.mjyoo.limitedflashsale.cart.repository.CartRepository;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import com.mjyoo.limitedflashsale.user.entity.User;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartProductRepository cartProductRepository;
    private final ProductRepository productRepository;

    // 사용자별 장바구니 목록 조회
    public CartListResponseDto getCartList(User user) {
        // 사용자의 장바구니 조회
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

        // 장바구니에 담긴 상품 정보를 담을 리스트 생성
        List<CartProductResponseDto> cartInfoList = new ArrayList<>();
        // 해당 장바구니에 담긴 모든 상품들을 조회
        List<CartProduct> cartProductList = cart.getCartProductList();

        // 장바구니에 담긴 모든 상품들을 하나씩 순회
        for (CartProduct cartProduct : cartProductList) {
            CartProductResponseDto cartProductResponseDto = new CartProductResponseDto(cartProduct);
            cartInfoList.add(cartProductResponseDto);
        }

        return CartListResponseDto.builder()
                .cartProductList(cartInfoList)
                .totalCartProducts((long) cartInfoList.size())
                .totalAmount(cart.getTotalAmount())
                .build();
    }

    // 상품을 장바구니에 추가
    @Transactional
    public CartProductResponseDto addToCart(CartRequestDto requestDto, User user) {
        // 유저 장바구니 조회
        Optional<Cart> cartOptional = cartRepository.findByUserId(user.getId());

        Cart cart;
        if (cartOptional.isPresent()) {
            cart = cartOptional.get();
        } else { // 장바구니가 없으면 새로 생성
            cart = new Cart(user);
            cartRepository.save(cart);
        }
        // 삭제되지 않은 상품만 조회
        Product product = getValidProduct(requestDto.getProductId());

        // 상품 재고 확인
        int stock = product.getInventory().getStock();
        if (stock < requestDto.getQuantity()) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }

        // 장바구니에 상품이 있는지 조회
        CartProduct cartProduct = cartProductRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        // 상품이 장바구니에 없으면 추가
        if (cartProduct == null) {
            cartProduct = CartProduct.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(requestDto.getQuantity())
                    .build();
            cartProductRepository.save(cartProduct);
        } else {
            // 상품이 장바구니에 있으면 수량만 업데이트
            cartProduct.updateQuantity(requestDto.getQuantity());
        }
        return new CartProductResponseDto(cartProduct);
    }

    // 장바구니 상품 수량 업데이트
    @Transactional
    public void updateCart(Long productId, int quantity, User user) {
        // 유저 장바구니 조회
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

        // 상품 조회
        Product product = getValidProduct(productId);

        // 장바구니 상품 조회
        CartProduct cartProduct = cartProductRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_PRODUCT_NOT_FOUND));

        // 수량/재고 유효한지 확인
        if (quantity <= 0 || quantity > product.getInventory().getStock()) {
            throw new CustomException(ErrorCode.INVALID_QUANTITY);
        }

        // 장바구니 상품 수량 업데이트
        cartProduct.setQuantity(quantity);
    }

    // 장바구니에서 상품 삭제
    public void deleteFromCart(Long productId, User user) {
        // 유저 장바구니 조회
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

        // 상품 조회
        Product product = getValidProduct(productId);

        // 장바구니 상품 조회
        CartProduct cartProduct = cartProductRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_PRODUCT_NOT_FOUND));

        // 장바구니 상품 삭제
        cartProductRepository.delete(cartProduct);
    }

    @NotNull
    private Product getValidProduct(Long productId) {
        return productRepository.findById(productId)
                .filter(product -> !product.isDeleted()) //삭제되지 않은 상품만 필터링
                // 삭제된 상품이면 예외 발생
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
