package com.example.ec.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.example.ec.exception.InsufficientStockException;
import com.example.ec.exception.ProductUnavailableException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * アプリケーション全体の例外を処理するハンドラです。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** メッセージ解決用コンポーネントです。 */
    @Autowired
    private MessageSource messageSource;

    /**
     * 在庫不足の業務例外を処理します。
     *
     * @param request リクエスト
     * @return リダイレクト先
     */
    @ExceptionHandler(InsufficientStockException.class)
    public String handleInsufficientStockException(HttpServletRequest request) {
        return redirectWithMessage(request, "error.cart.insufficientStock");
    }

    /**
     * 商品利用不可の業務例外を処理します。
     *
     * @param request リクエスト
     * @return リダイレクト先
     */
    @ExceptionHandler(ProductUnavailableException.class)
    public String handleProductUnavailableException(HttpServletRequest request) {
        return redirectWithMessage(request, "error.product.unavailable");
    }

    /**
     * 不正な数量指定の例外を処理します。
     *
     * @param request リクエスト
     * @return リダイレクト先
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(HttpServletRequest request) {
        return redirectWithMessage(request, "error.cart.invalidQuantity");
    }

    /**
     * エラーメッセージをFlash属性に格納して商品一覧へ戻します。
     *
     * @param request リクエスト
     * @param messageCode メッセージコード
     * @return リダイレクト先
     */
    public String redirectWithMessage(HttpServletRequest request, String messageCode) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", messageSource.getMessage(messageCode, null, Locale.getDefault()));
        return "redirect:" + resolveRedirectPath(request);
    }

    /**
     * 例外発生元に応じたリダイレクト先を判定します。
     *
     * @param request リクエスト
     * @return リダイレクト先パス
     */
    public String resolveRedirectPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri != null && requestUri.matches(".*/products/\\d+/cart$")) {
            return requestUri.replaceFirst("/cart$", "");
        }
        return "/products";
    }
}