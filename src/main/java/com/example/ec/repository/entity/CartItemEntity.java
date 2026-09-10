package com.example.ec.repository.entity;

import lombok.Data;

/**
 * カート一覧取得用のエンティティです。
 */
@Data
public class CartItemEntity {

    /** 商品IDです。 */
    private Long productId;

    /** 商品名です。 */
    private String name;

    /** 単価です。 */
    private Integer price;

    /** 数量です。 */
    private Integer quantity;
}