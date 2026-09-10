package com.example.ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.ec.controller.form.OrderForm;

/**
 * OrderServiceのビジネスロジックを検証するテストです。
 */
@SpringBootTest
class OrderServiceTest {

    /** 注文Serviceです。 */
    @Autowired
    private OrderService orderService;

    /** テスト用JDBC操作です。 */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * テストごとに初期化します。
     */
    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM cart_item");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (1, 2)");
    }

    /**
     * 注文確定時にordersへ保存し、カートを空にすることを検証します。
     */
    @Test
    void placeOrderStoresOrderAndClearsCart() {
        OrderForm orderForm = new OrderForm();
        orderForm.setCustomerName("山田 太郎");
        orderForm.setPostalCode("1500001");
        orderForm.setAddress("東京都千代田区1-1-1");
        orderForm.setPhoneNumber("0312345678");

        Long orderId = orderService.placeOrder(orderForm);

        assertEquals(1L, orderId);
        Integer orderCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        Integer cartCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cart_item", Integer.class);
        assertEquals(1, orderCount);
        assertEquals(0, cartCount);
    }
}