package com.example.ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ec.repository.CartRepository;
import com.example.ec.repository.OrderRepository;
import com.example.ec.repository.entity.OrderEntity;
import com.example.ec.service.model.OrderCompleteModel;

/**
 * 注文完了画面に関わるOrderServiceの動作をモックRepositoryで検証する単体テストです。
 */
@ExtendWith(MockitoExtension.class)
class OrderCompleteServiceMockTest {

    /** 注文Repositoryのモックです。 */
    @Mock
    private OrderRepository orderRepository;

    /** カートRepositoryのモックです。 */
    @Mock
    private CartRepository cartRepository;

    /** カートServiceのモックです。 */
    @Mock
    private CartService cartService;

    /** テスト対象のServiceです。 */
    @InjectMocks
    private OrderService orderService;

    /**
     * 注文情報が存在する場合に注文完了画面用モデルを返すことを検証します。
     */
    @Test
    void getOrderCompleteReturnsModelWhenOrderExists() {
        when(orderRepository.findByOrderId(10L)).thenReturn(createOrderEntity(10L, "山田 太郎"));

        OrderCompleteModel order = orderService.getOrderComplete(10L);

        assertNotNull(order);
        assertEquals(10L, order.getOrderId());
        verify(orderRepository, times(1)).findByOrderId(10L);
    }

    /**
     * 注文番号がモデルへ変換されることを検証します。
     */
    @Test
    void getOrderCompleteMapsOrderIdToModel() {
        when(orderRepository.findByOrderId(10L)).thenReturn(createOrderEntity(10L, "山田 太郎"));

        OrderCompleteModel order = orderService.getOrderComplete(10L);

        assertEquals(10L, order.getOrderId());
    }

    /**
     * 最小境界の注文IDでも取得できることを検証します。
     */
    @Test
    void getOrderCompleteReturnsModelWhenOrderIdIsMinimumBoundary() {
        when(orderRepository.findByOrderId(1L)).thenReturn(createOrderEntity(1L, "山田 花子"));

        OrderCompleteModel order = orderService.getOrderComplete(1L);

        assertNotNull(order);
        assertEquals(1L, order.getOrderId());
        verify(orderRepository, times(1)).findByOrderId(1L);
    }

    /**
     * 存在しない注文IDではnullを返すことを検証します。
     */
    @Test
    void getOrderCompleteReturnsNullWhenOrderDoesNotExist() {
        when(orderRepository.findByOrderId(999L)).thenReturn(null);

        OrderCompleteModel order = orderService.getOrderComplete(999L);

        assertNull(order);
        verify(orderRepository, times(1)).findByOrderId(999L);
    }

    /**
     * テスト用の注文Entityを生成します。
     *
     * @param orderId 注文ID
     * @param customerName 顧客名
     * @return 注文Entity
     */
    private OrderEntity createOrderEntity(Long orderId, String customerName) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(orderId);
        orderEntity.setCustomerName(customerName);
        orderEntity.setPostalCode("1500001");
        orderEntity.setAddress("東京都千代田区1-1-1");
        orderEntity.setPhoneNumber("0312345678");
        orderEntity.setTotalAmount(15940);
        orderEntity.setOrderAt(LocalDateTime.of(2026, 9, 10, 12, 0, 0));
        return orderEntity;
    }
}