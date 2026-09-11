package com.example.ec.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.ec.repository.entity.OrderEntity;

/**
 * 注文完了画面に関わるRepositoryのデータアクセスを検証するテストです。
 */
@SpringBootTest
class OrderCompleteRepositoryTest {

    /** 注文Repositoryです。 */
    @Autowired
    private OrderRepository orderRepository;

    /** テスト用JDBC操作です。 */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * テストごとにordersを初期化します。
     */
    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM orders");
    }

    /**
     * 注文IDを指定して注文情報を取得できることを検証します。
     */
    @Test
    void findByOrderIdReturnsOrderForOrderComplete() {
        LocalDateTime orderAt = LocalDateTime.of(2026, 9, 10, 12, 34, 56);
        insertOrder(10L, "山田 太郎", "1500001", "東京都千代田区1-1-1", "0312345678", 15940, orderAt);

        OrderEntity order = orderRepository.findByOrderId(10L);

        assertNotNull(order);
        assertEquals(10L, order.getOrderId());
    }

    /**
     * orders 定義に従った各項目を取得できることを検証します。
     */
    @Test
    void findByOrderIdReturnsAllOrderFields() {
        LocalDateTime orderAt = LocalDateTime.of(2026, 9, 10, 8, 15, 30);
        insertOrder(10L, "山田 太郎", "1500001", "東京都千代田区1-1-1", "0312345678", 15940, orderAt);

        OrderEntity order = orderRepository.findByOrderId(10L);

        assertNotNull(order);
        assertEquals(10L, order.getOrderId());
        assertEquals("山田 太郎", order.getCustomerName());
        assertEquals("1500001", order.getPostalCode());
        assertEquals("東京都千代田区1-1-1", order.getAddress());
        assertEquals("0312345678", order.getPhoneNumber());
        assertEquals(15940, order.getTotalAmount());
        assertEquals(orderAt, order.getOrderAt());
    }

    /**
     * adress 列が address フィールドへ正しくマッピングされることを検証します。
     */
    @Test
    void findByOrderIdMapsAdressColumnToAddressField() {
        insertOrder(10L, "山田 太郎", "1500001", "東京都千代田区1-1-1", "0312345678", 15940,
                LocalDateTime.of(2026, 9, 10, 9, 0, 0));

        OrderEntity order = orderRepository.findByOrderId(10L);

        assertNotNull(order);
        assertEquals("東京都千代田区1-1-1", order.getAddress());
    }

    /**
     * 存在しない注文IDではnullが返ることを検証します。
     */
    @Test
    void findByOrderIdReturnsNullWhenOrderDoesNotExist() {
        OrderEntity order = orderRepository.findByOrderId(999L);

        assertNull(order);
    }

    /**
     * 最小境界の注文IDでも取得できることを検証します。
     */
    @Test
    void findByOrderIdReturnsOrderWhenOrderIdIsMinimumBoundary() {
        insertOrder(1L, "山田 花子", "1600001", "東京都新宿区1-2-3", "09012345678", 5000,
                LocalDateTime.of(2026, 9, 10, 10, 20, 30));

        OrderEntity order = orderRepository.findByOrderId(1L);

        assertNotNull(order);
        assertEquals(1L, order.getOrderId());
        assertEquals("山田 花子", order.getCustomerName());
    }

    /**
     * テスト用の注文データを登録します。
     *
     * @param orderId 注文ID
     * @param customerName 顧客名
     * @param postalCode 郵便番号
     * @param address 住所
     * @param phoneNumber 電話番号
     * @param totalAmount 合計金額
     * @param orderAt 注文日時
     */
    private void insertOrder(Long orderId, String customerName, String postalCode, String address,
            String phoneNumber, Integer totalAmount, LocalDateTime orderAt) {
        jdbcTemplate.update(
                "INSERT INTO orders (order_id, customer_name, postal_code, adress, phone_number, total_amount, order_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                orderId,
                customerName,
                postalCode,
                address,
                phoneNumber,
                totalAmount,
                Timestamp.valueOf(orderAt));
    }
}