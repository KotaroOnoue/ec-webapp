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

import com.example.ec.controller.form.OrderForm;
import com.example.ec.service.CartService;
import com.example.ec.service.OrderService;

import jakarta.validation.Valid;

/**
 * 注文確認画面を制御するControllerです。
 */
@Controller
@SessionAttributes("cart")
public class OrderController {

    /** カートServiceです。 */
    @Autowired
    private CartService cartService;

    /** 注文Serviceです。 */
    @Autowired
    private OrderService orderService;

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
     * 注文確認画面を表示します。
     *
     * @param cart セッション上のカート
     * @param model 画面モデル
     * @return テンプレート名
     */
    @GetMapping("/orders/confirm")
    public String showOrderConfirm(@ModelAttribute("cart") Map<Long, Integer> cart, Model model) {
        prepareOrderConfirmModel(model);
        if (!model.containsAttribute("orderForm")) {
            model.addAttribute("orderForm", new OrderForm());
        }
        model.addAttribute("cart", cart);
        return "order-confirm";
    }

    /**
     * 注文完了画面を表示します。
     *
     * @param orderId 注文ID
     * @param model 画面モデル
     * @return テンプレート名またはリダイレクト先
     */
    @GetMapping("/orders/complete/{orderId}")
    public String showOrderComplete(@PathVariable("orderId") Long orderId, Model model) {
        var order = orderService.getOrderComplete(orderId);
        if (order == null) {
            return "redirect:/products";
        }
        model.addAttribute("order", order);
        return "order-complete";
    }

    /**
     * 注文を確定します。
     *
     * @param orderForm 注文フォーム
     * @param bindingResult バリデーション結果
     * @param cart セッション上のカート
     * @param model 画面モデル
     * @return リダイレクト先またはテンプレート名
     */
    @PostMapping("/orders")
    public String placeOrder(
            @Valid @ModelAttribute("orderForm") OrderForm orderForm,
            BindingResult bindingResult,
            @ModelAttribute("cart") Map<Long, Integer> cart,
            Model model) {
        if (bindingResult.hasErrors()) {
            prepareOrderConfirmModel(model);
            model.addAttribute("cart", cart);
            return "order-confirm";
        }

        Long orderId = orderService.placeOrder(orderForm);
        cart.clear();
        return "redirect:/orders/complete/" + orderId;
    }

    /**
     * 注文確認画面に必要な共通モデルを設定します。
     *
     * @param model 画面モデル
     */
    public void prepareOrderConfirmModel(Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("cartItemCount", cartService.getTotalQuantity());
        model.addAttribute("totalAmount", cartService.getTotalAmount());
    }
}