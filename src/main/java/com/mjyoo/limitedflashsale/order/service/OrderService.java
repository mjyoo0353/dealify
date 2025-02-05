package com.mjyoo.limitedflashsale.order.service;

import com.mjyoo.limitedflashsale.cart.dto.CartRequestDto;
import com.mjyoo.limitedflashsale.cart.entity.Cart;
import com.mjyoo.limitedflashsale.cart.entity.CartItem;
import com.mjyoo.limitedflashsale.cart.repository.CartRepository;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleItem;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleItemStatus;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleStatus;
import com.mjyoo.limitedflashsale.flashsale.repository.FlashSaleItemRepository;
import com.mjyoo.limitedflashsale.flashsale.repository.FlashSaleRepository;
import com.mjyoo.limitedflashsale.order.dto.OrderRequestDto;
import com.mjyoo.limitedflashsale.order.dto.OrderListResponseDto;
import com.mjyoo.limitedflashsale.order.dto.OrderResponseDto;
import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderItem;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import com.mjyoo.limitedflashsale.payment.entity.PaymentStatus;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.order.repository.OrderItemRepository;
import com.mjyoo.limitedflashsale.order.repository.OrderRepository;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import com.mjyoo.limitedflashsale.product.service.InventoryService;
import com.mjyoo.limitedflashsale.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final InventoryService inventoryService;

    //주문 내역 상세 조회
    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long orderId, User user) {
        Order order = findOrderById(orderId);
        findOrderByUser(user, order);
        return new OrderResponseDto(order);
    }

    //주문 리스트 조회
    @Transactional(readOnly = true)
    public OrderListResponseDto getOrderList(User user, Long cursor, int size) {
        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Order> orderList = findOrdersByCursor(user, cursor, pageRequest);

        //사용자가 주문한 상품 정보를 담을 리스트 생성
        List<OrderResponseDto> orderInfoList = new ArrayList<>();
        //주문한 목록을 순회하고
        for (Order order : orderList) {
            //주문 정보를 OrderResponseDto로 변환하여 리스트에 추가
            orderInfoList.add(new OrderResponseDto(order));
        }

        long totalOrderCount = orderRepository.countAllByUserId(user.getId());

        Long nextCursor = orderList.hasNext() && !orderInfoList.isEmpty() ? orderInfoList.get(orderInfoList.size() - 1).getId() : null;
        return new OrderListResponseDto(orderInfoList, totalOrderCount, nextCursor);
    }

    //주문 생성 (단일 상품)
    @Transactional
    public Long createOrder(OrderRequestDto requestDto, User user) {
        // 상품/재고 조회
        Product product = findActiveProductWithInventory(requestDto.getProductId());
        int quantity = requestDto.getQuantity();
        inventoryService.decreaseStock(product, quantity);

        BigDecimal priceToApply;
        boolean isFlashSaleItem = false;

        //해당 상품이 행사 상품인지 확인
        BigDecimal flashSalePrice = processFlashSaleItem(product);

        //행사상품이면 할인된 가격 적용
        if (flashSalePrice != null) {
            priceToApply = flashSalePrice;
            isFlashSaleItem = true;
        } else {
            //행사 상품이 아닌 경우 일반 가격 적용
            priceToApply = product.getPrice();
        }

        //주문 생성
        Order order = createOrderEntity(user);
        //주문 상품 생성
        OrderItem orderItem = createOrderProductEntity(order, product, priceToApply, quantity, isFlashSaleItem);
        // 주문 상품 리스트에 단일 상품 추가
        order.getOrderItemList().add(orderItem);
        orderItemRepository.save(orderItem);

        return order.getId();
    }

    //주문 생성 (장바구니 상품)
    @Transactional
    public Long createOrderFromCart(List<CartRequestDto> cartRequestDtos, User user) {
        // 유저 장바구니 조회
        Cart cart = findCartByUser(user);
        //주문 생성
        Order order = createOrderEntity(user);
        // 주문 상품 리스트 생성
        List<OrderItem> orderItemList = new ArrayList<>();

        // 사용자가 요청한 주문 상품 리스트를 순회하며 주문 상품 생성
        for (CartRequestDto cartRequest : cartRequestDtos) {
            Long productId = cartRequest.getProductId();
            int quantity = cartRequest.getQuantity();

            //사용자의 장바구니에 해당 상품이 있는지 확인
            CartItem cartItem = findProductFromCart(cart, productId);

            //장바구니 수량과 요청 수량이 일치하는지 검증
            if (cartItem.getQuantity() < quantity) {
                throw new CustomException(ErrorCode.INVALID_QUANTITY);
            }

            //상품 조회
            Product product = findActiveProductWithInventory(productId);
            //재고 차감 및 레디스 재고 업데이트
            inventoryService.decreaseStock(product, quantity);

            BigDecimal priceToApply;
            boolean isFlashSaleItem = false;

            //해당 상품이 행사 상품인지 확인
            BigDecimal flashSalePrice = processFlashSaleItem(product);

            //행사 상품인 경우 할인된 가격 적용
            if (flashSalePrice != null) {
                priceToApply = flashSalePrice;
                isFlashSaleItem = true;
            } else {
                //행사 상품이 아닌 경우 일반 가격 적용
                priceToApply = product.getPrice();
            }

            //주문 상품 생성
            OrderItem orderItem = createOrderProductEntity(order, product, priceToApply, quantity, isFlashSaleItem);
            //주문 상품 리스트에 추가
            orderItemList.add(orderItem);
            //장바구니에서 주문한 상품만 제거
            cart.getCartItemList().remove(cartItem);
        }

        //주문 상품 리스트에 주문 상품 추가
        order.getOrderItemList().addAll(orderItemList);
        orderItemRepository.saveAll(orderItemList);
        cartRepository.save(cart);

        return order.getId();
    }

    //주문 취소
    @Transactional
    public void cancelOrder(Long orderId, User user) {
        //주문 조회
        Order order = findOrderById(orderId);

        if (OrderStatus.CANCELED.equals(order.getStatus())) {
            throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
        }

        // 주문 취소 권한 확인
        findOrderByUser(user, order);
        // 주문/결제 상태 변경
        order.updateStatus(OrderStatus.CANCELED);
        order.getPayment().setStatus(PaymentStatus.CANCELED);

        //취소된 주문에 대한 모든 상품 처리
        inventoryService.restoreStock(order.getOrderItemList());
        orderRepository.save(order);
    }

