package com.example.ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ec.exception.InsufficientStockException;
import com.example.ec.repository.CartRepository;
import com.example.ec.repository.DiscountCodeRepository;
import com.example.ec.repository.entity.CartItemEntity;
import com.example.ec.repository.entity.DiscountCodeEntity;
import com.example.ec.service.model.CartItemModel;

/**
 * カート画面に関わるCartServiceの動作をモックRepositoryで検証する単体テストです。
 */
@ExtendWith(MockitoExtension.class)
class CartServiceMockTest {

    /** カートRepositoryのモックです。 */
    @Mock
    private CartRepository cartRepository;

    /** 商品Serviceのモックです。 */
    @Mock
    private ProductService productService;

    /** クーポンRepositoryのモックです。 */
    @Mock
    private DiscountCodeRepository discountCodeRepository;

    /** テスト対象のServiceです。 */
    @InjectMocks
    private CartService cartService;

    /**
     * カート内商品一覧を画面表示用モデルへ変換して返すことを検証します。
     */
    @Test
    void getCartItemsReturnsConvertedModels() {
        when(cartRepository.findCartItems()).thenReturn(List.of(
                createCartItemEntity(1L, "ワイヤレスイヤホン", 5980, 2),
                createCartItemEntity(2L, "ゲーミングマウス", 3980, 1)));

        List<CartItemModel> cartItems = cartService.getCartItems();

        assertEquals(2, cartItems.size());
        assertEquals(1L, cartItems.get(0).getProductId());
        assertEquals("ワイヤレスイヤホン", cartItems.get(0).getName());
        assertEquals(5980, cartItems.get(0).getPrice());
        assertEquals(2, cartItems.get(0).getQuantity());
        assertEquals(11960, cartItems.get(0).getSubtotal());
    }

    /**
     * 合計金額を小計の合計として返すことを検証します。
     */
    @Test
    void getTotalAmountReturnsSumOfSubtotals() {
        when(cartRepository.findCartItems()).thenReturn(List.of(
                createCartItemEntity(1L, "ワイヤレスイヤホン", 5980, 2),
                createCartItemEntity(2L, "ゲーミングマウス", 3980, 1)));

        int totalAmount = cartService.getTotalAmount();

        assertEquals(15940, totalAmount);
    }

    /**
     * 空カート時の合計金額が0であることを検証します。
     */
    @Test
    void getTotalAmountReturnsZeroWhenCartIsEmpty() {
        when(cartRepository.findCartItems()).thenReturn(List.of());

        int totalAmount = cartService.getTotalAmount();

        assertEquals(0, totalAmount);
    }

    /**
     * カートに商品がある場合の送料が1000円であることを検証します。
     */
    @Test
    void getShippingAmountReturnsFlatFeeWhenCartHasItems() {
        when(cartRepository.findCartItems()).thenReturn(List.of(
                createCartItemEntity(1L, "ワイヤレスイヤホン", 5980, 2)));

        int shippingAmount = cartService.getShippingAmount();

        assertEquals(1000, shippingAmount);
    }

    /**
     * 空カート時の送料が0円であることを検証します。
     */
    @Test
    void getShippingAmountReturnsZeroWhenCartIsEmpty() {
        when(cartRepository.findCartItems()).thenReturn(List.of());

        int shippingAmount = cartService.getShippingAmount();

        assertEquals(0, shippingAmount);
    }

    /**
     * 請求合計金額に商品合計と送料が加算されることを検証します。
     */
    @Test
    void getBillingAmountReturnsTotalAmountWithShipping() {
        when(cartRepository.findCartItems()).thenReturn(List.of(
                createCartItemEntity(1L, "ワイヤレスイヤホン", 5980, 2),
                createCartItemEntity(2L, "ゲーミングマウス", 3980, 1)));

        int billingAmount = cartService.getBillingAmount();

        assertEquals(16940, billingAmount);
    }

    /**
     * 有効なクーポンの割引額を返すことを検証します。
     */
    @Test
    void getDiscountAmountReturnsDiscountWhenCodeExists() {
        when(cartRepository.findCartItems()).thenReturn(List.of(
                createCartItemEntity(1L, "ワイヤレスイヤホン", 5980, 2),
                createCartItemEntity(2L, "ゲーミングマウス", 3980, 1)));
        when(discountCodeRepository.findByDiscountCode(1001L)).thenReturn(createDiscountCodeEntity(1001L, "0.10"));

        int discountAmount = cartService.getDiscountAmount(1001L);

        assertEquals(1694, discountAmount);
    }

    /**
     * 無効なクーポンでは割引額が0であることを検証します。
     */
    @Test
    void getDiscountAmountReturnsZeroWhenCodeDoesNotExist() {
        when(discountCodeRepository.findByDiscountCode(9999L)).thenReturn(null);

        int discountAmount = cartService.getDiscountAmount(9999L);

        assertEquals(0, discountAmount);
    }

