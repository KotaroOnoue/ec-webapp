package com.example.ec.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * OrderRepositoryのデータ保存を検証するテストです。
 */
@SpringBootTest
class OrderRepositoryTest {

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
     * 注文情報を保存できることを検証します。
     */
    @Test
    void insertOrderStoresOrder() {
        orderRepository.insertOrder(1L, "山田 太郎", "1500001", "東京都千代田区1-1-1", "0312345678", 1000, LocalDateTime.now());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders WHERE order_id = 1", Integer.class);
        assertEquals(1, count);
    }
}