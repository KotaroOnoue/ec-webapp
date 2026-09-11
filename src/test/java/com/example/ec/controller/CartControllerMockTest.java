package com.example.ec.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.ec.exception.InsufficientStockException;
import com.example.ec.service.CartService;
import com.example.ec.service.model.CartItemModel;
import com.example.ec.service.model.CartSummaryModel;

/**
 * カート画面のControllerとThymeleaf表示をモックServiceで検証するテストです。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CartControllerMockTest {

    /** MockMvcです。 */
    private MockMvc mockMvc;

    /** WebApplicationContextです。 */
    @Autowired
    private WebApplicationContext webApplicationContext;

    /** カートServiceのモックです。 */
    @MockitoBean
    private CartService cartService;

    /**
     * テストごとにMockMvcを初期化します。
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * カート画面に主要な表示要素が描画されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showCartDisplaysPageTitleHeaderAndCartItems() throws Exception {
        stubFilledCart();

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(content().string(containsString("<title>カート</title>")))
                .andExpect(content().string(containsString("ECサイト")))
                .andExpect(content().string(containsString("商品一覧")))
                .andExpect(content().string(containsString("カート")))
                .andExpect(content().string(containsString("ワイヤレスイヤホン")))
                .andExpect(content().string(containsString("ゲーミングマウス")))
                .andExpect(content().string(containsString("¥5,980")))
                .andExpect(content().string(containsString("¥11,960")))
                .andExpect(content().string(containsString("¥15,940")))
                .andExpect(content().string(containsString("数量更新")))
                .andExpect(content().string(containsString("削除")));
    }

    /**
     * カートヘッダーの現在位置が押下不可表示であることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showCartDisplaysCurrentCartLinkAsDisabled() throws Exception {
        stubFilledCart();

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("nav-link-current")))
                .andExpect(content().string(containsString("aria-disabled=\"true\"")));
    }

    /**
     * カート集計と遷移リンクが表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showCartDisplaysSummaryAndNavigationLinks() throws Exception {
        stubFilledCart();

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cartItemCount", 3))
                .andExpect(model().attribute("totalAmount", 15940))
                .andExpect(model().attribute("shippingAmount", 1000))
                .andExpect(model().attribute("billingAmount", 16940))
                .andExpect(content().string(containsString("3点")))
                .andExpect(content().string(containsString("送料")))
                .andExpect(content().string(containsString("¥1,000")))
                .andExpect(content().string(containsString("¥16,940")))
                .andExpect(content().string(containsString("href=\"/products\"")))
                .andExpect(content().string(containsString("買い物を続ける")))
                .andExpect(content().string(containsString("href=\"/orders/confirm\"")))
                .andExpect(content().string(containsString("注文手続きへ")));
    }

    /**
     * 空カート時に空メッセージが表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showCartDisplaysEmptyMessageWhenCartIsEmpty() throws Exception {
        stubCartSummary(List.of(), 0, 0, 0, 0);

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(content().string(containsString("カートに商品がありません")))
                .andExpect(content().string(not(containsString("ワイヤレスイヤホン"))));
    }

    /**
     * 初回表示時に数量更新フォームが設定されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showCartSetsInitialCartUpdateForm() throws Exception {
        stubFilledCart();

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("cartUpdateForm"))
                .andExpect(model().attribute("cartUpdateForm", hasProperty("productId", is((Object) null))));
    }

    /**
     * 正常な数量更新後にカートへリダイレクトすることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void updateCartItemRedirectsWhenUpdateSucceeds() throws Exception {
        mockMvc.perform(post("/cart/items/1/update")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService, times(1)).updateCartItem(1L, 5);
    }

    /**
     * 数量更新時はパス変数の商品IDが優先されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void updateCartItemUsesPathVariableProductId() throws Exception {
        mockMvc.perform(post("/cart/items/1/update")
                        .with(csrf())
                        .param("productId", "2")
                        .param("quantity", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService, times(1)).updateCartItem(1L, 3);
    }

    /**
     * 在庫超過時にエラーメッセージ付きでカートへ戻ることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void updateCartItemRedirectsWithErrorWhenStockIsExceeded() throws Exception {
        doThrow(new InsufficientStockException("指定した数量は在庫数を超えています。"))
                .when(cartService).updateCartItem(1L, 11);

        mockMvc.perform(post("/cart/items/1/update")
                .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "11"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"))
                .andExpect(flash().attribute("errorMessage", "在庫数を超えるためカートに追加できません。"));
    }

    /**
     * 数量未指定時はカート画面を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void updateCartItemReturnsCartWhenQuantityIsMissing() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/cart/items/1/update")
                        .with(csrf())
                        .param("productId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(content().string(containsString("数量を指定してください。")));

        verify(cartService, never()).updateCartItem(1L, null);
    }

    /**
     * 数量0入力時はカート画面を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void updateCartItemReturnsCartWhenQuantityIsZero() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/cart/items/1/update")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(content().string(containsString("数量は1以上を指定してください。")));

        verify(cartService, never()).updateCartItem(1L, 0);
    }

    /**
     * 数量100入力時はカート画面を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void updateCartItemReturnsCartWhenQuantityExceedsMaximum() throws Exception {
        stubFilledCart();

        mockMvc.perform(post("/cart/items/1/update")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(content().string(containsString("数量は99以下を指定してください。")));

        verify(cartService, never()).updateCartItem(1L, 100);
    }

    /**
     * 削除後にカートへリダイレクトすることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void deleteCartItemRedirectsWhenDeletionSucceeds() throws Exception {
        mockMvc.perform(post("/cart/items/2/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService, times(1)).deleteCartItem(2L);
    }

    /**
     * 存在しない商品削除でもカートへ戻ることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void deleteCartItemRedirectsWhenTargetDoesNotExist() throws Exception {
        mockMvc.perform(post("/cart/items/999/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService, times(1)).deleteCartItem(999L);
    }

    /**
     * カートに商品がある場合の戻り値をスタブします。
     */
    private void stubFilledCart() {
        stubCartSummary(List.of(
                createCartItemModel(1L, "ワイヤレスイヤホン", 5980, 2, 11960),
                createCartItemModel(2L, "ゲーミングマウス", 3980, 1, 3980)), 3, 15940, 1000, 16940);
    }

    /**
     * テスト用のカート集計をスタブします。
     *
     * @param cartItems カート商品一覧
     * @param totalQuantity 合計数量
     * @param totalAmount 商品合計
     * @param shippingAmount 送料
     * @param billingAmount 請求金額
     */
    private void stubCartSummary(List<CartItemModel> cartItems, int totalQuantity, int totalAmount, int shippingAmount,
            int billingAmount) {
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
        cartSummaryModel.setDiscountAmount(0);
        cartSummaryModel.setBillingAmount(billingAmount);
        when(cartService.getCartSummary()).thenReturn(cartSummaryModel);
    }

    /**
     * テスト用のカート商品モデルを生成します。
     *
     * @param productId 商品ID
     * @param name 商品名
     * @param price 単価
     * @param quantity 数量
     * @param subtotal 小計
     * @return カート商品モデル
     */
    private CartItemModel createCartItemModel(Long productId, String name, Integer price, Integer quantity,
            Integer subtotal) {
        CartItemModel cartItemModel = new CartItemModel();
        cartItemModel.setProductId(productId);
        cartItemModel.setName(name);
        cartItemModel.setPrice(price);
        cartItemModel.setQuantity(quantity);
        cartItemModel.setSubtotal(subtotal);
        return cartItemModel;
    }
}