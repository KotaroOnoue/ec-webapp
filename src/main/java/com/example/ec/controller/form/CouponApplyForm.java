package com.example.ec.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * クーポン適用フォームです。
 */
@Data
public class CouponApplyForm {

    /** クーポン番号です。 */
    @NotBlank(message = "{validation.couponApplyForm.discountCode.notBlank}")
    @Pattern(regexp = "^[0-9]+$", message = "{validation.couponApplyForm.discountCode.pattern}")
    private String discountCode;
}