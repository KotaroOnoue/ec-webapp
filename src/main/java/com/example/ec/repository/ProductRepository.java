package com.example.ec.repository;

import java.util.List;

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
}