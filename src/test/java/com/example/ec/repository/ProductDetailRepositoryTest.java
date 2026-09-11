package com.example.ec.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.ec.repository.entity.ProductEntity;

/**
 * 商品詳細画面に関わるRepositoryのデータアクセスを検証するテストです。
 */
@SpringBootTest
class ProductDetailRepositoryTest {

    /** 商品Repositoryです。 */
    @Autowired
    private ProductRepository productRepository;

    /** カートRepositoryです。 */
    @Autowired
    private CartRepository cartRepository;

    /** テスト用JDBC操作です。 */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * テストごとに商品とカートのテストデータを初期化します。
     */
    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM cart_item");
        jdbcTemplate.update("DELETE FROM products");

        insertProduct(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png");
        insertProduct(3L, "USB-Cハブ", "5in1モデル", 2980, 0, "ON_SALE", "/images/products/3.png");
        insertProduct(4L, "Webカメラ", "フルHD対応", 4980, 8, "STOPPED", "/images/products/4.png");
    }

    /**
     * 販売中かつ在庫ありの商品詳細を取得できることを検証します。
     */
    @Test
    void findByProductIdReturnsOnSaleProduct() {
        ProductEntity productEntity = productRepository.findByProductId(1L);

        assertNotNull(productEntity);
        assertEquals(1L, productEntity.getProductId());
        assertEquals("ワイヤレスイヤホン", productEntity.getName());
        assertEquals("ノイズキャンセリング対応", productEntity.getDescription());
        assertEquals(5980, productEntity.getPrice());
        assertEquals(10, productEntity.getStock());
        assertEquals("ON_SALE", productEntity.getStatus());
        assertEquals("/images/products/1.png", productEntity.getImageUrl());
    }

    /**
     * 在庫0の商品詳細を取得できることを検証します。
     */
    @Test
    void findByProductIdReturnsOutOfStockProduct() {
        ProductEntity productEntity = productRepository.findByProductId(3L);

        assertNotNull(productEntity);
        assertEquals(3L, productEntity.getProductId());
        assertEquals(0, productEntity.getStock());
        assertEquals("ON_SALE", productEntity.getStatus());
    }

    /**
     * 販売停止中の商品詳細を取得できることを検証します。
     */
    @Test
    void findByProductIdReturnsStoppedProduct() {
        ProductEntity productEntity = productRepository.findByProductId(4L);

        assertNotNull(productEntity);
        assertEquals(4L, productEntity.getProductId());
        assertEquals("STOPPED", productEntity.getStatus());
        assertEquals(8, productEntity.getStock());
    }

    /**
     * 存在しない商品IDではnullが返ることを検証します。
     */
    @Test
    void findByProductIdReturnsNullWhenProductDoesNotExist() {
        ProductEntity productEntity = productRepository.findByProductId(999L);

        assertNull(productEntity);
    }

    /**
     * 最小数量1をcart_itemへ保存できることを検証します。
     */
    @Test
    void insertCartItemStoresMinimumQuantity() {
        cartRepository.insertCartItem(1L, 1);

        Integer quantity = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM cart_item WHERE product_id = ?",
                Integer.class,
                1L);

        assertEquals(1, quantity);
    }

    /**
     * 最大数量99をcart_itemへ保存できることを検証します。
     */
    @Test
    void insertCartItemStoresMaximumQuantity() {
        cartRepository.insertCartItem(1L, 99);

        Integer quantity = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM cart_item WHERE product_id = ?",
                Integer.class,
                1L);

        assertEquals(99, quantity);
    }

    /**
     * 同一商品の複数追加後に数量合計を取得できることを検証します。
     */
    @Test
    void sumCartItemQuantityByProductIdReturnsAccumulatedQuantity() {
        cartRepository.insertCartItem(1L, 2);
        cartRepository.insertCartItem(1L, 3);
        cartRepository.insertCartItem(1L, 4);

        int quantity = cartRepository.sumCartItemQuantityByProductId(1L);

        assertEquals(9, quantity);
    }

    /**
     * テスト用の商品データを登録します。
     *
     * @param productId 商品ID
     * @param name 商品名
     * @param description 商品説明
     * @param price 価格
     * @param stock 在庫数
     * @param status 販売状態
     * @param imageUrl 画像URL
     */
    private void insertProduct(Long productId, String name, String description, Integer price, Integer stock,
            String status, String imageUrl) {
        jdbcTemplate.update(
                "INSERT INTO products (product_id, name, description, price, stock, status, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)",
                productId,
                name,
                description,
                price,
                stock,
                status,
                imageUrl);
    }
}