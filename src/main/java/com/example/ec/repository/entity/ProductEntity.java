package com.example.ec.repository.entity;

import lombok.Data;

/**
 * productsテーブルに対応するエンティティクラスです。
 */
@Data
public class ProductEntity {

    /** 商品IDです。 */
    private Long productId;

    /** 商品名です。 */
    private String name;

    /** 商品説明です。 */
    private String description;

    /** 価格です。 */
    private Integer price;

    /** 在庫数です。 */
    private Integer stock;

    /** 販売状態です。 */
    private String status;

    /** 画像URLです。 */
    private String imageUrl;
}