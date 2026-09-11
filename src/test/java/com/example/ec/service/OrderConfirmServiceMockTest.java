package com.example.ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ec.controller.form.OrderForm;
import com.example.ec.repository.CartRepository;
import com.example.ec.repository.OrderRepository;
import com.example.ec.repository.entity.OrderEntity;
import com.example.ec.service.model.OrderCompleteModel;

/**
 * 注文確認画面に関わるOrderServiceの動作をモックRepositoryで検証する単体テストです。
 */
@ExtendWith(MockitoExtension.class)
class OrderConfirmServiceMockTest {

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
     * 正常時に注文を保存し採番した注文IDを返すことを検証します。
     */
    @Test
    void placeOrderStoresOrderAndReturnsOrderId() {
        OrderForm orderForm = createOrderForm();
        when(cartService.getTotalAmount()).thenReturn(15940);
        when(orderRepository.findNextOrderId()).thenReturn(5L);

        Long orderId = orderService.placeOrder(orderForm);

        assertEquals(5L, orderId);
        verify(orderRepository, times(1)).insertOrder(
            eq(5L),
            eq("山田 太郎"),
            eq("1500001"),
            eq("東京都千代田区1-1-1"),
            eq("0312345678"),
            eq(15940),
            any());
        verify(cartRepository, times(1)).deleteAllCartItems();
    }

    /**
     * 空カート時は例外を送出し保存処理を行わないことを検証します。
     */
    @Test
    void placeOrderThrowsWhenCartIsEmpty() {
        OrderForm orderForm = createOrderForm();
        when(cartService.getTotalAmount()).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(orderForm));

        verify(orderRepository, never()).findNextOrderId();
        verify(orderRepository, never()).insertOrder(any(), any(), any(), any(), any(), any(), any());
        verify(cartRepository, never()).deleteAllCartItems();
    }

    /**
     * 注文完了画面表示用に注文番号を取得できることを検証します。
     */
    @Test
    void getOrderCompleteReturnsOrderId() {
        when(orderRepository.findByOrderId(7L)).thenReturn(createOrderEntity(7L, "山田 太郎", "1500001", "東京都千代田区1-1-1", "0312345678", 9800));

        OrderCompleteModel order = orderService.getOrderComplete(7L);

        assertEquals(7L, order.getOrderId());
    }

    /**
     * 存在しない注文IDではnullを返すことを検証します。
     */
    @Test
    void getOrderCompleteReturnsNullWhenOrderDoesNotExist() {
        when(orderRepository.findByOrderId(999L)).thenReturn(null);

        OrderCompleteModel order = orderService.getOrderComplete(999L);

        assertNull(order);
    }

    /**
     * テスト用の注文フォームを生成します。
     *
     * @return 注文フォーム
     */
    private OrderForm createOrderForm() {
        OrderForm orderForm = new OrderForm();
        orderForm.setCustomerName("山田 太郎");
        orderForm.setPostalCode("1500001");
        orderForm.setAddress("東京都千代田区1-1-1");
        orderForm.setPhoneNumber("0312345678");
        return orderForm;
    }

    /**
     * テスト用の注文Entityを生成します。
     *
     * @param orderId 注文ID
     * @param customerName 顧客名
     * @param postalCode 郵便番号
     * @param address 住所
     * @param phoneNumber 電話番号
     * @param totalAmount 合計金額
     * @return 注文Entity
     */
    private OrderEntity createOrderEntity(Long orderId, String customerName, String postalCode, String address,
            String phoneNumber, Integer totalAmount) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(orderId);
        orderEntity.setCustomerName(customerName);
        orderEntity.setPostalCode(postalCode);
        orderEntity.setAddress(address);
        orderEntity.setPhoneNumber(phoneNumber);
        orderEntity.setTotalAmount(totalAmount);
        return orderEntity;
    }
}