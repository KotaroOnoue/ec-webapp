package com.example.ec.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.ec.service.CartService;
import com.example.ec.service.OrderService;
import com.example.ec.service.model.OrderCompleteModel;

/**
 * 注文完了画面のControllerとThymeleaf表示をモックServiceで検証するテストです。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class OrderCompleteControllerMockTest {

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
     * 注文完了画面に主要な表示要素が描画されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderCompleteDisplaysPageTitleHeaderMessageAndOrderId() throws Exception {
        when(orderService.getOrderComplete(10L)).thenReturn(createOrderCompleteModel(10L));

        mockMvc.perform(get("/orders/complete/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-complete"))
                .andExpect(content().string(containsString("<title>注文完了</title>")))
                .andExpect(content().string(containsString("ECサイト")))
                .andExpect(content().string(containsString("商品一覧")))
                .andExpect(content().string(containsString("カート")))
                .andExpect(content().string(containsString("注文完了")))
                .andExpect(content().string(containsString("ご注文ありがとうございました。注文内容を受け付けました。")))
                .andExpect(content().string(containsString("10")));
    }

    /**
     * ヘッダーの現在位置と商品一覧導線が表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderCompleteDisplaysCurrentNavAndReturnLink() throws Exception {
        when(orderService.getOrderComplete(10L)).thenReturn(createOrderCompleteModel(10L));

        mockMvc.perform(get("/orders/complete/10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("nav-link-current")))
                .andExpect(content().string(containsString("aria-disabled=\"true\"")))
                .andExpect(content().string(containsString("href=\"/products\"")))
                .andExpect(content().string(containsString("商品一覧に戻る")));
    }

    /**
     * 注文完了画面用モデルが設定されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderCompleteSetsOrderModel() throws Exception {
        when(orderService.getOrderComplete(10L)).thenReturn(createOrderCompleteModel(10L));

        mockMvc.perform(get("/orders/complete/10"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("order", hasProperty("orderId", is(10L))));
    }

    /**
     * 存在しない注文IDでは商品一覧へリダイレクトすることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderCompleteRedirectsToProductsWhenOrderDoesNotExist() throws Exception {
        when(orderService.getOrderComplete(999L)).thenReturn(null);

        mockMvc.perform(get("/orders/complete/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    /**
     * 最小境界の注文IDでも正常表示できることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderCompleteDisplaysMinimumBoundaryOrderId() throws Exception {
        when(orderService.getOrderComplete(1L)).thenReturn(createOrderCompleteModel(1L));

        mockMvc.perform(get("/orders/complete/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-complete"))
                .andExpect(model().attribute("order", hasProperty("orderId", is(1L))))
                .andExpect(content().string(containsString("1")));
    }

    /**
     * 数値以外の注文IDではバッドリクエストになることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderCompleteReturnsBadRequestWhenOrderIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/orders/complete/abc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    /**
     * パス変数の注文IDがServiceへ渡されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderCompletePassesOrderIdToService() throws Exception {
        when(orderService.getOrderComplete(10L)).thenReturn(createOrderCompleteModel(10L));

        mockMvc.perform(get("/orders/complete/10"))
                .andExpect(status().isOk());

        verify(orderService, times(1)).getOrderComplete(10L);
    }

    /**
     * テスト用の注文完了モデルを生成します。
     *
     * @param orderId 注文ID
     * @return 注文完了モデル
     */
    private OrderCompleteModel createOrderCompleteModel(Long orderId) {
        OrderCompleteModel orderCompleteModel = new OrderCompleteModel();
        orderCompleteModel.setOrderId(orderId);
        return orderCompleteModel;
    }
}