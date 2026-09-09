package com.example.ec.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}