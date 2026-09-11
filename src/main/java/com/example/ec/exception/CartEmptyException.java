package com.example.ec.exception;

/**
 * カートが空の状態で注文処理を行った場合の業務例外です。
 */
public class CartEmptyException extends RuntimeException {

    /**
     * メッセージを指定して例外を生成します。
     *
     * @param message 例外メッセージ
     */
    public CartEmptyException(String message) {
        super(message);
    }
}