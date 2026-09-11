package com.example.ec.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ec.exception.InsufficientStockException;
import com.example.ec.exception.ProductUnavailableException;
import com.example.ec.repository.CartRepository;
import com.example.ec.repository.ProductRepository;
import com.example.ec.repository.entity.ProductEntity;
import com.example.ec.service.model.ProductModel;

/**
 * 商品詳細画面に関わるProductServiceの動作をモックRepositoryで検証する単体テストです。
 */
@ExtendWith(MockitoExtension.class)
class ProductDetailServiceMockTest {

    /** 商品Repositoryのモックです。 */
    @Mock
    private ProductRepository productRepository;

    /** カートRepositoryのモックです。 */
    @Mock
    private CartRepository cartRepository;

    /** テスト対象のServiceです。 */
    @InjectMocks
    private ProductService productService;

    /**
     * 商品IDに対応する商品詳細を取得できることを検証します。
     */
    @Test
    void getProductByIdReturnsProductDetail() {
        when(productRepository.findByProductId(1L)).thenReturn(
                createProductEntity(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        ProductModel productModel = productService.getProductById(1L);

        assertEquals(1L, productModel.getProductId());
        assertEquals("ワイヤレスイヤホン", productModel.getName());
        assertEquals("ノイズキャンセリング対応", productModel.getDescription());
        assertEquals(5980, productModel.getPrice());
        assertEquals(10, productModel.getStock());
        assertEquals("ON_SALE", productModel.getStatus());
        assertEquals("/images/products/1.png", productModel.getImageUrl());
    }

    /**
     * 存在しない商品IDを指定した場合は例外になることを検証します。
     */
    @Test
    void getProductByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findByProductId(999L)).thenReturn(null);

        assertThrows(ProductUnavailableException.class, () -> productService.getProductById(999L));
    }

    /**
     * 最小数量1であればカート追加可能であることを検証します。
     */
    @Test
    void validateAddToCartAllowsMinimumQuantity() {
        when(productRepository.findByProductId(1L)).thenReturn(
                createProductEntity(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        assertDoesNotThrow(() -> productService.validateAddToCart(1L, 0, 1));
    }

    /**
     * 数量99までなら上限内としてカート追加可能であることを検証します。
     */
    @Test
    void validateAddToCartAllowsMaximumQuantity() {
        when(productRepository.findByProductId(10L)).thenReturn(
                createProductEntity(10L, "4Kモニター", "高解像度モニター", 49800, 120, "ON_SALE", "/images/products/10.png"));

        assertDoesNotThrow(() -> productService.validateAddToCart(10L, 0, 99));
    }

    /**
     * 数量0は下限未満のため例外になることを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenQuantityIsLessThanOne() {
        assertThrows(IllegalArgumentException.class, () -> productService.validateAddToCart(1L, 0, 0));
    }

    /**
     * 数量100は上限超過のため例外になることを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenQuantityExceedsMaximum() {
        assertThrows(IllegalArgumentException.class, () -> productService.validateAddToCart(1L, 0, 100));
    }

    /**
     * 在庫ちょうどまでの追加は許可されることを検証します。
     */
    @Test
    void validateAddToCartAllowsQuantityAtStockBoundary() {
        when(productRepository.findByProductId(1L)).thenReturn(
                createProductEntity(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        assertDoesNotThrow(() -> productService.validateAddToCart(1L, 9, 1));
    }

    /**
     * 在庫を1つでも超えると例外になることを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenQuantityExceedsStock() {
        when(productRepository.findByProductId(1L)).thenReturn(
                createProductEntity(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        assertThrows(InsufficientStockException.class, () -> productService.validateAddToCart(1L, 10, 1));
    }

    /**
     * 在庫0の商品はカート追加できないことを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenProductIsOutOfStock() {
        when(productRepository.findByProductId(3L)).thenReturn(
                createProductEntity(3L, "USB-Cハブ", "5in1モデル", 2980, 0, "ON_SALE", "/images/products/3.png"));

        assertThrows(InsufficientStockException.class, () -> productService.validateAddToCart(3L, 0, 1));
    }

    /**
     * 販売停止中の商品はカート追加できないことを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenProductIsStopped() {
        when(productRepository.findByProductId(4L)).thenReturn(
                createProductEntity(4L, "Webカメラ", "フルHD対応", 4980, 8, "STOPPED", "/images/products/4.png"));

        assertThrows(ProductUnavailableException.class, () -> productService.validateAddToCart(4L, 0, 1));
    }

    /**
     * 存在しない商品はカート追加できないことを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenProductDoesNotExist() {
        when(productRepository.findByProductId(999L)).thenReturn(null);

        assertThrows(ProductUnavailableException.class, () -> productService.validateAddToCart(999L, 0, 1));
    }

    /**
     * カート追加時にCartRepositoryへ保存処理が委譲されることを検証します。
     */
    @Test
    void addCartItemDelegatesToCartRepository() {
        productService.addCartItem(2L, 3);

        verify(cartRepository, times(1)).insertCartItem(2L, 3);
    }

    /**
     * テスト用の商品Entityを生成します。
     *
     * @param productId 商品ID
     * @param name 商品名
     * @param description 商品説明
     * @param price 価格
     * @param stock 在庫数
     * @param status 販売状態
     * @param imageUrl 画像URL
     * @return 商品Entity
     */
    private ProductEntity createProductEntity(Long productId, String name, String description, Integer price,
            Integer stock, String status, String imageUrl) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setProductId(productId);
        productEntity.setName(name);
        productEntity.setDescription(description);
        productEntity.setPrice(price);
        productEntity.setStock(stock);
        productEntity.setStatus(status);
        productEntity.setImageUrl(imageUrl);
        return productEntity;
    }
}