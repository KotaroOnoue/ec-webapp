package com.example.ec.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.ec.repository.entity.CartItemEntity;

/**
 * CartRepositoryのデータ取得を検証するテストです。
 */
@SpringBootTest
class CartRepositoryTest {

    /** カートRepositoryです。 */
    @Autowired
    private CartRepository cartRepository;

    /** テスト用JDBC操作です。 */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * テストごとにcart_itemを初期化します。
     */
    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM cart_item");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (1, 2)");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (1, 1)");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (2, 4)");
    }

    /**
     * カート内商品一覧を数量集計付きで取得できることを検証します。
     */
    @Test
    void findCartItemsReturnsAggregatedItems() {
        List<CartItemEntity> cartItems = cartRepository.findCartItems();

        assertEquals(2, cartItems.size());
        assertEquals(1L, cartItems.get(0).getProductId());
        assertEquals(3, cartItems.get(0).getQuantity());
        assertEquals(2L, cartItems.get(1).getProductId());
        assertEquals(4, cartItems.get(1).getQuantity());
    }
}