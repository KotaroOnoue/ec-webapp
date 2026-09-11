package com.example.ec.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.ec.repository.entity.CartItemEntity;
import com.example.ec.repository.entity.OrderEntity;

/**
 * 注文確認画面に関わるRepositoryのデータアクセスを検証するテストです。
 */
@SpringBootTest
class OrderConfirmRepositoryTest {

    /** 注文Repositoryです。 */
    @Autowired
    private OrderRepository orderRepository;

    /** カートRepositoryです。 */
    @Autowired
    private CartRepository cartRepository;

    /** テスト用JDBC操作です。 */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * テストごとに商品、カート、注文データを初期化します。
     */
    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM cart_item");
        jdbcTemplate.update("DELETE FROM products");

        insertProduct(1L, "ワイヤレスイヤホン", 5980, 10, "ON_SALE");
        insertProduct(2L, "ゲーミングマウス", 3980, 5, "ON_SALE");

        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (1, 2)");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (2, 1)");
    }

    /**
     * 注文確認画面表示用のカート内容を取得できることを検証します。
     */
    @Test
    void findCartItemsReturnsCartItemsForOrderConfirm() {
        List<CartItemEntity> cartItems = cartRepository.findCartItems();

        assertEquals(2, cartItems.size());
        assertEquals(1L, cartItems.get(0).getProductId());
        assertEquals("ワイヤレスイヤホン", cartItems.get(0).getName());
        assertEquals(5980, cartItems.get(0).getPrice());
        assertEquals(2, cartItems.get(0).getQuantity());
        assertEquals(2L, cartItems.get(1).getProductId());
        assertEquals("ゲーミングマウス", cartItems.get(1).getName());
        assertEquals(3980, cartItems.get(1).getPrice());
        assertEquals(1, cartItems.get(1).getQuantity());
    }

    /**
     * 注文データがない場合の次注文IDが1であることを検証します。
     */
    @Test
    void findNextOrderIdReturnsOneWhenOrdersAreEmpty() {
        jdbcTemplate.update("DELETE FROM orders");

        Long nextOrderId = orderRepository.findNextOrderId();

        assertEquals(1L, nextOrderId);
    }

    /**
     * 既存注文の最大値に対して次注文IDが採番されることを検証します。
     */
    @Test
    void findNextOrderIdReturnsNextValueWhenOrdersExist() {
        orderRepository.insertOrder(5L, "山田 太郎", "1500001", "東京都千代田区1-1-1", "0312345678", 15940, LocalDateTime.now());

        Long nextOrderId = orderRepository.findNextOrderId();

        assertEquals(6L, nextOrderId);
    }

    /**
     * 注文情報を保存できることを検証します。
     */
    @Test
    void insertOrderStoresOrderData() {
        orderRepository.insertOrder(1L, "山田 太郎", "1500001", "東京都千代田区1-1-1", "0312345678", 15940, LocalDateTime.now());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders WHERE order_id = 1", Integer.class);
        String customerName = jdbcTemplate.queryForObject("SELECT customer_name FROM orders WHERE order_id = 1", String.class);
        String postalCode = jdbcTemplate.queryForObject("SELECT postal_code FROM orders WHERE order_id = 1", String.class);
        String address = jdbcTemplate.queryForObject("SELECT adress FROM orders WHERE order_id = 1", String.class);
        String phoneNumber = jdbcTemplate.queryForObject("SELECT phone_number FROM orders WHERE order_id = 1", String.class);
        Integer totalAmount = jdbcTemplate.queryForObject("SELECT total_amount FROM orders WHERE order_id = 1", Integer.class);

        assertEquals(1, count);
        assertEquals("山田 太郎", customerName);
        assertEquals("1500001", postalCode);
        assertEquals("東京都千代田区1-1-1", address);
        assertEquals("0312345678", phoneNumber);
        assertEquals(15940, totalAmount);
    }

    /**
     * 注文IDを指定して注文情報を取得できることを検証します。
     */
    @Test
    void findByOrderIdReturnsOrder() {
        orderRepository.insertOrder(5L, "山田 花子", "1600001", "東京都新宿区1-2-3", "09012345678", 5000, LocalDateTime.now());

        OrderEntity order = orderRepository.findByOrderId(5L);

        assertNotNull(order);
        assertEquals(5L, order.getOrderId());
        assertEquals("山田 花子", order.getCustomerName());
        assertEquals("1600001", order.getPostalCode());
        assertEquals("東京都新宿区1-2-3", order.getAddress());
        assertEquals("09012345678", order.getPhoneNumber());
        assertEquals(5000, order.getTotalAmount());
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
     * カート内商品をすべて削除できることを検証します。
     */
    @Test
    void deleteAllCartItemsRemovesAllCartItems() {
        cartRepository.deleteAllCartItems();

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cart_item", Integer.class);

        assertEquals(0, count);
    }

    /**
     * カートが空の場合は空リストを返すことを検証します。
     */
    @Test
    void findCartItemsReturnsEmptyListWhenCartIsEmpty() {
        cartRepository.deleteAllCartItems();

        List<CartItemEntity> cartItems = cartRepository.findCartItems();

        assertTrue(cartItems.isEmpty());
    }

    /**
     * テスト用の商品データを登録します。
     *
     * @param productId 商品ID
     * @param name 商品名
     * @param price 価格
     * @param stock 在庫数
     * @param status 販売状態
     */
    private void insertProduct(Long productId, String name, Integer price, Integer stock, String status) {
        jdbcTemplate.update(
                "INSERT INTO products (product_id, name, description, price, stock, status, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)",
                productId,
                name,
                name + "の商品説明",
                price,
                stock,
                status,
                "/images/products/" + productId + ".png");
    }
}