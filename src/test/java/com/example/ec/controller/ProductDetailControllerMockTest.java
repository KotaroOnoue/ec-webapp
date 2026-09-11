package com.example.ec.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
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
import com.example.ec.exception.ProductUnavailableException;
import com.example.ec.service.CartService;
import com.example.ec.service.ProductService;
import com.example.ec.service.model.CartItemModel;
import com.example.ec.service.model.CartSummaryModel;
import com.example.ec.service.model.ProductModel;

/**
 * 商品詳細画面のControllerとThymeleaf表示をモックServiceで検証するテストです。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProductDetailControllerMockTest {

    /** MockMvcです。 */
    private MockMvc mockMvc;

    /** WebApplicationContextです。 */
    @Autowired
    private WebApplicationContext webApplicationContext;

    /** 商品Serviceのモックです。 */
    @MockitoBean
    private ProductService productService;

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
     * 商品詳細画面に主要な表示要素が描画されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductDetailDisplaysPageTitleHeaderAndProductInfo() throws Exception {
                stubCartSummary(0);
        when(productService.getProductById(1L)).thenReturn(
                createProductModel(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products-detail"))
                .andExpect(content().string(containsString("<title>商品詳細</title>")))
                .andExpect(content().string(containsString("商品一覧")))
                .andExpect(content().string(containsString("カートを見る")))
                .andExpect(content().string(containsString("ワイヤレスイヤホン")))
                .andExpect(content().string(containsString("ノイズキャンセリング対応")))
                .andExpect(content().string(containsString("商品ID")))
                .andExpect(content().string(containsString("1")))
                .andExpect(content().string(containsString("¥5,980")))
                .andExpect(content().string(containsString("5,980円")))
                .andExpect(content().string(containsString("在庫数")))
                .andExpect(content().string(containsString("10")))
                .andExpect(content().string(containsString("販売ステータス")))
                .andExpect(content().string(containsString("販売中")))
                .andExpect(content().string(containsString("/images/products/1.png")))
                .andExpect(content().string(containsString("href=\"/products\"")))
                .andExpect(content().string(containsString("href=\"/cart\"")));
    }

    /**
     * 販売中かつ在庫ありの商品では購入操作が有効になることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductDetailDisplaysPurchasableControlsWhenInStock() throws Exception {
                stubCartSummary(0);
        when(productService.getProductById(1L)).thenReturn(
                createProductModel(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("purchasable", true))
                .andExpect(model().attribute("maxSelectableQuantity", 10))
                .andExpect(content().string(containsString("カートに追加")))
                .andExpect(content().string(containsString("この商品は現在 <span>10</span> 点の在庫があります。")))
                .andExpect(content().string(not(containsString("在庫切れのため追加できません"))));
    }

    /**
     * 在庫切れ商品の場合は購入操作が無効になることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductDetailDisplaysDisabledControlsWhenOutOfStock() throws Exception {
                stubCartSummary(0);
        when(productService.getProductById(3L)).thenReturn(
                createProductModel(3L, "USB-Cハブ", "5in1モデル", 2980, 0, "ON_SALE", "/images/products/3.png"));

        mockMvc.perform(get("/products/3"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("purchasable", false))
                .andExpect(model().attribute("maxSelectableQuantity", 0))
                .andExpect(content().string(containsString("在庫なし")))
                .andExpect(content().string(containsString("disabled=\"disabled\"")))
                .andExpect(content().string(containsString("選択不可")))
                .andExpect(content().string(containsString("在庫切れ、または販売停止中のためカートへ追加できません。")));
    }

    /**
     * 販売停止商品の場合は購入操作が無効になることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductDetailDisplaysDisabledControlsWhenStopped() throws Exception {
                stubCartSummary(0);
        when(productService.getProductById(4L)).thenReturn(
                createProductModel(4L, "Webカメラ", "フルHD対応", 4980, 8, "STOPPED", "/images/products/4.png"));

        mockMvc.perform(get("/products/4"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("purchasable", false))
                .andExpect(model().attribute("maxSelectableQuantity", 8))
                .andExpect(content().string(containsString("販売停止")))
                .andExpect(content().string(containsString("disabled=\"disabled\"")))
                .andExpect(content().string(containsString("在庫切れ、または販売停止中のためカートへ追加できません。")));
    }

    /**
     * 在庫数1の場合は数量プルダウンの選択肢が1のみになることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductDetailLimitsSelectableQuantityToStockBoundary() throws Exception {
                stubCartSummary(0);
        when(productService.getProductById(5L)).thenReturn(
                createProductModel(5L, "LANケーブル", "カテゴリ6A", 980, 1, "ON_SALE", "/images/products/5.png"));

        mockMvc.perform(get("/products/5"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("maxSelectableQuantity", 1))
                .andExpect(content().string(containsString("<select id=\"quantity\"")))
                .andExpect(content().string(containsString("name=\"quantity\"")));
    }

    /**
     * 在庫数が99を超える場合も数量プルダウンの最大値が99になることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductDetailLimitsSelectableQuantityToNinetyNine() throws Exception {
                stubCartSummary(0);
        when(productService.getProductById(10L)).thenReturn(
                createProductModel(10L, "4Kモニター", "高解像度モニター", 49800, 120, "ON_SALE", "/images/products/10.png"));

        mockMvc.perform(get("/products/10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("maxSelectableQuantity", 99))
                .andExpect(content().string(containsString("<select id=\"quantity\"")))
                .andExpect(content().string(containsString("name=\"quantity\"")));
    }

    /**
        * カート数量合計がヘッダーに表示されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductDetailDisplaysCartItemCountFromCartSummary() throws Exception {
        stubCartSummary(5, createCartItemModel(1L, "ワイヤレスイヤホン", 5980, 2),
                createCartItemModel(2L, "ゲーミングマウス", 3980, 3));
        when(productService.getProductById(1L)).thenReturn(
                createProductModel(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cartItemCount", 5))
                .andExpect(content().string(containsString("cart-count\">5</span>")));
    }

    /**
     * 初回表示時にカート追加フォームの初期値が設定されることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductDetailSetsInitialCartAddForm() throws Exception {
                stubCartSummary(0);
        when(productService.getProductById(1L)).thenReturn(
                createProductModel(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("cartAddForm"))
                .andExpect(model().attribute("cartAddForm", hasProperty("productId", is(1L))))
                .andExpect(model().attribute("cartAddForm", hasProperty("quantity", is(1))));
    }

    /**
     * 存在しない商品IDでは商品一覧へリダイレクトすることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void showProductDetailRedirectsWhenProductDoesNotExist() throws Exception {
        when(productService.getProductById(999L)).thenThrow(new ProductUnavailableException("指定した商品は存在しません。"));

        mockMvc.perform(get("/products/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attribute("errorMessage", "指定した商品は販売中ではありません。"));
    }

    /**
     * 商品詳細画面からの正常なカート追加で同一詳細画面へ戻ることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartFromDetailRedirectsWhenAdditionSucceeds() throws Exception {
                when(productService.getCurrentCartQuantity(2L)).thenReturn(2);

        mockMvc.perform(post("/cart/items")
                                                .with(csrf())
                        .param("productId", "2")
                        .param("quantity", "3")
                        .param("redirectTo", "/products/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/2"));

        verify(productService, times(1)).validateAddToCart(2L, 2, 3);
        verify(productService, times(1)).addCartItem(2L, 3);
    }

    /**
     * 商品詳細画面からの在庫超過時に同一詳細画面へエラー付きで戻ることを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartFromDetailRedirectsWithErrorWhenStockIsExceeded() throws Exception {
        when(productService.getCurrentCartQuantity(1L)).thenReturn(10);
        doThrow(new InsufficientStockException("指定した数量は在庫数を超えています。"))
                .when(productService).validateAddToCart(1L, 10, 1);

        mockMvc.perform(post("/cart/items")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "1")
                        .param("redirectTo", "/products/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/1"))
                .andExpect(flash().attribute("errorMessage", "在庫数を超えるためカートに追加できません。"));
    }

    /**
     * 数量未指定時は商品詳細画面を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartFromDetailReturnsDetailWhenQuantityIsMissing() throws Exception {
        stubCartSummary(0);
        when(productService.getProductById(1L)).thenReturn(
                createProductModel(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        mockMvc.perform(post("/cart/items")
                        .with(csrf())
                        .param("productId", "1")
                        .param("redirectTo", "/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products-detail"))
                .andExpect(content().string(containsString("数量を指定してください。")));

        verifyNoAddFlowInvocations();
    }

    /**
     * 数量0入力時は商品詳細画面を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartFromDetailReturnsDetailWhenQuantityIsZero() throws Exception {
        stubCartSummary(0);
        when(productService.getProductById(1L)).thenReturn(
                createProductModel(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        mockMvc.perform(post("/cart/items")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "0")
                        .param("redirectTo", "/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products-detail"))
                .andExpect(content().string(containsString("数量は1以上を指定してください。")));

        verifyNoAddFlowInvocations();
    }

    /**
     * 商品ID未指定時も商品詳細画面を再表示することを検証します。
     *
     * @throws Exception テスト失敗時
     */
    @Test
    void addToCartFromDetailReturnsDetailWhenProductIdIsMissing() throws Exception {
        stubCartSummary(0);
        when(productService.getProductById(1L)).thenReturn(
                createProductModel(1L, "ワイヤレスイヤホン", "ノイズキャンセリング対応", 5980, 10, "ON_SALE", "/images/products/1.png"));

        mockMvc.perform(post("/cart/items")
                        .with(csrf())
                        .param("quantity", "1")
                        .param("redirectTo", "/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products-detail"))
                .andExpect(content().string(containsString("商品IDを指定してください。")))
                .andExpect(model().attribute("cartAddForm", hasProperty("productId", is(1L))));

        verifyNoAddFlowInvocations();
    }

    /**
     * 商品詳細画面用のテストデータを生成します。
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
         * テスト用のカート集計をスタブします。
         *
         * @param totalQuantity 合計数量
         * @param cartItems カート商品
         */
        private void stubCartSummary(int totalQuantity, CartItemModel... cartItems) {
                CartSummaryModel cartSummaryModel = new CartSummaryModel();
                Map<Long, Integer> cartQuantities = new LinkedHashMap<>();
                for (CartItemModel cartItem : cartItems) {
                        cartQuantities.put(cartItem.getProductId(), cartItem.getQuantity());
                }
                cartSummaryModel.setCartItems(java.util.List.of(cartItems));
                cartSummaryModel.setCartQuantities(cartQuantities);
                cartSummaryModel.setTotalQuantity(totalQuantity);
                cartSummaryModel.setTotalAmount(0);
                cartSummaryModel.setShippingAmount(0);
                cartSummaryModel.setDiscountAmount(0);
                cartSummaryModel.setBillingAmount(0);
                when(cartService.getCartSummary()).thenReturn(cartSummaryModel);
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

    /**
     * カート追加処理のService呼び出しが行われないことを検証します。
     */
    private void verifyNoAddFlowInvocations() {
        verify(productService, times(0)).validateAddToCart(anyLong(), anyInt(), anyInt());
        verify(productService, times(0)).addCartItem(anyLong(), anyInt());
    }
}