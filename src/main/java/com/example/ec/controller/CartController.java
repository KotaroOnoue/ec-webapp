package com.example.ec.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.ec.controller.form.CartUpdateForm;
import com.example.ec.service.CartService;
import com.example.ec.service.model.CartSummaryModel;

import jakarta.validation.Valid;

/**
 * カート画面を制御するControllerです。
 */
@Controller
public class CartController {

    /** カートServiceです。 */
    @Autowired
    private CartService cartService;

    /**
     * カート画面を表示します。
     *
     * @param model 画面モデル
     * @return テンプレート名
     */
    @GetMapping("/cart")
    public String showCart(Model model) {
        prepareCartModel(model);
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
     * @param model 画面モデル
     * @return 遷移先テンプレート名
     */
    @PostMapping("/cart/items/{productId}/update")
    public String updateCartItem(
            @PathVariable("productId") Long productId,
            @Valid @ModelAttribute("cartUpdateForm") CartUpdateForm cartUpdateForm,
            BindingResult bindingResult,
            Model model) {
        cartUpdateForm.setProductId(productId);
        if (bindingResult.hasErrors()) {
            prepareCartModel(model);
            return "cart";
        }

        cartService.updateCartItem(productId, cartUpdateForm.getQuantity());
        return "redirect:/cart";
    }

    /**
     * カート内の商品を削除します。
     *
     * @param productId 商品ID
     * @return リダイレクト先
     */
    @PostMapping("/cart/items/{productId}/delete")
    public String deleteCartItem(@PathVariable("productId") Long productId) {
        cartService.deleteCartItem(productId);
        return "redirect:/cart";
    }

    /**
     * カート画面に必要な共通モデルを設定します。
     *
     * @param model 画面モデル
     */
    public void prepareCartModel(Model model) {
        CartSummaryModel cartSummary = cartService.getCartSummary();
        model.addAttribute("cartItems", cartSummary.getCartItems());
        model.addAttribute("cartItemCount", cartSummary.getTotalQuantity());
        model.addAttribute("totalAmount", cartSummary.getTotalAmount());
        model.addAttribute("shippingAmount", cartSummary.getShippingAmount());
        model.addAttribute("billingAmount", cartSummary.getBillingAmount());
        model.addAttribute("cart", cartSummary.getCartQuantities());
    }
}