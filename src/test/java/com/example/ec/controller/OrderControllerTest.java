package com.example.ec.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * OrderControllerの表示と注文確定を検証するテストです。
 */
@SpringBootTest
class OrderControllerTest {

    /** MockMvcです。 */
    private MockMvc mockMvc;

    /** WebApplicationContextです。 */
    @Autowired
    private WebApplicationContext webApplicationContext;

    /** テスト用JDBC操作です。 */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * テストごとに初期化します。
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM cart_item");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (1, 2)");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (2, 1)");
    }

    /**
     * 注文確認画面にカート内容と入力欄が表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showOrderConfirmDisplaysCartItems() throws Exception {
        mockMvc.perform(get("/orders/confirm"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("ワイヤレスイヤホン")))
                .andExpect(content().string(containsString("ゲーミングマウス")))
                .andExpect(content().string(containsString("クーポン番号")))
                .andExpect(content().string(containsString("適用")))
                .andExpect(content().string(containsString("送料")))
                .andExpect(content().string(containsString("¥1,000")))
                .andExpect(content().string(containsString("¥16,940")))
                .andExpect(content().string(containsString("注文を確定する")));
    }

        /**
         * 注文完了画面に注文番号が表示されることを検証します。
         *
         * @throws Exception テスト失敗時
         */
        @Test
        void showOrderCompleteDisplaysOrderId() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO orders (order_id, customer_name, postal_code, adress, phone_number, total_amount, order_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
            10L,
            "山田 太郎",
            "1500001",
            "東京都千代田区1-1-1",
            "0312345678",
            12000);

        mockMvc.perform(get("/orders/complete/10"))
            .andExpect(status().isOk())
            .andExpect(view().name("order-complete"))
            .andExpect(content().string(containsString("注文完了")))
            .andExpect(content().string(containsString("10")))
            .andExpect(content().string(containsString("商品一覧に戻る")));
        }

    /**
     * 注文確定時にordersへ保存し、注文完了画面へリダイレクトすることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderRedirectsToCompleteAndStoresOrder() throws Exception {
        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "山田 太郎")
                        .param("postalCode", "1500001")
                        .param("address", "東京都千代田区1-1-1")
                        .param("phoneNumber", "0312345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/complete/1"));

        Integer orderCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        Integer totalAmount = jdbcTemplate.queryForObject("SELECT total_amount FROM orders WHERE order_id = ?", Integer.class, 1L);
        assertEquals(1, orderCount);
        assertEquals(15246, totalAmount);
    }

    /**
     * 入力不足時は注文確認画面を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void placeOrderReturnsOrderConfirmWhenValidationFails() throws Exception {
        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("discountCode", "1001")
                        .param("customerName", "")
                        .param("postalCode", "")
                        .param("address", "")
                        .param("phoneNumber", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirm"))
                .andExpect(content().string(containsString("氏名を入力してください。")));
    }
}