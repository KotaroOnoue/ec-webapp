package com.example.ec.service.model;

import lombok.Data;

/**
 * カート画面表示用のモデルです。
 */
@Data
public class CartItemModel {

    /** 商品IDです。 */
    private Long productId;

    /** 商品名です。 */
    private String name;

    /** 単価です。 */
    private Integer price;

    /** 数量です。 */
    private Integer quantity;

    /** 小計です。 */
    private Integer subtotal;
}