package com.example.ec.service.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * カート画面および注文確認画面で利用する集計済みモデルです。
 */
@Data
public class CartSummaryModel {

    /** カート内の商品一覧です。 */
    private List<CartItemModel> cartItems;

    /** 商品ごとの数量一覧です。 */
    private Map<Long, Integer> cartQuantities;

    /** カート内の合計数量です。 */
    private Integer totalQuantity;

    /** 商品合計金額です。 */
    private Integer totalAmount;

    /** 送料です。 */
    private Integer shippingAmount;

    /** 割引額です。 */
    private Integer discountAmount;

    /** 請求金額です。 */
    private Integer billingAmount;
}