/// -------------------------------------------- private method -------------------------------------------- ///

    // 삭제되지 않은 상품 조회
    private Product findActiveProductWithInventory(Long productId) {
        return productRepository.findByIdWithInventory(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    //주문 조회
    private Order findOrderById(Long orderId) {
        return orderRepository.findByIdWithCheck(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    //주문 상세 조회
    private void findOrderByUser(User user, Order order) {
        if (!order.getUser().getEmail().equals(user.getEmail())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    //주문 리스트 조회
    private Slice<Order> findOrdersByCursor(User user, Long cursor, PageRequest pageRequest) {
        return orderRepository.findByUserIdAndCursor(user.getId(), cursor, pageRequest);
    }

    //유저 장바구니 조회
    private Cart findCartByUser(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
    }

    //장바구니 상품 조회
    private CartItem findProductFromCart(Cart cart, Long productId) {
        return cart.getCartItemList().stream()
                .filter(cp -> cp.getProduct().getId().equals(productId))
                .findAny()
                .orElseThrow(() -> new CustomException(ErrorCode.CART_PRODUCT_NOT_FOUND));
    }

    //주문 Entity 생성
    private Order createOrderEntity(User user) {
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.ORDER_PROCESSING)
                .expiryTime(LocalDateTime.now().plusMinutes(5)) // 5분 후 만료
                .orderItemList(new ArrayList<>())
                .build();
        orderRepository.save(order);
        return order;
    }

    //주문 상품 Entity 생성
    private OrderItem createOrderProductEntity(Order order, Product product, BigDecimal price, int quantity, boolean isFlashSaleItem) {
        return OrderItem.builder()
                .order(order)
                .product(product)
                .name(product.getName())
                .price(price)
                .quantity(quantity)
                .totalAmount(price.multiply(BigDecimal.valueOf(quantity)))
                .isFlashSaleItem(isFlashSaleItem)
                .build();
    }

    //행사 상품 처리
    private BigDecimal processFlashSaleItem(Product product) {
        //해당 상품이 행사 상품인지 확인
        FlashSaleItem flashSaleItem = flashSaleItemRepository.findByProductIdAndFlashSaleStatus(product.getId(), FlashSaleStatus.ACTIVE)
                .orElse(null);

        //행사 상품인 경우
        if (flashSaleItem != null) {

            // 행사 조회
            FlashSale flashSale = flashSaleItem.getFlashSale();

            // 행사 시작 시간이 현재 시간보다 미래인지 확인하고 미래라면 예외 발생
            if (LocalDateTime.now().isBefore(flashSale.getStartTime())) {
                throw new CustomException(ErrorCode.FLASH_SALE_NOT_STARTED);
            }

            // 행사 상품이 ONGOING 상태인지 확인
            if (!FlashSaleStatus.ACTIVE.equals(flashSale.getStatus())) {
                throw new CustomException(ErrorCode.FLASH_SALE_NOT_ACTIVE);
            }

            //재고가 없으면 상태 변경
            if (product.getInventory().getStock() == 0) {
                flashSaleItem.setStatus(FlashSaleItemStatus.OUT_OF_STOCK);
                flashSaleItemRepository.save(flashSaleItem);

                flashSale.updateStatus(FlashSaleStatus.ENDED);
                flashSaleRepository.save(flashSale);
            }
            return flashSaleItem.getDiscountedPrice();
        }
        return null;
    }

}
