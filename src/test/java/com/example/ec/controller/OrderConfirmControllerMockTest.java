package com.example.ec.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.ec.controller.form.OrderForm;
import com.example.ec.exception.CartEmptyException;
import com.example.ec.service.CartService;
import com.example.ec.service.OrderService;
import com.example.ec.service.model.CartItemModel;
import com.example.ec.service.model.CartSummaryModel;

/**
 * 注文確認画面のControllerとThymeleaf表示をモックServiceで検証するテストです。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class OrderConfirmControllerMockTest {

    /** MockMvcです。 */
    private MockMvc mockMvc;

    /** WebApplicationContextです。 */
    @Autowired
    private WebApplicationContext webApplicationContext;

    /** カートServiceのモックです。 */
    @MockitoBean
    private CartService cartService;

    /** 注文Serviceのモックです。 */
    @MockitoBean
    private OrderService orderService;

    /**
     * テストごとにMockMvcを初期化します。
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 注文確認画面に主要な表示要素が描画されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderConfirmDisplaysPageTitleHeaderAndCartItems() throws Exception {
        stubFilledCart();

        mockMvc.perform(get("/orders/confirm"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("<title>注文確認</title>")))
                .andExpect(content().string(containsString("ECサイト")))
                .andExpect(content().string(containsString("商品一覧")))
                .andExpect(content().string(containsString("カート")))
                .andExpect(content().string(containsString("注文確認")))
                .andExpect(content().string(containsString("ワイヤレスイヤホン")))
                .andExpect(content().string(containsString("ゲーミングマウス")))
                .andExpect(content().string(containsString("¥5,980")))
                .andExpect(content().string(containsString("¥11,960")))
                .andExpect(content().string(containsString("¥15,940")));
    }

    /**
     * 注文確認画面にフォーム入力欄と操作要素が表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderConfirmDisplaysFormFieldsAndActions() throws Exception {
        stubFilledCart();

        mockMvc.perform(get("/orders/confirm"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("クーポン番号")))
                .andExpect(content().string(containsString("適用")))
                .andExpect(content().string(containsString("氏名")))
                .andExpect(content().string(containsString("郵便番号")))
                .andExpect(content().string(containsString("住所")))
                .andExpect(content().string(containsString("電話番号")))
                .andExpect(content().string(containsString("注文を確定する")))
                .andExpect(content().string(containsString("href=\"/cart\"")))
                .andExpect(content().string(containsString("カートへ戻る")));
    }

    /**
     * 注文確認画面に集計情報が表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderConfirmDisplaysSummaryValues() throws Exception {
        stubFilledCart();

        mockMvc.perform(get("/orders/confirm"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cartItemCount", 3))
                .andExpect(model().attribute("totalAmount", 15940))
                .andExpect(model().attribute("shippingAmount", 1000))
                .andExpect(model().attribute("discountAmount", 0))
                .andExpect(model().attribute("billingAmount", 16940))
                .andExpect(content().string(containsString("3点")))
                .andExpect(content().string(containsString("送料")))
                .andExpect(content().string(containsString("¥1,000")))
                .andExpect(content().string(containsString("クーポン割引")))
                .andExpect(content().string(containsString("¥0")))
                .andExpect(content().string(containsString("ご請求金額")))
                .andExpect(content().string(containsString("¥16,940")));
    }

    /**
     * 有効なクーポン番号指定時に割引後の金額が表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderConfirmAppliesDiscountWhenDiscountCodeIsValid() throws Exception {
        stubFilledCart();

        mockMvc.perform(get("/orders/confirm").param("discountCode", "1001"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("appliedDiscountCode", 1001L))
                .andExpect(model().attribute("discountAmount", 1694))
                .andExpect(model().attribute("billingAmount", 15246))
                .andExpect(content().string(containsString("適用中のクーポン番号: <span>1001</span>")))
                .andExpect(content().string(containsString("¥1,694")))
                .andExpect(content().string(containsString("¥15,246")));
    }

    /**
     * ヘッダーの現在位置が押下不可表示であることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderConfirmDisplaysCurrentNavAsDisabled() throws Exception {
        stubFilledCart();

        mockMvc.perform(get("/orders/confirm"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("nav-link-current")))
                .andExpect(content().string(containsString("aria-disabled=\"true\"")));
    }

    /**
     * 空カート時に空表示メッセージが描画されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderConfirmDisplaysEmptyMessageWhenCartIsEmpty() throws Exception {
        stubCartSummary(List.of(), 0, 0, 0, 0, 0, null);

        mockMvc.perform(get("/orders/confirm"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("カートに商品がありません。")))
                .andExpect(content().string(not(containsString("ワイヤレスイヤホン"))));
    }

    /**
     * 初回表示時に注文フォームが初期化されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderConfirmSetsInitialOrderForm() throws Exception {
        stubFilledCart();

        mockMvc.perform(get("/orders/confirm"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("orderForm"))
                .andExpect(model().attribute("orderForm", hasProperty("customerName", is((Object) null))));
    }

    /**
     * 有効なクーポン適用時に割引付きURLへリダイレクトすることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void applyDiscountCodeRedirectsToConfirmWhenCodeIsValid() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/orders/confirm/coupon")
                        .with(csrf())
                        .param("discountCode", "1001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/confirm?discountCode=1001"));
    }

    /**
     * 存在しないクーポン適用時は同画面でエラー表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void applyDiscountCodeReturnsOrderConfirmWhenCodeIsInvalid() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/orders/confirm/coupon")
                        .with(csrf())
                        .param("discountCode", "9999"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("クーポン番号が正しくありません。")));
    }

    /**
     * 正常な注文確定後に注文完了画面へリダイレクトすることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderRedirectsToCompleteWhenInputIsValid() throws Exception {
        when(orderService.placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class))).thenReturn(5L);

        MvcResult result = mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "山田 太郎")
                        .param("postalCode", "1500001")
                        .param("address", "東京都千代田区1-1-1")
                        .param("phoneNumber", "0312345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/complete/5"))
                .andReturn();

            assertTrue(result.getResponse().getRedirectedUrl().endsWith("/orders/complete/5"));
        verify(orderService, times(1)).placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class));
    }

    /**
     * 郵便番号7桁かつ電話番号10桁で正常処理に進むことを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderAcceptsBoundaryPostalCodeSevenDigitsAndPhoneTenDigits() throws Exception {
        when(orderService.placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class))).thenReturn(7L);

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "山田 太郎")
                        .param("postalCode", "1234567")
                        .param("address", "東京都千代田区1-1-1")
                        .param("phoneNumber", "0312345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/complete/7"));

        verify(orderService, times(1)).placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class));
    }

    /**
     * 電話番号11桁でも正常処理に進むことを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderAcceptsBoundaryPhoneElevenDigits() throws Exception {
        when(orderService.placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class))).thenReturn(8L);

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "山田 花子")
                        .param("postalCode", "7654321")
                        .param("address", "東京都新宿区1-2-3")
                        .param("phoneNumber", "09012345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/complete/8"));

        verify(orderService, times(1)).placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class));
    }

    /**
     * 氏名未入力時は注文確認画面を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderReturnsOrderConfirmWhenCustomerNameIsBlank() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "")
                        .param("postalCode", "1500001")
                        .param("address", "東京都千代田区1-1-1")
                        .param("phoneNumber", "0312345678"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("氏名を入力してください。")));

        verify(orderService, never()).placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class));
    }

    /**
     * 郵便番号未入力時は注文確認画面を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderReturnsOrderConfirmWhenPostalCodeIsBlank() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "山田 太郎")
                        .param("postalCode", "")
                        .param("address", "東京都千代田区1-1-1")
                        .param("phoneNumber", "0312345678"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("郵便番号を入力してください。")));

        verify(orderService, never()).placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class));
    }

    /**
     * 郵便番号6桁時は形式エラーになることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderReturnsOrderConfirmWhenPostalCodeHasSixDigits() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "山田 太郎")
                        .param("postalCode", "123456")
                        .param("address", "東京都千代田区1-1-1")
                        .param("phoneNumber", "0312345678"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("郵便番号はハイフンなし7桁の数字で入力してください。")));

        verify(orderService, never()).placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class));
    }

    /**
     * 郵便番号8桁時は形式エラーになることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderReturnsOrderConfirmWhenPostalCodeHasEightDigits() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "山田 太郎")
                        .param("postalCode", "12345678")
                        .param("address", "東京都千代田区1-1-1")
                        .param("phoneNumber", "0312345678"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("郵便番号はハイフンなし7桁の数字で入力してください。")));

        verify(orderService, never()).placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class));
    }

    /**
     * 住所未入力時は注文確認画面を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderReturnsOrderConfirmWhenAddressIsBlank() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "山田 太郎")
                        .param("postalCode", "1500001")
                        .param("address", "")
                        .param("phoneNumber", "0312345678"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("住所を入力してください。")));

        verify(orderService, never()).placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class));
    }

    /**
     * 電話番号9桁時は形式エラーになることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderReturnsOrderConfirmWhenPhoneNumberHasNineDigits() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "山田 太郎")
                        .param("postalCode", "1500001")
                        .param("address", "東京都千代田区1-1-1")
                        .param("phoneNumber", "031234567"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("電話番号は10桁または11桁の数字で入力してください。")));

        verify(orderService, never()).placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class));
    }

    /**
     * 電話番号12桁時は形式エラーになることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderReturnsOrderConfirmWhenPhoneNumberHasTwelveDigits() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "山田 太郎")
                        .param("postalCode", "1500001")
                        .param("address", "東京都千代田区1-1-1")
                        .param("phoneNumber", "031234567890"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("電話番号は10桁または11桁の数字で入力してください。")));

        verify(orderService, never()).placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class));
    }

    /**
     * 空カート例外時はエラーメッセージ付きで注文確認画面へ戻ることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderRedirectsToConfirmWithErrorWhenCartIsEmpty() throws Exception {
        when(orderService.placeOrder(org.mockito.ArgumentMatchers.any(OrderForm.class)))
            .thenThrow(new CartEmptyException("カートに商品がありません。"));

        mockMvc.perform(post("/orders")
                .with(csrf())
                .param("discountCode", "1001")
                        .param("customerName", "山田 太郎")
                        .param("postalCode", "1500001")
                        .param("address", "東京都千代田区1-1-1")
                        .param("phoneNumber", "0312345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/confirm"))
                .andExpect(flash().attribute("errorMessage", "カートに商品がありません。"));
    }

    /**
     * テスト用のカート内容をモックします。
     */
    private void stubFilledCart() {
        when(cartService.isDiscountCodeAvailable(1001L)).thenReturn(true);
        when(cartService.isDiscountCodeAvailable(9999L)).thenReturn(false);
        stubCartSummary(List.of(
            createCartItemModel(1L, "ワイヤレスイヤホン", 5980, 2),
            createCartItemModel(2L, "ゲーミングマウス", 3980, 1)), 3, 15940, 1000, 0, 16940, null);
        stubCartSummary(List.of(
            createCartItemModel(1L, "ワイヤレスイヤホン", 5980, 2),
            createCartItemModel(2L, "ゲーミングマウス", 3980, 1)), 3, 15940, 1000, 1694, 15246, 1001L);
    }

        /**
         * テスト用のカート集計をスタブします。
         *
         * @param cartItems カート商品
         * @param totalQuantity 合計数量
         * @param totalAmount 商品合計
         * @param shippingAmount 送料
         * @param discountAmount 割引額
         * @param billingAmount 請求金額
         * @param discountCode クーポン番号
         */
        private void stubCartSummary(List<CartItemModel> cartItems, int totalQuantity, int totalAmount, int shippingAmount,
            int discountAmount, int billingAmount, Long discountCode) {
        CartSummaryModel cartSummaryModel = new CartSummaryModel();
        Map<Long, Integer> cartQuantities = new LinkedHashMap<>();
        for (CartItemModel cartItem : cartItems) {
            cartQuantities.put(cartItem.getProductId(), cartItem.getQuantity());
        }
        cartSummaryModel.setCartItems(cartItems);
        cartSummaryModel.setCartQuantities(cartQuantities);
        cartSummaryModel.setTotalQuantity(totalQuantity);
        cartSummaryModel.setTotalAmount(totalAmount);
        cartSummaryModel.setShippingAmount(shippingAmount);
        cartSummaryModel.setDiscountAmount(discountAmount);
        cartSummaryModel.setBillingAmount(billingAmount);
        when(cartService.getCartSummary(discountCode)).thenReturn(cartSummaryModel);
        }

    /**
     * テスト用のカート商品モデルを生成します。
     *
     * @param productId 商品ID
     * @param name 商品名
     * @param price 単価
     * @param quantity 数量
     * @return カート商品モデル
     */
    private CartItemModel createCartItemModel(Long productId, String name, Integer price, Integer quantity) {
        CartItemModel cartItemModel = new CartItemModel();
        cartItemModel.setProductId(productId);
        cartItemModel.setName(name);
        cartItemModel.setPrice(price);
        cartItemModel.setQuantity(quantity);
        cartItemModel.setSubtotal(price * quantity);
        return cartItemModel;
    }
}