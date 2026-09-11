package com.example.ec.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.example.ec.exception.InsufficientStockException;
import com.example.ec.exception.ProductUnavailableException;
import com.example.ec.service.ProductService;
import com.example.ec.service.model.ProductModel;

/**
 * 商品一覧画面のControllerとThymeleaf表示をモックServiceで検証するテストです。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProductControllerMockTest {

    /** MockMvcです。 */
    private MockMvc mockMvc;

    /** WebApplicationContextです。 */
    @Autowired
    private WebApplicationContext webApplicationContext;

    /** 商品Serviceのモックです。 */
    @MockitoBean
    private ProductService productService;

    /**
     * テストごとにMockMvcを初期化します。
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 商品一覧画面に主要な表示要素が描画されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductsDisplaysPageTitleHeaderAndProductCards() throws Exception {
        when(productService.getOnSaleProducts()).thenReturn(createProductList());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(content().string(containsString("<title>商品一覧</title>")))
                .andExpect(content().string(containsString("ECサイト")))
                .andExpect(content().string(containsString("カートを見る")))
                .andExpect(content().string(containsString("ワイヤレスイヤホン")))
                .andExpect(content().string(containsString("ゲーミングマウス")))
                .andExpect(content().string(containsString("USB-Cハブ")))
                .andExpect(content().string(containsString("¥5,980")))
                .andExpect(content().string(containsString("/images/products/1.png")))
                .andExpect(content().string(containsString("href=\"/products/1\"")))
                .andExpect(content().string(containsString("href=\"/cart\"")))
                .andExpect(content().string(containsString("詳細を見る")))
                .andExpect(content().string(containsString("カートに追加")))
                .andExpect(content().string(containsString("在庫あり")))
                .andExpect(content().string(containsString("在庫なし")));
    }

    /**
     * 商品一覧画面で販売停止商品が表示されないことを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductsDoesNotDisplayStoppedProduct() throws Exception {
        when(productService.getOnSaleProducts()).thenReturn(createProductList());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Webカメラ"))));
    }

    /**
     * 在庫0の商品ではカート追加ボタンが押下不可として描画されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductsDisplaysDisabledCartButtonForOutOfStockProduct() throws Exception {
        when(productService.getOnSaleProducts()).thenReturn(createProductList());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("USB-Cハブ")))
                .andExpect(content().string(containsString("disabled=\"disabled\"")));
    }

    /**
     * セッション上のカート数量合計がヘッダーに表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductsDisplaysCartItemCountFromSessionCart() throws Exception {
        when(productService.getOnSaleProducts()).thenReturn(createProductList());
        Map<Long, Integer> cart = new LinkedHashMap<>();
        cart.put(1L, 2);
        cart.put(2L, 3);

        mockMvc.perform(get("/products").sessionAttr("cart", cart))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cartItemCount", 5))
                .andExpect(content().string(containsString("cart-count\">5</span>")));
    }

    /**
     * 初回表示時にカート追加フォームの初期数量が1で設定されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductsSetsInitialCartAddForm() throws Exception {
        when(productService.getOnSaleProducts()).thenReturn(createProductList());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("cartAddForm"))
                .andExpect(model().attribute("cartAddForm", hasProperty("quantity", is(1))));
    }

    /**
     * 正常にカート追加できた場合に商品一覧へリダイレクトすることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartRedirectsWhenAdditionSucceeds() throws Exception {
        mockMvc.perform(post("/cart/items")
                        .sessionAttr("cart", new LinkedHashMap<Long, Integer>())
                        .param("productId", "1")
                        .param("quantity", "1")
                        .param("redirectTo", "/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        verify(productService, times(1)).validateAddToCart(1L, 0, 1);
        verify(productService, times(1)).addCartItem(1L, 1);
    }

    /**
     * 在庫超過時にエラーメッセージ付きで商品一覧へリダイレクトすることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartRedirectsWithErrorWhenStockIsExceeded() throws Exception {
        doThrow(new InsufficientStockException("指定した数量は在庫数を超えています。"))
                .when(productService).validateAddToCart(1L, 10, 1);
        Map<Long, Integer> cart = new LinkedHashMap<>();
        cart.put(1L, 10);

        mockMvc.perform(post("/cart/items")
                        .sessionAttr("cart", cart)
                        .param("productId", "1")
                        .param("quantity", "1")
                        .param("redirectTo", "/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attribute("errorMessage", "在庫数を超えるためカートに追加できません。"));
    }

    /**
     * 販売停止商品を追加しようとした場合に商品利用不可メッセージで戻ることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartRedirectsWithErrorWhenProductIsUnavailable() throws Exception {
        doThrow(new ProductUnavailableException("指定した商品は販売中ではありません。"))
                .when(productService).validateAddToCart(4L, 0, 1);

        mockMvc.perform(post("/cart/items")
                        .sessionAttr("cart", new LinkedHashMap<Long, Integer>())
                        .param("productId", "4")
                        .param("quantity", "1")
                        .param("redirectTo", "/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attribute("errorMessage", "指定した商品は販売中ではありません。"));
    }

    /**
     * 数量未指定時はバリデーションエラーで商品一覧を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartReturnsProductsWhenQuantityIsMissing() throws Exception {
        when(productService.getOnSaleProducts()).thenReturn(createProductList());

        mockMvc.perform(post("/cart/items")
                        .sessionAttr("cart", new LinkedHashMap<Long, Integer>())
                        .param("productId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(content().string(containsString("数量を指定してください。")));

        verifyNoInteractionsOnAddFlow();
    }

    /**
     * 数量0入力時はバリデーションエラーで商品一覧を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartReturnsProductsWhenQuantityIsZero() throws Exception {
        when(productService.getOnSaleProducts()).thenReturn(createProductList());

        mockMvc.perform(post("/cart/items")
                        .sessionAttr("cart", new LinkedHashMap<Long, Integer>())
                        .param("productId", "1")
                        .param("quantity", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(content().string(containsString("数量は1以上を指定してください。")));

        verifyNoInteractionsOnAddFlow();
    }

    /**
     * 数量100入力時はバリデーションエラーで商品一覧を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartReturnsProductsWhenQuantityExceedsUpperLimit() throws Exception {
        when(productService.getOnSaleProducts()).thenReturn(createProductList());

        mockMvc.perform(post("/cart/items")
                        .sessionAttr("cart", new LinkedHashMap<Long, Integer>())
                        .param("productId", "1")
                        .param("quantity", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(content().string(containsString("数量は99以下を指定してください。")));

        verifyNoInteractionsOnAddFlow();
    }

    /**
     * 商品ID未指定時はバリデーションエラーで商品一覧を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartReturnsProductsWhenProductIdIsMissing() throws Exception {
        when(productService.getOnSaleProducts()).thenReturn(createProductList());

        mockMvc.perform(post("/cart/items")
                        .sessionAttr("cart", new LinkedHashMap<Long, Integer>())
                        .param("quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(content().string(containsString("商品IDを指定してください。")));

        verifyNoInteractionsOnAddFlow();
    }

    /**
     * 商品一覧画面用のテストデータを生成します。
     *
     * @return 商品一覧
     */
    private List<ProductModel> createProductList() {
        return List.of(
                createProductModel(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"),
                createProductModel(2L, "ゲーミングマウス", "6ボタン搭載", 3980, 5, "ON_SALE", "/images/products/2.png"),
                createProductModel(3L, "USB-Cハブ", "5in1モデル", 2980, 0, "ON_SALE", "/images/products/3.png"));
    }

    /**
     * テスト用の商品モデルを生成します。
     *
     * @param productId 商品ID
     * @param name 商品名
     * @param description 商品説明
     * @param price 価格
     * @param stock 在庫数
     * @param status 販売状態
     * @param imageUrl 画像URL
     * @return 商品モデル
     */
    private ProductModel createProductModel(Long productId, String name, String description, Integer price,
            Integer stock, String status, String imageUrl) {
        ProductModel productModel = new ProductModel();
        productModel.setProductId(productId);
        productModel.setName(name);
        productModel.setDescription(description);
        productModel.setPrice(price);
        productModel.setStock(stock);
        productModel.setStatus(status);
        productModel.setImageUrl(imageUrl);
        return productModel;
    }

    /**
     * カート追加処理のService呼び出しが行われないことを検証します。
     */
    private void verifyNoInteractionsOnAddFlow() {
        verify(productService, times(0)).validateAddToCart(eq(1L), eq(0), eq(1));
        verify(productService, times(0)).addCartItem(eq(1L), eq(1));
    }
}