package com.example.ec.controller;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.example.ec.controller.form.CouponApplyForm;
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

    /** メッセージ解決用コンポーネントです。 */
    @Autowired
    private MessageSource messageSource;

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
    public String showOrderConfirm(
            @RequestParam(value = "discountCode", required = false) Long discountCode,
            @ModelAttribute("cart") Map<Long, Integer> cart,
            Model model) {
        Long appliedDiscountCode = prepareOrderConfirmModel(model, discountCode);
        ensureOrderForm(model, appliedDiscountCode);
        ensureCouponApplyForm(model, appliedDiscountCode);
        model.addAttribute("cart", cart);
        return "order-confirm";
    }

    /**
     * クーポン番号を適用します。
     *
     * @param couponApplyForm クーポン適用フォーム
     * @param bindingResult バリデーション結果
     * @param cart セッション上のカート
     * @param model 画面モデル
     * @return 遷移先テンプレート名またはリダイレクト先
     */
    @PostMapping("/orders/confirm/coupon")
    public String applyDiscountCode(
            @Valid @ModelAttribute("couponApplyForm") CouponApplyForm couponApplyForm,
            BindingResult bindingResult,
            @ModelAttribute("cart") Map<Long, Integer> cart,
            Model model) {
        if (bindingResult.hasErrors()) {
            prepareOrderConfirmModel(model, null);
            ensureOrderForm(model, null);
            model.addAttribute("cart", cart);
            return "order-confirm";
        }

        Long discountCode = Long.valueOf(couponApplyForm.getDiscountCode());
        if (!cartService.isDiscountCodeAvailable(discountCode)) {
            prepareOrderConfirmModel(model, null);
            ensureOrderForm(model, null);
            model.addAttribute("cart", cart);
            model.addAttribute("errorMessage", messageSource.getMessage("error.discountCode.invalid", null, Locale.getDefault()));
            return "order-confirm";
        }

        return "redirect:/orders/confirm?discountCode=" + discountCode;
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
            prepareOrderConfirmModel(model, orderForm.getDiscountCode());
            ensureCouponApplyForm(model, orderForm.getDiscountCode());
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
     * @param discountCode クーポン番号
     * @return 適用したクーポン番号。無効な場合はnull
     */
    public Long prepareOrderConfirmModel(Model model, Long discountCode) {
        Long appliedDiscountCode = cartService.isDiscountCodeAvailable(discountCode) ? discountCode : null;
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("cartItemCount", cartService.getTotalQuantity());
        model.addAttribute("totalAmount", cartService.getTotalAmount());
        model.addAttribute("shippingAmount", cartService.getShippingAmount());
        model.addAttribute("discountAmount",
                appliedDiscountCode == null ? 0 : cartService.getDiscountAmount(appliedDiscountCode));
        model.addAttribute("billingAmount",
                appliedDiscountCode == null ? cartService.getBillingAmount()
                        : cartService.getDiscountedBillingAmount(appliedDiscountCode));
        model.addAttribute("appliedDiscountCode", appliedDiscountCode);
        return appliedDiscountCode;
    }

    /**
     * 注文フォームを必要に応じて初期化します。
     *
     * @param model 画面モデル
     * @param discountCode 適用済みクーポン番号
     */
    private void ensureOrderForm(Model model, Long discountCode) {
        if (!model.containsAttribute("orderForm")) {
            OrderForm orderForm = new OrderForm();
            orderForm.setDiscountCode(discountCode);
            model.addAttribute("orderForm", orderForm);
            return;
        }

        Object orderFormObject = model.getAttribute("orderForm");
        if (orderFormObject instanceof OrderForm orderForm && orderForm.getDiscountCode() == null) {
            orderForm.setDiscountCode(discountCode);
        }
    }

    /**
     * クーポン適用フォームを必要に応じて初期化します。
     *
     * @param model 画面モデル
     * @param discountCode 適用済みクーポン番号
     */
    private void ensureCouponApplyForm(Model model, Long discountCode) {
        if (model.containsAttribute("couponApplyForm")) {
            return;
        }

        CouponApplyForm couponApplyForm = new CouponApplyForm();
        if (discountCode != null) {
            couponApplyForm.setDiscountCode(String.valueOf(discountCode));
        }
        model.addAttribute("couponApplyForm", couponApplyForm);
    }
}