package com.example.ec.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ec.repository.CartRepository;
import com.example.ec.exception.InsufficientStockException;
import com.example.ec.exception.ProductUnavailableException;
import com.example.ec.repository.ProductRepository;
import com.example.ec.repository.entity.ProductEntity;
import com.example.ec.service.model.ProductModel;

/**
 * 商品に関するビジネスロジックを扱うServiceです。
 */
@Service
public class ProductService {

    /** 販売中の状態を表す値です。 */
    private static final String ON_SALE_STATUS = "ON_SALE";

    /** 商品Repositoryです。 */
    @Autowired
    private ProductRepository productRepository;

    /** カートRepositoryです。 */
    @Autowired
    private CartRepository cartRepository;

    /**
     * 販売中の商品一覧を取得します。
     *
     * @return 画面表示用の商品一覧
     */
    public List<ProductModel> getOnSaleProducts() {
        return productRepository.findOnSaleProducts().stream()
                .map(this::toModel)
                .toList();
    }

    /**
     * 商品IDに対応する商品詳細を取得します。
     *
     * @param productId 商品ID
     * @return 画面表示用の商品詳細
     */
    public ProductModel getProductById(Long productId) {
        ProductEntity productEntity = productRepository.findByProductId(productId);
        if (productEntity == null) {
            throw new ProductUnavailableException("指定した商品は存在しません。");
        }
        return toModel(productEntity);
    }

    /**
     * 指定した数量をカートに追加できるかを検証します。
     *
     * @param productId 商品ID
     * @param currentCartQuantity 現在カートに入っている数量
     * @param addQuantity 今回追加する数量
     */
    public void validateAddToCart(Long productId, int currentCartQuantity, int addQuantity) {
        if (addQuantity < 1 || addQuantity > 99) {
            throw new IllegalArgumentException("数量は1から99の範囲で指定してください。");
        }

        ProductEntity productEntity = productRepository.findByProductId(productId);
        if (productEntity == null || !ON_SALE_STATUS.equals(productEntity.getStatus())) {
            throw new ProductUnavailableException("指定した商品は販売中ではありません。");
        }

        int requestedQuantity = currentCartQuantity + addQuantity;
        if (productEntity.getStock() < requestedQuantity) {
            throw new InsufficientStockException("指定した数量は在庫数を超えています。");
        }
    }

    /**
     * 商品をcart_itemテーブルへ追加します。
     *
     * @param productId 商品ID
     * @param quantity 追加数量
     */
    public void addCartItem(Long productId, Integer quantity) {
        cartRepository.insertCartItem(productId, quantity);
    }

    /**
     * EntityをService層のモデルへ変換します。
     *
     * @param productEntity 変換元の商品Entity
     * @return 変換後の商品モデル
     */
    public ProductModel toModel(ProductEntity productEntity) {
        ProductModel productModel = new ProductModel();
        productModel.setProductId(productEntity.getProductId());
        productModel.setName(productEntity.getName());
        productModel.setDescription(productEntity.getDescription());
        productModel.setPrice(productEntity.getPrice());
        productModel.setStock(productEntity.getStock());
        productModel.setStatus(productEntity.getStatus());
        productModel.setImageUrl(productEntity.getImageUrl());
        return productModel;
    }
}