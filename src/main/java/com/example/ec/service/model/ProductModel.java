package com.example.ec.service.model;

import lombok.Data;

/**
 * Service層からController層へ受け渡す商品モデルです。
 */
@Data
public class ProductModel {

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