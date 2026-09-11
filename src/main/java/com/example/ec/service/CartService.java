package com.example.ec.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ec.repository.CartRepository;
import com.example.ec.repository.DiscountCodeRepository;
import com.example.ec.repository.entity.CartItemEntity;
import com.example.ec.repository.entity.DiscountCodeEntity;
import com.example.ec.service.model.CartItemModel;

/**
 * カートに関するビジネスロジックを扱うServiceです。
 */
@Service
public class CartService {

    /** 送料です。 */
    private static final int SHIPPING_AMOUNT = 1000;

    /** カートRepositoryです。 */
    @Autowired
    private CartRepository cartRepository;

    /** 商品Serviceです。 */
    @Autowired
    private ProductService productService;

    /** クーポンRepositoryです。 */
    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    /**
     * カート内の商品一覧を取得します。
     *
     * @return カート内の商品一覧
     */
    public List<CartItemModel> getCartItems() {
        return cartRepository.findCartItems().stream()
                .map(this::toModel)
                .toList();
    }

    /**
     * カート内の合計金額を取得します。
     *
     * @return 合計金額
     */
    public int getTotalAmount() {
        return getCartItems().stream()
                .mapToInt(CartItemModel::getSubtotal)
                .sum();
    }

    /**
     * カート内商品の送料を取得します。
     *
     * @return 送料
     */
    public int getShippingAmount() {
        return getCartItems().isEmpty() ? 0 : SHIPPING_AMOUNT;
    }

    /**
     * カート内商品の請求合計金額を取得します。
     *
     * @return 請求合計金額
     */
    public int getBillingAmount() {
        return getTotalAmount() + getShippingAmount();
    }

    /**
     * 指定したクーポン番号が有効かを判定します。
     *
     * @param discountCode クーポン番号
     * @return 有効ならtrue
     */
    public boolean isDiscountCodeAvailable(Long discountCode) {
        return findDiscountCode(discountCode) != null;
    }

    /**
     * 指定したクーポンによる割引額を取得します。
     *
     * @param discountCode クーポン番号
     * @return 割引額
     */
    public int getDiscountAmount(Long discountCode) {
        DiscountCodeEntity discountCodeEntity = findDiscountCode(discountCode);
        if (discountCodeEntity == null) {
            return 0;
        }
        return BigDecimal.valueOf(getBillingAmount())
                .multiply(discountCodeEntity.getDiscountRate())
                .setScale(0, RoundingMode.DOWN)
                .intValue();
    }

    /**
     * クーポン適用後の請求金額を取得します。
     *
     * @param discountCode クーポン番号
     * @return クーポン適用後の請求金額
     */
    public int getDiscountedBillingAmount(Long discountCode) {
        return getBillingAmount() - getDiscountAmount(discountCode);
    }

    /**
     * カート内の合計数量を取得します。
     *
     * @return 合計数量
     */
    public int getTotalQuantity() {
        return getCartItems().stream()
                .mapToInt(CartItemModel::getQuantity)
                .sum();
    }

    /**
     * クーポン情報を取得します。
     *
     * @param discountCode クーポン番号
     * @return クーポン情報。存在しない場合はnull
     */
    private DiscountCodeEntity findDiscountCode(Long discountCode) {
        if (discountCode == null) {
            return null;
        }
        return discountCodeRepository.findByDiscountCode(discountCode);
    }

    /**
     * 商品をcart_itemテーブルへ追加します。
     *
     * @param productId 商品ID
     * @param quantity 追加数量
     */
    public void addCartItem(Long productId, Integer quantity) {
        cartRepository.insertCartItem(productId, quantity);
    }

    /**
     * カート内商品の数量を更新します。
     *
     * @param productId 商品ID
     * @param quantity 更新数量
     */
    public void updateCartItem(Long productId, Integer quantity) {
        productService.validateAddToCart(productId, 0, quantity);
        cartRepository.deleteCartItemsByProductId(productId);
        cartRepository.insertCartItem(productId, quantity);
    }

    /**
     * カート内の商品を削除します。
     *
     * @param productId 商品ID
     */
    public void deleteCartItem(Long productId) {
        cartRepository.deleteCartItemsByProductId(productId);
    }

    /**
     * 商品ごとのカート内数量を取得します。
     *
     * @param productId 商品ID
     * @return 数量
     */
    public int getCartQuantityByProductId(Long productId) {
        return cartRepository.sumCartItemQuantityByProductId(productId);
    }

    /**
     * EntityをService層のモデルへ変換します。
     *
     * @param cartItemEntity 変換元のEntity
     * @return 変換後のモデル
     */
    public CartItemModel toModel(CartItemEntity cartItemEntity) {
        CartItemModel cartItemModel = new CartItemModel();
        cartItemModel.setProductId(cartItemEntity.getProductId());
        cartItemModel.setName(cartItemEntity.getName());
        cartItemModel.setPrice(cartItemEntity.getPrice());
        cartItemModel.setQuantity(cartItemEntity.getQuantity());
        cartItemModel.setSubtotal(cartItemEntity.getPrice() * cartItemEntity.getQuantity());
        return cartItemModel;
    }
}