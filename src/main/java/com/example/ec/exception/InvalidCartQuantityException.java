package com.example.ec.exception;

/**
 * カート数量が許容範囲外の場合の業務例外です。
 */
public class InvalidCartQuantityException extends RuntimeException {

    /**
     * メッセージを指定して例外を生成します。
     *
     * @param message 例外メッセージ
     */
    public InvalidCartQuantityException(String message) {
        super(message);
    }
}