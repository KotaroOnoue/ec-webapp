package com.example.ec.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 商品一覧画面のControllerを検証するテストです。
 */
@SpringBootTest
class ProductControllerTest {

    /** MockMvcです。 */
    private MockMvc mockMvc;

    /** WebApplicationContextです。 */
    @Autowired
    private WebApplicationContext webApplicationContext;

    /**
     * テストごとにMockMvcを初期化します。
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 商品一覧画面に販売中の商品だけが表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductsDisplaysOnSaleProducts() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(content().string(containsString("ワイヤレスイヤホン")))
                .andExpect(content().string(containsString("ゲーミングマウス")))
                .andExpect(content().string(containsString("USB-Cハブ")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Webカメラ"))));
    }

    /**
     * 在庫数を超える追加時にエラーメッセージ付きで一覧へ戻ることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartRedirectsWithErrorWhenStockIsExceeded() throws Exception {
        Map<Long, Integer> cart = new LinkedHashMap<>();
        cart.put(1L, 10);

        mockMvc.perform(post("/products/cart")
                        .sessionAttr("cart", cart)
                        .param("productId", "1")
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attribute("errorMessage", "在庫数を超えるためカートに追加できません。"));
    }

    /**
     * 正常にカート追加できると商品一覧へリダイレクトすることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartRedirectsWhenAdditionSucceeds() throws Exception {
        mockMvc.perform(post("/products/cart")
                        .sessionAttr("cart", new LinkedHashMap<Long, Integer>())
                        .param("productId", "1")
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }
}