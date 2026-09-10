package com.example.ec.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.ec.repository.entity.CartItemEntity;

/**
 * カート情報を取得・更新するRepositoryです。
 */
@Mapper
public interface CartRepository {

    /**
     * カート内の商品一覧を取得します。
     *
     * @return カート内の商品一覧
     */
    List<CartItemEntity> findCartItems();

    /**
     * カートに商品を追加します。
     *
     * @param productId 商品ID
     * @param quantity 追加数量
     */
    void insertCartItem(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 指定した商品のカート登録数量合計を取得します。
     *
     * @param productId 商品ID
     * @return 登録数量合計
     */
    int sumCartItemQuantityByProductId(@Param("productId") Long productId);

    /**
     * 指定した商品のカート情報を削除します。
     *
     * @param productId 商品ID
     */
    void deleteCartItemsByProductId(@Param("productId") Long productId);
}