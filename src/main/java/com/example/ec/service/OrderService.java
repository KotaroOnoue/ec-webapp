package com.example.ec.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ec.controller.form.OrderForm;
import com.example.ec.repository.CartRepository;
import com.example.ec.repository.OrderRepository;

/**
 * 注文に関するビジネスロジックを扱うServiceです。
 */
@Service
public class OrderService {

    /** 注文Repositoryです。 */
    @Autowired
    private OrderRepository orderRepository;

    /** カートRepositoryです。 */
    @Autowired
    private CartRepository cartRepository;

    /** カートServiceです。 */
    @Autowired
    private CartService cartService;

    /**
     * 注文を確定して保存します。
     *
     * @param orderForm 注文フォーム
     * @return 保存した注文ID
     */
    public Long placeOrder(OrderForm orderForm) {
        int totalAmount = cartService.getTotalAmount();
        if (totalAmount <= 0) {
            throw new IllegalArgumentException("カートに商品がありません。");
        }

        Long orderId = orderRepository.findNextOrderId();
        orderRepository.insertOrder(
                orderId,
                orderForm.getCustomerName(),
                orderForm.getPostalCode(),
                orderForm.getAddress(),
                orderForm.getPhoneNumber(),
                totalAmount,
                LocalDateTime.now());
        cartRepository.deleteAllCartItems();
        return orderId;
    }
}