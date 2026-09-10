package com.example.ec.repository.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * ordersテーブルに対応するEntityです。
 */
@Data
public class OrderEntity {

    /** 注文IDです。 */
    private Long orderId;

    /** 顧客名です。 */
    private String customerName;

    /** 郵便番号です。 */
    private String postalCode;

    /** 住所です。 */
    private String address;

    /** 電話番号です。 */
    private String phoneNumber;

    /** 合計金額です。 */
    private Integer totalAmount;

    /** 注文日時です。 */
    private LocalDateTime orderAt;
}