package com.example.ec.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ec.repository.CartRepository;
import com.example.ec.repository.entity.CartItemEntity;
import com.example.ec.service.model.CartItemModel;

/**
 * カートに関するビジネスロジックを扱うServiceです。
 */
@Service
public class CartService {

    /** カートRepositoryです。 */
    @Autowired
    private CartRepository cartRepository;

    /** 商品Serviceです。 */
    @Autowired
    private ProductService productService;

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