    /**
     * 有効なクーポン適用後の請求金額を返すことを検証します。
     */
    @Test
    void getDiscountedBillingAmountReturnsBillingAmountAfterDiscount() {
        when(cartRepository.findCartItems()).thenReturn(List.of(
                createCartItemEntity(1L, "ワイヤレスイヤホン", 5980, 2),
                createCartItemEntity(2L, "ゲーミングマウス", 3980, 1)));
        when(discountCodeRepository.findByDiscountCode(1001L)).thenReturn(createDiscountCodeEntity(1001L, "0.10"));

        int billingAmount = cartService.getDiscountedBillingAmount(1001L);

        assertEquals(15246, billingAmount);
    }

    /**
     * 合計数量を返すことを検証します。
     */
    @Test
    void getTotalQuantityReturnsSumOfQuantities() {
        when(cartRepository.findCartItems()).thenReturn(List.of(
                createCartItemEntity(1L, "ワイヤレスイヤホン", 5980, 2),
                createCartItemEntity(2L, "ゲーミングマウス", 3980, 4)));

        int totalQuantity = cartService.getTotalQuantity();

        assertEquals(6, totalQuantity);
    }

    /**
     * 空カート時の合計数量が0であることを検証します。
     */
    @Test
    void getTotalQuantityReturnsZeroWhenCartIsEmpty() {
        when(cartRepository.findCartItems()).thenReturn(List.of());

        int totalQuantity = cartService.getTotalQuantity();

        assertEquals(0, totalQuantity);
    }

    /**
     * 数量更新時に在庫チェック後に削除して再登録することを検証します。
     */
    @Test
    void updateCartItemValidatesThenDeletesAndInserts() {
        cartService.updateCartItem(1L, 5);

        InOrder inOrder = inOrder(productService, cartRepository);
        inOrder.verify(productService, times(1)).validateAddToCart(1L, 0, 5);
        inOrder.verify(cartRepository, times(1)).deleteCartItemsByProductId(1L);
        inOrder.verify(cartRepository, times(1)).insertCartItem(1L, 5);
    }

    /**
     * 在庫超過時は削除・再登録を行わずに例外を送出することを検証します。
     */
    @Test
    void updateCartItemThrowsWhenStockIsExceeded() {
        InsufficientStockException exception = new InsufficientStockException("指定した数量は在庫数を超えています。");
        org.mockito.Mockito.doThrow(exception).when(productService).validateAddToCart(1L, 0, 11);

        assertThrows(InsufficientStockException.class, () -> cartService.updateCartItem(1L, 11));

        verify(cartRepository, never()).deleteCartItemsByProductId(1L);
        verify(cartRepository, never()).insertCartItem(1L, 11);
    }

    /**
     * 商品削除時にRepositoryへ削除が委譲されることを検証します。
     */
    @Test
    void deleteCartItemDelegatesToRepository() {
        cartService.deleteCartItem(2L);

        verify(cartRepository, times(1)).deleteCartItemsByProductId(2L);
    }

    /**
     * 商品ごとのカート内数量を取得できることを検証します。
     */
    @Test
    void getCartQuantityByProductIdReturnsQuantity() {
        when(cartRepository.sumCartItemQuantityByProductId(1L)).thenReturn(3);

        int quantity = cartService.getCartQuantityByProductId(1L);

        assertEquals(3, quantity);
    }

    /**
     * 商品追加時にRepositoryへ保存が委譲されることを検証します。
     */
    @Test
    void addCartItemDelegatesToRepository() {
        cartService.addCartItem(3L, 2);

        verify(cartRepository, times(1)).insertCartItem(3L, 2);
    }

    /**
     * テスト用のカート商品Entityを生成します。
     *
     * @param productId 商品ID
     * @param name 商品名
     * @param price 単価
     * @param quantity 数量
     * @return カート商品Entity
     */
    private CartItemEntity createCartItemEntity(Long productId, String name, Integer price, Integer quantity) {
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setProductId(productId);
        cartItemEntity.setName(name);
        cartItemEntity.setPrice(price);
        cartItemEntity.setQuantity(quantity);
        return cartItemEntity;
    }

    /**
     * テスト用のクーポンEntityを生成します。
     *
     * @param discountCode クーポン番号
     * @param discountRate 割引率
     * @return クーポンEntity
     */
    private DiscountCodeEntity createDiscountCodeEntity(Long discountCode, String discountRate) {
        DiscountCodeEntity discountCodeEntity = new DiscountCodeEntity();
        discountCodeEntity.setDiscountCode(discountCode);
        discountCodeEntity.setDiscountRate(new java.math.BigDecimal(discountRate));
        return discountCodeEntity;
    }
}