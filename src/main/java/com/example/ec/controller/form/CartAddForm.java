package com.example.ec.controller.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品をカートへ追加するフォームです。
 */
@Data
public class CartAddForm {

    /** 商品IDです。 */
    @NotNull(message = "{validation.cartAddForm.productId.notNull}")
    private Long productId;

    /** 追加数量です。 */
    @NotNull(message = "{validation.cartAddForm.quantity.notNull}")
    @Min(value = 1, message = "{validation.cartAddForm.quantity.min}")
    private Integer quantity;
}