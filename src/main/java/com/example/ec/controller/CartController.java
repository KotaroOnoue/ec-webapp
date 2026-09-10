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
import org.springframework.web.bind.annotation.SessionAttributes;

import com.example.ec.controller.form.CartUpdateForm;
import com.example.ec.service.CartService;

import jakarta.validation.Valid;

/**
 * カート画面を制御するControllerです。
 */
@Controller
@SessionAttributes("cart")
public class CartController {

    /** カートServiceです。 */
    @Autowired
    private CartService cartService;

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
     * カート画面を表示します。
     *
     * @param cart セッション上のカート
     * @param model 画面モデル
     * @return テンプレート名
     */
    @GetMapping("/cart")
    public String showCart(@ModelAttribute("cart") Map<Long, Integer> cart, Model model) {
        prepareCartModel(cart, model);
        if (!model.containsAttribute("cartUpdateForm")) {
            model.addAttribute("cartUpdateForm", new CartUpdateForm());
        }
        return "cart";
    }

    /**
     * カート内商品の数量を更新します。
     *
     * @param productId 商品ID
     * @param cartUpdateForm 数量更新フォーム
     * @param bindingResult バリデーション結果
     * @param cart セッション上のカート
     * @param model 画面モデル
     * @return 遷移先テンプレート名
     */
    @PostMapping("/cart/items/{productId}/update")
    public String updateCartItem(
            @PathVariable("productId") Long productId,
            @Valid @ModelAttribute("cartUpdateForm") CartUpdateForm cartUpdateForm,
            BindingResult bindingResult,
            @ModelAttribute("cart") Map<Long, Integer> cart,
            Model model) {
        cartUpdateForm.setProductId(productId);
        if (bindingResult.hasErrors()) {
            prepareCartModel(cart, model);
            return "cart";
        }

        cartService.updateCartItem(productId, cartUpdateForm.getQuantity());
        if (cartUpdateForm.getQuantity() != null) {
            cart.put(productId, cartUpdateForm.getQuantity());
        }
        return "redirect:/cart";
    }

    /**
     * カート内の商品を削除します。
     *
     * @param productId 商品ID
     * @param cart セッション上のカート
     * @return リダイレクト先
     */
    @PostMapping("/cart/items/{productId}/delete")
    public String deleteCartItem(
            @PathVariable("productId") Long productId,
            @ModelAttribute("cart") Map<Long, Integer> cart) {
        cartService.deleteCartItem(productId);
        cart.remove(productId);
        return "redirect:/cart";
    }

    /**
     * カート画面に必要な共通モデルを設定します。
     *
     * @param cart セッション上のカート
     * @param model 画面モデル
     */
    public void prepareCartModel(Map<Long, Integer> cart, Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("cartItemCount", cartService.getTotalQuantity());
        model.addAttribute("totalAmount", cartService.getTotalAmount());
        model.addAttribute("cart", cart);
    }
}