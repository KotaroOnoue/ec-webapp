package com.example.ec.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ec.controller.form.OrderForm;
import com.example.ec.repository.CartRepository;
import com.example.ec.repository.OrderRepository;
import com.example.ec.repository.entity.OrderEntity;
import com.example.ec.service.model.OrderCompleteModel;

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

    /**
     * 注文完了画面表示用の注文情報を取得します。
     *
     * @param orderId 注文ID
     * @return 注文完了画面表示用モデル。存在しない場合はnull
     */
    public OrderCompleteModel getOrderComplete(Long orderId) {
        OrderEntity orderEntity = orderRepository.findByOrderId(orderId);
        if (orderEntity == null) {
            return null;
        }

        OrderCompleteModel orderCompleteModel = new OrderCompleteModel();
        orderCompleteModel.setOrderId(orderEntity.getOrderId());
        return orderCompleteModel;
    }
}