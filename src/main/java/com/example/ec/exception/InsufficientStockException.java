package com.example.ec.exception;

/**
 * カート追加時に在庫が不足している場合の業務例外です。
 */
public class InsufficientStockException extends RuntimeException {

    /**
     * メッセージを指定して例外を生成します。
     *
     * @param message 例外メッセージ
     */
    public InsufficientStockException(String message) {
        super(message);
    }
}