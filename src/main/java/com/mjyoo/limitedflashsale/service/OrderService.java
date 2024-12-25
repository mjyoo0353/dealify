package com.mjyoo.limitedflashsale.service;

import com.mjyoo.limitedflashsale.dto.requestDto.OrderRequestDto;
import com.mjyoo.limitedflashsale.dto.responseDto.OrderProductResponseDto;
import com.mjyoo.limitedflashsale.dto.responseDto.OrderResponseDto;
import com.mjyoo.limitedflashsale.entity.*;
import com.mjyoo.limitedflashsale.repository.*;
import com.mjyoo.limitedflashsale.security.UserDetailsImpl;
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

    //주문 생성 (단일 상품)
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, UserDetailsImpl userDetails) {
        User user = getUserByEmail(userDetails.getEmail()); // 사용자 조회
        Product product = getProductById(requestDto); // 상품 조회

        //재고 확인
        if (product.getInventory().getStock() < requestDto.getQuantity()) {
            throw new IllegalArgumentException("Out of stock.");
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

    private Product getProductById(OrderRequestDto requestDto) {
        return productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }


}
