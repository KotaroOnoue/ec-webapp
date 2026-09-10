package com.example.ec.repository;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import com.example.ec.repository.entity.ProductEntity;

/**
 * 商品情報を取得するRepositoryです。
 */
@Mapper
public interface ProductRepository {

    /**
     * 販売中の商品一覧を取得します。
     *
     * @return 販売中の商品一覧
     */
    List<ProductEntity> findOnSaleProducts();

    /**
     * 商品IDに対応する商品を取得します。
     *
     * @param productId 商品ID
     * @return 商品情報
     */
    ProductEntity findByProductId(Long productId);

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
}