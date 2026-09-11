package com.example.ec.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.ec.repository.entity.ProductEntity;

/**
 * ProductRepositoryのデータ取得を検証するテストです。
 */
@SpringBootTest
class ProductRepositoryTest {

    /** 商品Repositoryです。 */
    @Autowired
    private ProductRepository productRepository;

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
        insertProduct(3L, "USB-Cハブ", "5in1モデル", 2980, 0, "ON_SALE", "/images/products/3.png");
        insertProduct(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png");
        insertProduct(4L, "Webカメラ", "フルHD対応", 4980, 8, "STOPPED", "/images/products/4.png");
        insertProduct(2L, "ゲーミングマウス", "6ボタン搭載", 3980, 5, "ON_SALE", "/images/products/2.png");
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
    void insertProduct(Long productId, String name, String description, Integer price, Integer stock, String status,
            String imageUrl) {
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

    /**
     * 販売中の商品だけが取得されることを検証します。
     */
    @Test
    void findOnSaleProductsReturnsOnlyOnSaleProducts() {
        List<ProductEntity> productEntities = productRepository.findOnSaleProducts();

        assertEquals(3, productEntities.size());
        assertEquals(List.of(1L, 2L, 3L), productEntities.stream().map(ProductEntity::getProductId).toList());
        assertTrue(productEntities.stream().allMatch(product -> "ON_SALE".equals(product.getStatus())));
    }

    /**
     * 販売中の商品が商品ID昇順で取得されることを検証します。
     */
    @Test
    void findOnSaleProductsReturnsProductsOrderedByProductId() {
        List<ProductEntity> productEntities = productRepository.findOnSaleProducts();

        assertEquals(List.of(1L, 2L, 3L), productEntities.stream().map(ProductEntity::getProductId).toList());
    }

    /**
     * 商品IDで商品を取得できることを検証します。
     */
    @Test
    void findByProductIdReturnsProduct() {
        ProductEntity productEntity = productRepository.findByProductId(4L);

        assertNotNull(productEntity);
        assertEquals("Webカメラ", productEntity.getName());
        assertEquals("STOPPED", productEntity.getStatus());
        assertEquals(4980, productEntity.getPrice());
        assertEquals(8, productEntity.getStock());
        assertEquals("/images/products/4.png", productEntity.getImageUrl());
    }

    /**
     * 存在しない商品IDを指定した場合はnullが返ることを検証します。
     */
    @Test
    void findByProductIdReturnsNullWhenProductDoesNotExist() {
        ProductEntity productEntity = productRepository.findByProductId(999L);

        assertNull(productEntity);
    }
}