package com.example.ec.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        jdbcTemplate.update("DELETE FROM products");
        insertProduct(1L, "ワイヤレスイヤホン", 5980, 10, "ON_SALE");
        insertProduct(2L, "ゲーミングマウス", 3980, 5, "ON_SALE");
        insertProduct(3L, "USB-Cハブ", 2980, 0, "ON_SALE");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (1, 2)");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (1, 1)");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (2, 4)");
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

    /**
     * カート内商品一覧を数量集計付きで取得できることを検証します。
     */
    @Test
    void findCartItemsReturnsAggregatedItems() {
        List<CartItemEntity> cartItems = cartRepository.findCartItems();

        assertEquals(2, cartItems.size());
        assertEquals(1L, cartItems.get(0).getProductId());
        assertEquals("ワイヤレスイヤホン", cartItems.get(0).getName());
        assertEquals(5980, cartItems.get(0).getPrice());
        assertEquals(3, cartItems.get(0).getQuantity());
        assertEquals(2L, cartItems.get(1).getProductId());
        assertEquals("ゲーミングマウス", cartItems.get(1).getName());
        assertEquals(3980, cartItems.get(1).getPrice());
        assertEquals(4, cartItems.get(1).getQuantity());
    }

    /**
     * カートが空の場合は空リストを返すことを検証します。
     */
    @Test
    void findCartItemsReturnsEmptyListWhenCartIsEmpty() {
        jdbcTemplate.update("DELETE FROM cart_item");

        List<CartItemEntity> cartItems = cartRepository.findCartItems();

        assertTrue(cartItems.isEmpty());
    }

    /**
     * 最小数量1を保存できることを検証します。
     */
    @Test
    void insertCartItemStoresMinimumQuantity() {
        jdbcTemplate.update("DELETE FROM cart_item");

        cartRepository.insertCartItem(1L, 1);

        Integer quantity = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM cart_item WHERE product_id = ?",
                Integer.class,
                1L);
        assertEquals(1, quantity);
    }

    /**
     * 最大数量99を保存できることを検証します。
     */
    @Test
    void insertCartItemStoresMaximumQuantity() {
        jdbcTemplate.update("DELETE FROM cart_item");

        cartRepository.insertCartItem(1L, 99);

        Integer quantity = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM cart_item WHERE product_id = ?",
                Integer.class,
                1L);
        assertEquals(99, quantity);
    }

    /**
     * 指定商品の数量合計を取得できることを検証します。
     */
    @Test
    void sumCartItemQuantityByProductIdReturnsAccumulatedQuantity() {
        int quantity = cartRepository.sumCartItemQuantityByProductId(1L);

        assertEquals(3, quantity);
    }

    /**
     * 該当商品のカート数量がない場合は0を返すことを検証します。
     */
    @Test
    void sumCartItemQuantityByProductIdReturnsZeroWhenItemDoesNotExist() {
        int quantity = cartRepository.sumCartItemQuantityByProductId(999L);

        assertEquals(0, quantity);
    }

    /**
     * 指定商品のみ削除できることを検証します。
     */
    @Test
    void deleteCartItemsByProductIdRemovesOnlyTargetProduct() {
        cartRepository.deleteCartItemsByProductId(1L);

        Integer deletedQuantity = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM cart_item WHERE product_id = ?",
                Integer.class,
                1L);
        Integer remainingQuantity = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM cart_item WHERE product_id = ?",
                Integer.class,
                2L);

        assertEquals(0, deletedQuantity);
        assertEquals(4, remainingQuantity);
    }

    /**
     * カート内商品をすべて削除できることを検証します。
     */
    @Test
    void deleteAllCartItemsRemovesAllItems() {
        cartRepository.deleteAllCartItems();

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cart_item", Integer.class);

        assertEquals(0, count);
    }
}