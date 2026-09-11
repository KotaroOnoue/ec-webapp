package com.example.ec.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ec.controller.form.CartAddForm;
import com.example.ec.service.CartService;
import com.example.ec.service.ProductService;
import com.example.ec.service.model.CartSummaryModel;
import com.example.ec.service.model.ProductModel;

import jakarta.validation.Valid;

/**
 * 商品一覧画面を制御するControllerです。
 */
@Controller
public class ProductController {

    /** 商品Serviceです。 */
    @Autowired
    private ProductService productService;

    /** カートServiceです。 */
    @Autowired
    private CartService cartService;

    /**
     * 商品一覧画面を表示します。
     *
     * @param model 画面モデル
     * @return テンプレート名
     */
    @GetMapping("/products")
    public String showProducts(Model model) {
        preparePageModel(model);
        if (!model.containsAttribute("cartAddForm")) {
            CartAddForm cartAddForm = new CartAddForm();
            cartAddForm.setQuantity(1);
            model.addAttribute("cartAddForm", cartAddForm);
        }
        return "products";
    }

    /**
     * 商品詳細画面を表示します。
     *
     * @param productId 商品ID
     * @param model 画面モデル
     * @return テンプレート名
     */
    @GetMapping("/products/{productId}")
    public String showProductDetail(
            @PathVariable("productId") Long productId,
            Model model) {
        ProductModel product = productService.getProductById(productId);
        prepareProductDetailModel(product, model);
        if (!model.containsAttribute("cartAddForm")) {
            CartAddForm cartAddForm = new CartAddForm();
            cartAddForm.setProductId(productId);
            cartAddForm.setQuantity(1);
            model.addAttribute("cartAddForm", cartAddForm);
        }
        return "products-detail";
    }

    /**
     * 商品をカートへ追加します。
     *
     * @param cartAddForm カート追加フォーム
     * @param bindingResult バリデーション結果
     * @param model 画面モデル
     * @return 遷移先テンプレート名
     */
    @PostMapping("/cart/items")
    public String addToCart(
            @Valid @ModelAttribute("cartAddForm") CartAddForm cartAddForm,
            BindingResult bindingResult,
            @RequestParam(name = "redirectTo", defaultValue = "/products") String redirectTo,
            Model model) {
        if (bindingResult.hasErrors()) {
            if (isProductDetailPath(redirectTo)) {
                Long productId = resolveProductIdForDetail(cartAddForm.getProductId(), redirectTo);
                if (productId == null) {
                    preparePageModel(model);
                    return "products";
                }
                cartAddForm.setProductId(productId);
                ProductModel product = productService.getProductById(productId);
                prepareProductDetailModel(product, model);
                return "products-detail";
            }
            preparePageModel(model);
            return "products";
        }

        int currentQuantity = productService.getCurrentCartQuantity(cartAddForm.getProductId());
        productService.validateAddToCart(cartAddForm.getProductId(), currentQuantity, cartAddForm.getQuantity());
        productService.addCartItem(cartAddForm.getProductId(), cartAddForm.getQuantity());

        return "redirect:" + redirectTo;
    }

    /**
     * 商品一覧画面に必要な共通モデルを設定します。
     *
     * @param model 画面モデル
     */
    public void preparePageModel(Model model) {
        CartSummaryModel cartSummary = cartService.getCartSummary();
        model.addAttribute("products", productService.getOnSaleProducts());
        model.addAttribute("cartItemCount", cartSummary.getTotalQuantity());
        model.addAttribute("cart", cartSummary.getCartQuantities());
    }

    /**
     * 商品詳細画面に必要な共通モデルを設定します。
     *
     * @param product 商品詳細
     * @param model 画面モデル
     */
    public void prepareProductDetailModel(ProductModel product, Model model) {
        CartSummaryModel cartSummary = cartService.getCartSummary();
        model.addAttribute("product", product);
        model.addAttribute("cartItemCount", cartSummary.getTotalQuantity());
        model.addAttribute("cart", cartSummary.getCartQuantities());
        model.addAttribute("purchasable", "ON_SALE".equals(product.getStatus()) && product.getStock() > 0);
        model.addAttribute("maxSelectableQuantity", Math.min(product.getStock(), 99));
    }

    /**
     * 商品詳細画面への戻り先かを判定します。
     *
     * @param redirectTo 戻り先パス
     * @return 商品詳細画面への戻り先であればtrue
     */
    public boolean isProductDetailPath(String redirectTo) {
        return redirectTo != null && redirectTo.matches("/products/\\d+");
    }

    /**
     * 商品詳細画面に戻る際に利用する商品IDを解決します。
     *
     * @param productId フォームに含まれる商品ID
     * @param redirectTo 戻り先パス
     * @return 商品ID。解決できない場合はnull
     */
    public Long resolveProductIdForDetail(Long productId, String redirectTo) {
        if (productId != null) {
            return productId;
        }
        if (!isProductDetailPath(redirectTo)) {
            return null;
        }
        return Long.valueOf(redirectTo.substring(redirectTo.lastIndexOf('/') + 1));
    }
}