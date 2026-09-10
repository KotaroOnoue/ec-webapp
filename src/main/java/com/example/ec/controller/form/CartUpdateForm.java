package com.example.ec.controller.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * カート内商品の数量更新フォームです。
 */
@Data
public class CartUpdateForm {

    /** 商品IDです。 */
    @NotNull(message = "{validation.cartUpdateForm.productId.notNull}")
    private Long productId;

    /** 更新数量です。 */
    @NotNull(message = "{validation.cartUpdateForm.quantity.notNull}")
    @Min(value = 1, message = "{validation.cartUpdateForm.quantity.min}")
    @Max(value = 99, message = "{validation.cartUpdateForm.quantity.max}")
    private Integer quantity;
}