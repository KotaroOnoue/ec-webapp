package com.example.ec.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.jdbc.core.JdbcTemplate;
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

    /** テスト用JDBC操作です。 */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * テストごとにMockMvcを初期化します。
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        jdbcTemplate.update("DELETE FROM cart_item");
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

    /**
     * 商品詳細画面が表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductDetailDisplaysProduct() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products-detail"))
                .andExpect(model().attributeExists("product"))
                .andExpect(content().string(containsString("ワイヤレスイヤホン")))
                .andExpect(content().string(containsString("商品一覧へ戻る")));
    }

    /**
     * 商品詳細画面からカート追加するとcart_itemへ数量が保存されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartFromDetailRedirectsAndStoresQuantity() throws Exception {
        mockMvc.perform(post("/products/2/cart")
                        .sessionAttr("cart", new LinkedHashMap<Long, Integer>())
                        .param("productId", "2")
                        .param("quantity", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/2"));

        Integer quantity = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM cart_item WHERE product_id = ?",
                Integer.class,
                2L);
        assertEquals(3, quantity);
    }
}