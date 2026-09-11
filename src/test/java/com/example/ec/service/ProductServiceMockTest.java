package com.example.ec.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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
 * ProductServiceの動作をモックRepositoryで検証する単体テストです。
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceMockTest {

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
     * 販売中の商品だけがモデルとして返されることを検証します。
     */
    @Test
    void getOnSaleProductsReturnsOnlyOnSaleProducts() {
        when(productRepository.findOnSaleProducts()).thenReturn(List.of(
                createProductEntity(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"),
                createProductEntity(2L, "ゲーミングマウス", "6ボタン搭載", 3980, 5, "ON_SALE", "/images/products/2.png"),
                createProductEntity(3L, "USB-Cハブ", "5in1モデル", 2980, 0, "ON_SALE", "/images/products/3.png")));

        List<ProductModel> productModels = productService.getOnSaleProducts();

        assertEquals(3, productModels.size());
        assertEquals(List.of(1L, 2L, 3L), productModels.stream().map(ProductModel::getProductId).toList());
        assertEquals("ワイヤレスイヤホン", productModels.get(0).getName());
        assertEquals("/images/products/1.png", productModels.get(0).getImageUrl());
    }

    /**
     * 商品IDで商品詳細を取得できることを検証します。
     */
    @Test
    void getProductByIdReturnsProductDetail() {
        when(productRepository.findByProductId(1L)).thenReturn(
                createProductEntity(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        ProductModel productModel = productService.getProductById(1L);

        assertEquals(1L, productModel.getProductId());
        assertEquals("ワイヤレスイヤホン", productModel.getName());
        assertEquals("ON_SALE", productModel.getStatus());
        assertEquals(5980, productModel.getPrice());
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
     * 在庫数以内の最小数量であればカートに追加できることを検証します。
     */
    @Test
    void validateAddToCartAllowsMinimumQuantityWithinStock() {
        when(productRepository.findByProductId(1L)).thenReturn(
                createProductEntity(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        assertDoesNotThrow(() -> productService.validateAddToCart(1L, 0, 1));
    }

    /**
     * 在庫数以内の上限数量であればカートに追加できることを検証します。
     */
    @Test
    void validateAddToCartAllowsMaximumQuantityWithinStock() {
        when(productRepository.findByProductId(10L)).thenReturn(
                createProductEntity(10L, "大容量モバイルバッテリー", "大容量モデル", 9980, 120, "ON_SALE", "/images/products/10.png"));

        assertDoesNotThrow(() -> productService.validateAddToCart(10L, 0, 99));
    }

    /**
     * 数量が1未満の場合は例外になることを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenQuantityIsLessThanOne() {
        assertThrows(IllegalArgumentException.class, () -> productService.validateAddToCart(1L, 0, 0));
    }

    /**
     * 数量が99を超える場合は例外になることを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenQuantityExceedsMaximum() {
        assertThrows(IllegalArgumentException.class, () -> productService.validateAddToCart(1L, 0, 100));
    }

    /**
     * 在庫数ちょうどまでの追加は許可されることを検証します。
     */
    @Test
    void validateAddToCartAllowsQuantityAtStockBoundary() {
        when(productRepository.findByProductId(1L)).thenReturn(
                createProductEntity(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        assertDoesNotThrow(() -> productService.validateAddToCart(1L, 9, 1));
    }

    /**
     * 在庫数を1つでも超える追加は例外になることを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenQuantityExceedsStock() {
        when(productRepository.findByProductId(1L)).thenReturn(
                createProductEntity(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        assertThrows(InsufficientStockException.class, () -> productService.validateAddToCart(1L, 10, 1));
    }

    /**
     * 販売停止中の商品はカート追加できないことを検証します。
     */
    @Test
    void validateAddToCartThrowsWhenProductIsNotOnSale() {
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
     * 商品追加時にCartRepositoryへ保存が委譲されることを検証します。
     */
    @Test
    void addCartItemDelegatesToCartRepository() {
        productService.addCartItem(2L, 4);

        verify(cartRepository, times(1)).insertCartItem(2L, 4);
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