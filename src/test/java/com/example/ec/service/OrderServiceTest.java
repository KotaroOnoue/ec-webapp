package com.example.ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.ec.controller.form.OrderForm;
import com.example.ec.exception.InsufficientStockException;

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
        jdbcTemplate.update("UPDATE products SET stock = 10 WHERE product_id = 1");
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
        orderForm.setDiscountCode(1001L);

        Long orderId = orderService.placeOrder(orderForm);

        assertEquals(1L, orderId);
        Integer orderCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        Integer cartCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cart_item", Integer.class);
        Integer totalAmount = jdbcTemplate.queryForObject("SELECT total_amount FROM orders WHERE order_id = ?", Integer.class, 1L);
        Integer stock = jdbcTemplate.queryForObject("SELECT stock FROM products WHERE product_id = ?", Integer.class, 1L);
        assertEquals(1, orderCount);
        assertEquals(0, cartCount);
        assertEquals(11664, totalAmount);
        assertEquals(8, stock);
    }

    /**
     * 在庫不足時は注文を保存せずロールバックすることを検証します。
     */
    @Test
    void placeOrderThrowsWhenStockIsInsufficient() {
        jdbcTemplate.update("UPDATE products SET stock = 1 WHERE product_id = 1");

        OrderForm orderForm = new OrderForm();
        orderForm.setCustomerName("山田 太郎");
        orderForm.setPostalCode("1500001");
        orderForm.setAddress("東京都千代田区1-1-1");
        orderForm.setPhoneNumber("0312345678");

        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(orderForm));

        Integer orderCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        Integer cartCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cart_item", Integer.class);
        Integer stock = jdbcTemplate.queryForObject("SELECT stock FROM products WHERE product_id = ?", Integer.class, 1L);
        assertEquals(0, orderCount);
        assertEquals(1, cartCount);
        assertEquals(1, stock);
    }

    /**
     * 注文完了画面表示用に注文番号を取得できることを検証します。
     */
    @Test
    void getOrderCompleteReturnsOrderId() {
        jdbcTemplate.update(
                "INSERT INTO orders (order_id, customer_name, postal_code, adress, phone_number, total_amount, order_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                7L,
                "山田 太郎",
                "1500001",
                "東京都千代田区1-1-1",
                "0312345678",
                9800);

        var order = orderService.getOrderComplete(7L);

        assertEquals(7L, order.getOrderId());
    }
}