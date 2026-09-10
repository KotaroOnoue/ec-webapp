package com.example.ec.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注文確定時の配送先入力フォームです。
 */
@Data
public class OrderForm {

    /** 顧客名です。 */
    @NotBlank(message = "{validation.orderForm.customerName.notBlank}")
    @Size(max = 100, message = "{validation.orderForm.customerName.size}")
    private String customerName;

    /** 郵便番号です。 */
    @NotBlank(message = "{validation.orderForm.postalCode.notBlank}")
    @Pattern(regexp = "^[0-9]{7}$", message = "{validation.orderForm.postalCode.pattern}")
    private String postalCode;

    /** 住所です。 */
    @NotBlank(message = "{validation.orderForm.address.notBlank}")
    @Size(max = 255, message = "{validation.orderForm.address.size}")
    private String address;

    /** 電話番号です。 */
    @NotBlank(message = "{validation.orderForm.phoneNumber.notBlank}")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "{validation.orderForm.phoneNumber.pattern}")
    private String phoneNumber;
}