package com.example.ec.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
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
 * カート画面のControllerを検証するテストです。
 */
@SpringBootTest
class CartControllerTest {

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
        jdbcTemplate.update("DELETE FROM cart_item");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (1, 2)");
        jdbcTemplate.update("INSERT INTO cart_item (product_id, quantity) VALUES (2, 1)");
    }

    /**
     * カート画面にカート内商品一覧と合計金額が表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showCartDisplaysCartItems() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(content().string(containsString("ワイヤレスイヤホン")))
                .andExpect(content().string(containsString("ゲーミングマウス")))
                .andExpect(content().string(containsString("¥15,940")))
                .andExpect(content().string(containsString("送料")))
                .andExpect(content().string(containsString("¥1,000")))
                .andExpect(content().string(containsString("¥16,940")));
    }

    /**
     * 数量更新後にカート画面へ戻り、DBの数量が更新されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void updateCartItemRedirectsAndStoresQuantity() throws Exception {
        mockMvc.perform(post("/cart/items/1/update")
                        .sessionAttr("cart", new LinkedHashMap<Long, Integer>())
                        .param("productId", "1")
                        .param("quantity", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        Integer quantity = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM cart_item WHERE product_id = ?",
                Integer.class,
                1L);
        assertEquals(5, quantity);
    }

    /**
     * 在庫超過の数量更新時はエラーメッセージ付きでカートへ戻ることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void updateCartItemRedirectsWithErrorWhenStockIsExceeded() throws Exception {
        mockMvc.perform(post("/cart/items/1/update")
                        .sessionAttr("cart", new LinkedHashMap<Long, Integer>())
                        .param("productId", "1")
                        .param("quantity", "11"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"))
                .andExpect(flash().attribute("errorMessage", "在庫数を超えるためカートに追加できません。"));
    }

    /**
     * 削除後にカート画面へ戻り、DBから削除されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void deleteCartItemRedirectsAndRemovesItem() throws Exception {
        mockMvc.perform(post("/cart/items/2/delete")
                        .sessionAttr("cart", new LinkedHashMap<Long, Integer>()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        Integer quantity = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM cart_item WHERE product_id = ?",
                Integer.class,
                2L);
        assertEquals(0, quantity);
    }
}