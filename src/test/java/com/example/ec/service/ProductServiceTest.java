package com.example.ec.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.ec.exception.InsufficientStockException;
import com.example.ec.exception.ProductUnavailableException;
import com.example.ec.service.model.ProductModel;

/**
 * ProductServiceのビジネスロジックを検証するテストです。
 */
@SpringBootTest
class ProductServiceTest {

    /** 商品Serviceです。 */
    @Autowired
    private ProductService productService;

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
     * 販売中の商品だけがモデルとして返されることを検証します。
     */
    @Test
    void getOnSaleProductsReturnsOnlyOnSaleProducts() {
        List<ProductModel> productModels = productService.getOnSaleProducts();

        assertEquals(3, productModels.size());
        assertEquals(List.of(1L, 2L, 3L), productModels.stream().map(ProductModel::getProductId).toList());
    }

    /**
     * 商品IDで商品詳細を取得できることを検証します。
     */
    @Test
    void getProductByIdReturnsProductDetail() {
        ProductModel productModel = productService.getProductById(1L);

        assertEquals("ワイヤレスイヤホン", productModel.getName());
        assertEquals("ON_SALE", productModel.getStatus());
    }

    /**
     * 在庫数を超える数量を追加しようとすると例外になることを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenQuantityExceedsStock() {
        assertThrows(InsufficientStockException.class, () -> productService.validateAddToCart(1L, 10, 1));
    }

    /**
     * 在庫数以内の数量であればカートに追加できることを検証します。
     */
    @Test
    void validateAddToCartAllowsQuantityWithinStock() {
        assertDoesNotThrow(() -> productService.validateAddToCart(1L, 9, 1));
    }

    /**
     * 販売停止中の商品はカート追加できないことを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenProductIsNotOnSale() {
        assertThrows(ProductUnavailableException.class, () -> productService.validateAddToCart(4L, 0, 1));
    }

    /**
     * 数量が1未満の場合は例外になることを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenQuantityIsLessThanOne() {
        assertThrows(IllegalArgumentException.class, () -> productService.validateAddToCart(1L, 0, 0));
    }

    /**
     * cart_itemテーブルへ数量付きで追加できることを検証します。
     */
    @Test
    void addCartItemStoresQuantity() {
        productService.addCartItem(2L, 4);

        Integer quantity = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM cart_item WHERE product_id = ?",
                Integer.class,
                2L);

        assertEquals(4, quantity);
    }
}