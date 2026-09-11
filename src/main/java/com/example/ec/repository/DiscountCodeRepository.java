package com.example.ec.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.ec.repository.entity.DiscountCodeEntity;

/**
 * クーポン割引情報を取得するRepositoryです。
 */
@Mapper
public interface DiscountCodeRepository {

    /**
     * クーポン番号に対応する割引情報を取得します。
     *
     * @param discountCode クーポン番号
     * @return 割引情報。存在しない場合はnull
     */
    DiscountCodeEntity findByDiscountCode(@Param("discountCode") Long discountCode);
}