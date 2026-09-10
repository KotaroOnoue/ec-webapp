package com.example.ec.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.example.ec.controller.form.CartAddForm;
import com.example.ec.service.ProductService;
import com.example.ec.service.model.ProductModel;

import jakarta.validation.Valid;

/**
 * 商品一覧画面を制御するControllerです。
 */
@Controller
@SessionAttributes("cart")
public class ProductController {

    /** 商品Serviceです。 */
    @Autowired
    private ProductService productService;

    /**
     * セッション上のカート情報を初期化します。
     *
     * @return カート情報
     */
    @ModelAttribute("cart")
    public Map<Long, Integer> createCart() {
        return new LinkedHashMap<>();
    }

    /**
     * 商品一覧画面を表示します。
     *
     * @param cart セッション上のカート
     * @param model 画面モデル
     * @return テンプレート名
     */
    @GetMapping("/products")
    public String showProducts(@ModelAttribute("cart") Map<Long, Integer> cart, Model model) {
        preparePageModel(cart, model);
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
     * @param cart セッション上のカート
     * @param model 画面モデル
     * @return テンプレート名
     */
    @GetMapping("/products/{productId}")
    public String showProductDetail(
            @PathVariable("productId") Long productId,
            @ModelAttribute("cart") Map<Long, Integer> cart,
            Model model) {
        ProductModel product = productService.getProductById(productId);
        prepareProductDetailModel(product, cart, model);
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
     * @param cart セッション上のカート
     * @param model 画面モデル
     * @return 遷移先テンプレート名
     */
    @PostMapping("/cart/items")
    public String addToCart(
            @Valid @ModelAttribute("cartAddForm") CartAddForm cartAddForm,
            BindingResult bindingResult,
            @RequestParam(name = "redirectTo", defaultValue = "/products") String redirectTo,
            @ModelAttribute("cart") Map<Long, Integer> cart,
            Model model) {
        if (bindingResult.hasErrors()) {
            if (isProductDetailPath(redirectTo)) {
                ProductModel product = productService.getProductById(cartAddForm.getProductId());
                prepareProductDetailModel(product, cart, model);
                return "products-detail";
            }
            preparePageModel(cart, model);
            return "products";
        }

        int currentQuantity = cart.getOrDefault(cartAddForm.getProductId(), 0);
        productService.validateAddToCart(cartAddForm.getProductId(), currentQuantity, cartAddForm.getQuantity());
        productService.addCartItem(cartAddForm.getProductId(), cartAddForm.getQuantity());
        cart.put(cartAddForm.getProductId(), currentQuantity + cartAddForm.getQuantity());

        return "redirect:" + redirectTo;
    }

    /**
     * 商品一覧画面に必要な共通モデルを設定します。
     *
     * @param cart セッション上のカート
     * @param model 画面モデル
     */
    public void preparePageModel(Map<Long, Integer> cart, Model model) {
        model.addAttribute("products", productService.getOnSaleProducts());
        model.addAttribute("cartItemCount", cart.values().stream().mapToInt(Integer::intValue).sum());
    }

    /**
     * 商品詳細画面に必要な共通モデルを設定します。
     *
     * @param product 商品詳細
     * @param cart セッション上のカート
     * @param model 画面モデル
     */
    public void prepareProductDetailModel(ProductModel product, Map<Long, Integer> cart, Model model) {
        model.addAttribute("product", product);
        model.addAttribute("cartItemCount", cart.values().stream().mapToInt(Integer::intValue).sum());
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
}