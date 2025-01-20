package com.mjyoo.limitedflashsale.order.service;

import com.mjyoo.limitedflashsale.cart.dto.CartRequestDto;
import com.mjyoo.limitedflashsale.cart.entity.Cart;
import com.mjyoo.limitedflashsale.cart.entity.CartProduct;
import com.mjyoo.limitedflashsale.cart.repository.CartRepository;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleProduct;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleProductStatus;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleStatus;
import com.mjyoo.limitedflashsale.flashsale.repository.FlashSaleProductRepository;
import com.mjyoo.limitedflashsale.flashsale.repository.FlashSaleRepository;
import com.mjyoo.limitedflashsale.order.dto.OrderRequestDto;
import com.mjyoo.limitedflashsale.order.dto.OrderListResponseDto;
import com.mjyoo.limitedflashsale.order.dto.OrderResponseDto;
import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderProduct;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import com.mjyoo.limitedflashsale.payment.entity.PaymentStatus;
import com.mjyoo.limitedflashsale.payment.service.PaymentService;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.order.repository.OrderProductRepository;
import com.mjyoo.limitedflashsale.order.repository.OrderRepository;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import com.mjyoo.limitedflashsale.product.service.InventoryService;
import com.mjyoo.limitedflashsale.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    private final CartRepository cartRepository;
    private final FlashSaleProductRepository flashSaleProductRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final RedisTemplate<String, Object> redisTemplate;

    //주문 내역 상세 조회
    public OrderResponseDto getOrder(Long orderId, User user) {
        Order order = getOrderById(orderId);
        ValidateOrderUser(user, order);
        return new OrderResponseDto(order);
    }

    //주문 리스트 조회
    public OrderListResponseDto getOrderList(User user, Long cursor, int size) {
        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Order> orderList = getOrdersByCursor(user, cursor, pageRequest);

        //사용자가 주문한 상품 정보를 담을 리스트 생성
        List<OrderResponseDto> orderInfoList = new ArrayList<>();
        //주문한 목록을 순회하고
        for (Order order : orderList) {
            //주문 정보를 OrderResponseDto로 변환하여 리스트에 추가
            OrderResponseDto orderResponseDto = new OrderResponseDto(order);
            orderInfoList.add(orderResponseDto);
        }
        /*List<OrderResponseDto> orderInfoList = orderList.stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());*/

        long totalOrderCount = orderRepository.countAllByUserId(user.getId());

        Long nextCursor = orderList.hasNext() ? orderInfoList.get(orderInfoList.size() - 1).getId() : null;
        return new OrderListResponseDto(orderInfoList, totalOrderCount, nextCursor);
    }

    private Slice<Order> getOrdersByCursor(User user, Long cursor, PageRequest pageRequest) {
        Slice<Order> orderList;
        if (cursor == null || cursor == 0) {
            orderList = orderRepository.findByUserId(user.getId(), pageRequest);
        } else {
            orderList = orderRepository.findByUserIdAndIdLessThan(user.getId(), cursor, pageRequest);
        }
        return orderList;
    }

    //주문 생성 (단일 상품)
    @Transactional
    public Long createOrder(OrderRequestDto requestDto, User user) {
        // 상품 조회
        Product product = productRepository.findById(requestDto.getProductId())
                .filter(prod -> !prod.isDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        int quantity = requestDto.getQuantity();

        // 재고 확인
        if (product.getInventory().getStock() < quantity) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }

        // 재고 차감 처리 (DB 업데이트 + 레디스 캐시 업데이트)
        inventoryService.decreaseStock(product.getId(), quantity);

        //해당 상품이 행사 상품인지 확인
        FlashSaleProduct flashSaleProduct = flashSaleProductRepository.findByProductId(product.getId())
                .orElse(null);

        BigDecimal priceToApply, totalPriceToApply;
        boolean isEventProduct = false;

        //행사 상품인 경우
        if (flashSaleProduct != null) {

            // 행사 조회
            FlashSale flashSale = flashSaleProduct.getFlashSale();

            // 행사 시작 시간이 현재 시간보다 미래인지 확인하고 미래라면 예외 발생
            if (LocalDateTime.now().isBefore(flashSale.getStartTime())) {
                throw new CustomException(ErrorCode.FLASH_SALE_NOT_STARTED);
            }

            // 행사 상품이 ACTIVE 상태인지 확인
            if (!FlashSaleStatus.ACTIVE.equals(flashSale.getStatus())) {
                throw new CustomException(ErrorCode.FLASH_SALE_NOT_ACTIVE);
            }

            //행사상품이면 할인된 가격 적용
            priceToApply = flashSaleProduct.getDiscountedPrice();
            totalPriceToApply = priceToApply.multiply(BigDecimal.valueOf(quantity));
            isEventProduct = true;

            //재고가 없으면 상태 변경
            if (product.getInventory().getStock() == 0) {
                flashSaleProduct.setStatus(FlashSaleProductStatus.OUT_OF_STOCK);
                flashSaleProductRepository.save(flashSaleProduct);

                flashSale.setStatus(FlashSaleStatus.ENDED);
                flashSaleRepository.save(flashSale);
            }

        } else {
            // 행사 상품이 아닌 경우 일반 가격 적용
            priceToApply = product.getPrice();
            totalPriceToApply = priceToApply.multiply(BigDecimal.valueOf(quantity));
        }

        //주문 생성
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.ORDER_PROCESSING)
                .expiryTime(LocalDateTime.now().plusMinutes(5)) // 5분 후 만료
                .orderProductList(new ArrayList<>())
                .build();
        orderRepository.save(order);

        //주문 상품 생성
        OrderProduct orderProduct = OrderProduct.builder()
                .order(order)
                .product(product)
                .name(product.getName())
                .price(priceToApply)
                .quantity(quantity)
                .totalAmount(totalPriceToApply)
                .isEventProduct(isEventProduct)
                .build();

        // 주문 상품 리스트에 단일 상품 추가
        order.getOrderProductList().add(orderProduct);
        orderProductRepository.save(orderProduct);

        // 레디스 주문 정보 저장 및 만료 TTL 설정 (5분)
        String orderTimeoutKey = "temp_order:" + order.getId();
        redisTemplate.opsForValue().set(orderTimeoutKey, order.getId(), 5, TimeUnit.MINUTES);

        return order.getId();
    }

    //주문 생성 (장바구니 상품)
    @Transactional
    public Long createOrderFromCart(List<CartRequestDto> cartRequestDtos, User user) {

        // 유저 장바구니 조회
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

        //주문 생성
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.ORDER_PROCESSING)
                .orderProductList(new ArrayList<>())
                .build();
        orderRepository.save(order);

        // 주문 상품 리스트 생성
        List<OrderProduct> orderProductList = new ArrayList<>();

        // 사용자가 요청한 주문 상품 리스트를 순회하며 주문 상품 생성
        for (CartRequestDto cartRequest : cartRequestDtos) {
            Long productId = cartRequest.getProductId();
            int quantity = cartRequest.getQuantity();

            //사용자의 장바구니에 해당 상품이 있는지 확인
            CartProduct cartProduct = cart.getCartProductList().stream()
                    .filter(cp -> cp.getProduct().getId().equals(productId))
                    .findAny()
                    .orElseThrow(() -> new CustomException(ErrorCode.CART_PRODUCT_NOT_FOUND));

            //장바구니 수량과 요청 수량이 일치하는지 검증
            if (cartProduct.getQuantity() < quantity) {
                throw new CustomException(ErrorCode.INVALID_QUANTITY);
            }

            //상품 조회 및 재고 확인
            Product product = getProduct(productId);

            //재고 업데이트 (감소)
            product.getInventory().decreaseStock(quantity);

            //주문 상품 생성
            OrderProduct orderProduct = OrderProduct.builder()
                    .order(order)
                    .product(product)
                    .name(product.getName())
                    .price(product.getPrice())
                    .quantity(quantity)
                    .totalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();
            //주문 상품 리스트에 추가
            orderProductList.add(orderProduct);

            //장바구니에서 주문한 상품만 제거
            cart.getCartProductList().remove(cartProduct);
        }

        //주문 상품 리스트에 주문 상품 추가
        order.getOrderProductList().addAll(orderProductList);
        orderProductRepository.saveAll(orderProductList);
        cartRepository.save(cart);

        return order.getId();
    }

    //주문 취소
    @Transactional
    public void cancelOrder(Long orderId, User user) {
        //주문 조회
        Order order = getOrderById(orderId);

        if (OrderStatus.CANCELED.equals(order.getStatus())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 주문 취소 권한 확인
        ValidateOrderUser(user, order);
        // 주문 상태 변경
        order.updateStatus(OrderStatus.CANCELED);
        // 결제 상태 변경
        order.getPayment().setStatus(PaymentStatus.CANCELED);

        //취소된 주문에 대한 모든 상품 처리
        for (OrderProduct orderProduct : order.getOrderProductList()) {
            Product product = orderProduct.getProduct();
            //취소된 상품 수량 재고에 추가
            product.getInventory().restoreStock(orderProduct.getQuantity());
        }
        orderRepository.save(order);
    }

    public Product getProduct(Long productId) {
        //삭제되지 않은 상품 조회
        return productRepository.findById(productId)
                .filter(prod -> !prod.isDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void ValidateOrderUser(User user, Order order) {
        if (!order.getUser().getEmail().equals(user.getEmail())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

}
