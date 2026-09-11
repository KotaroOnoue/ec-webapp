package com.example.ec.repository.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
 * discount_codesテーブルに対応するEntityです。
 */
@Data
public class DiscountCodeEntity {

    /** クーポン番号です。 */
    private Long discountCode;

    /** 割引率です。 */
    private BigDecimal discountRate;
}