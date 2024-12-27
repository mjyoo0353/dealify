package com.mjyoo.limitedflashsale.order.service;

import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.order.dto.OrderRequestDto;
import com.mjyoo.limitedflashsale.order.dto.OrderListResponseDto;
import com.mjyoo.limitedflashsale.order.dto.OrderProductResponseDto;
import com.mjyoo.limitedflashsale.order.dto.OrderResponseDto;
import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderProduct;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.order.repository.OrderProductRepository;
import com.mjyoo.limitedflashsale.order.repository.OrderRepository;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;

    //주문 내역 상세 조회
    public OrderResponseDto getOrder(Long orderId, UserDetailsImpl userDetails) {
        Order order = getOrderById(orderId);
        ValidateOrderUser(userDetails, order);
        return new OrderResponseDto(order);
    }

    //주문 리스트 조회
    public OrderListResponseDto getOrderList(UserDetailsImpl userDetails) {
        //사용자별 주문 조회
        List<Order> orderList = orderRepository.findByUserId(userDetails.getUser().getId());

        //주문 목록 변환
        List<OrderResponseDto> orderInfoList = orderList.stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());

        /*List<OrderResponseDto> orderInfoList = new ArrayList<>();
        for (Order order : orderList) {
            OrderResponseDto orderResponseDto = new OrderResponseDto(order);
            orderInfoList.add(orderResponseDto);
        }*/

        long totalOrderCount = (long) orderList.size();
        return new OrderListResponseDto(orderInfoList, totalOrderCount);
    }

    //주문 생성 (단일 상품)
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, UserDetailsImpl userDetails) {
        User user = getUserByEmail(userDetails.getEmail()); // 사용자 조회
        Product product = getProductById(requestDto); // 상품 조회

        //TODO 상품이 삭제되었는지 확인


        //재고 확인
        if (product.getInventory().getStock() < requestDto.getQuantity()) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }
        //재고 업데이트 (감소)
        product.getInventory().decreaseStock(requestDto.getQuantity());

        //주문 상품 생성
        OrderProduct orderProduct = OrderProduct.builder()
                .product(product)
                .name(product.getName())
                .price(product.getPrice())
                .quantity(requestDto.getQuantity())
                .totalAmount(product.getPrice().multiply(BigDecimal.valueOf(requestDto.getQuantity())))
                .build();

        // 주문 상품 리스트에 추가
        List<OrderProduct> orderProductList = new ArrayList<>();
        orderProductList.add(orderProduct);

        //주문 생성
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.ORDERED)
                .orderProductList(orderProductList)
                .build();

        //양방향 관계를 위해 orderProduct에 Order 객체 설정
        orderProduct.setOrder(order);

        orderRepository.save(order);
        orderProductRepository.save(orderProduct);

        return OrderResponseDto.builder()
                .id(order.getId())
                .status(order.getStatus())
                //orderProductList를 OrderProductResponseDto로 변환
                .orderProductList(order.getOrderProductList()
                        .stream()
                        .map(OrderProductResponseDto::new)
                        .collect(Collectors.toList()))
                .build();
    }

    //주문 취소
    @Transactional
    public void cancelOrder(Long orderId, UserDetailsImpl userDetails) {
        //주문 조회
        Order order = getOrderById(orderId);

        // 주문 취소 권한 확인
        ValidateOrderUser(userDetails, order);

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

    private void ValidateOrderUser(UserDetailsImpl userDetails, Order order) {
        if (!order.getUser().getEmail().equals(userDetails.getUser().getEmail())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Product getProductById(OrderRequestDto requestDto) {
        return productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

}
