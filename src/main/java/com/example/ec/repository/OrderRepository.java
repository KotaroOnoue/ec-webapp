package com.example.ec.repository;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 注文情報を保存するRepositoryです。
 */
@Mapper
public interface OrderRepository {

    /**
     * 次に採番する注文IDを取得します。
     *
     * @return 次の注文ID
     */
    Long findNextOrderId();

    /**
     * 注文情報を保存します。
     *
     * @param orderId 注文ID
     * @param customerName 顧客名
     * @param postalCode 郵便番号
     * @param address 住所
     * @param phoneNumber 電話番号
     * @param totalAmount 合計金額
     * @param orderAt 注文日時
     */
    void insertOrder(
            @Param("orderId") Long orderId,
            @Param("customerName") String customerName,
            @Param("postalCode") String postalCode,
            @Param("address") String address,
            @Param("phoneNumber") String phoneNumber,
            @Param("totalAmount") Integer totalAmount,
            @Param("orderAt") LocalDateTime orderAt);
}