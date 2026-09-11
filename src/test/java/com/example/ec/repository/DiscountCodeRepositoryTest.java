package com.example.ec.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * DiscountCodeRepositoryのデータ取得を検証するテストです。
 */
@SpringBootTest
class DiscountCodeRepositoryTest {

    /** テスト対象のRepositoryです。 */
    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    /**
     * 登録済みクーポン番号から割引率を取得できることを検証します。
     */
    @Test
    void findByDiscountCodeReturnsDiscountCode() {
        var discountCode = discountCodeRepository.findByDiscountCode(1001L);

        assertEquals(1001L, discountCode.getDiscountCode());
        assertEquals("0.10", discountCode.getDiscountRate().toPlainString());
    }

    /**
     * 存在しないクーポン番号ではnullを返すことを検証します。
     */
    @Test
    void findByDiscountCodeReturnsNullWhenCodeDoesNotExist() {
        var discountCode = discountCodeRepository.findByDiscountCode(9999L);

        assertNull(discountCode);
    }
}