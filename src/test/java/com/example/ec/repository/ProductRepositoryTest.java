package com.example.ec.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
     * 商品IDで商品を取得できることを検証します。
     */
    @Test
    void findByProductIdReturnsProduct() {
        ProductEntity productEntity = productRepository.findByProductId(4L);

        assertNotNull(productEntity);
        assertEquals("Webカメラ", productEntity.getName());
        assertEquals("STOPPED", productEntity.getStatus());
    }
}