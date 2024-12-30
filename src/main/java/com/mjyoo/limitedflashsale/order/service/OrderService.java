package com.mjyoo.limitedflashsale.order.service;

import com.mjyoo.limitedflashsale.cart.dto.CartRequestDto;
import com.mjyoo.limitedflashsale.cart.entity.Cart;
import com.mjyoo.limitedflashsale.cart.entity.CartProduct;
import com.mjyoo.limitedflashsale.cart.repository.CartRepository;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.order.dto.OrderRequestDto;
import com.mjyoo.limitedflashsale.order.dto.OrderListResponseDto;
import com.mjyoo.limitedflashsale.order.dto.OrderResponseDto;
import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderProduct;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.order.repository.OrderProductRepository;
import com.mjyoo.limitedflashsale.order.repository.OrderRepository;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import com.mjyoo.limitedflashsale.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    private final CartRepository cartRepository;

    //주문 내역 상세 조회
    public OrderResponseDto getOrder(Long orderId, User user) {
        Order order = getOrderById(orderId);
        ValidateOrderUser(user, order);
        return new OrderResponseDto(order);
    }

    //주문 리스트 조회
    public OrderListResponseDto getOrderList(User user) {
        //사용자별 주문 조회
        List<Order> orderList = orderRepository.findByUserId(user.getId());

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

        long totalOrderCount = (long) orderList.size();

        return new OrderListResponseDto(orderInfoList, totalOrderCount);
    }

    //주문 생성 (단일 상품)
    @Transactional
    public Long createOrder(OrderRequestDto requestDto, User user) {
        //상품 조회 및 재고 확인
        int quantity = requestDto.getQuantity();
        Product product = getValidProduct(requestDto.getProductId(), quantity);
        //재고 업데이트 (감소)
        product.getInventory().decreaseStock(quantity);

        //주문 생성
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.ORDERED)
                .build();
        orderRepository.save(order);

        //주문 상품 생성
        OrderProduct orderProduct = OrderProduct.builder()
                .order(order)
                .product(product)
                .name(product.getName())
                .price(product.getPrice())
                .quantity(quantity)
                .totalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .build();
        orderProductRepository.save(orderProduct);

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
                .status(OrderStatus.ORDERED)
                .orderProductList(new ArrayList<>())
                .build();
        orderRepository.save(order);

        //TODO : 장바구니에 담긴 상품 수량이 2이고 주문은 1개만 한다고 하는경우?

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
            if(cartProduct.getQuantity() < quantity) {
                throw new CustomException(ErrorCode.INVALID_QUANTITY);
            }

            //상품 조회
            Product product = productRepository.findById(productId)
                    .filter(prod -> !prod.isDeleted())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            //재고 확인
            if (product.getInventory().getStock() < quantity) {
                throw new CustomException(ErrorCode.OUT_OF_STOCK);
            }

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

        //변경된 장바구니 저장
        cartRepository.save(cart);

        return order.getId();
    }

    //주문 취소
    @Transactional
    public void cancelOrder(Long orderId, User user) {
        //주문 조회
        Order order = getOrderById(orderId);

        // 주문 취소 권한 확인
        ValidateOrderUser(user, order);

        // 주문 상태 변경
        //order.setStatus(OrderStatus.CANCELED);
        orderRepository.updateOrderStatusToCanceled(orderId, OrderStatus.CANCELED);

        //취소된 주문에 대한 모든 상품 처리
        for (OrderProduct orderProduct : order.getOrderProductList()) {
            Product product = orderProduct.getProduct();
            //취소된 상품 수량 재고에 추가
            product.getInventory().setStock(product.getInventory().getStock() + orderProduct.getQuantity());
        }
        orderRepository.save(order);
    }

    private Product getValidProduct(Long productId, int quantity) {
        //삭제되지 않은 상품 조회
        Product product = productRepository.findById(productId)
                .filter(product1 -> !product1.isDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        //재고 확인
        if (product.getInventory().getStock() < quantity) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }
        return product;
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
