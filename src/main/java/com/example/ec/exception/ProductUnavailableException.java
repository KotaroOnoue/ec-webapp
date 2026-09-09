package com.example.ec.exception;

/**
 * 販売停止などにより商品を利用できない場合の業務例外です。
 */
public class ProductUnavailableException extends RuntimeException {

    /**
     * メッセージを指定して例外を生成します。
     *
     * @param message 例外メッセージ
     */
    public ProductUnavailableException(String message) {
        super(message);
    }
}