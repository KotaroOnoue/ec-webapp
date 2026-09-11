# テスト結果-商品一覧画面 Repository

## 実施概要

- 実施日: 2026-09-10
- 対象テストクラス: `src/test/java/com/example/ec/repository/ProductRepositoryTest.java`
- 実行方法: JUnit 単体実行
- 使用 DB: H2 in-memory database

## 実装内容

- `products` テーブルを各テスト前に初期化し、`JdbcTemplate` で明示的にテストデータを登録するようにした。
- 以下の Repository 観点を検証するテストを実装した。
  - `findOnSaleProducts()` が `ON_SALE` の商品のみ返すこと
  - `findOnSaleProducts()` が `product_id` 昇順で返すこと
  - `findByProductId()` が存在する商品を正しく返すこと
  - `findByProductId()` が存在しない商品 ID の場合に `null` を返すこと

## 実行結果

- 実行件数: 5
- 成功: 5
- 失敗: 0

## 補足

- 在庫 0 かつ `ON_SALE` の商品もテストデータに含め、商品一覧取得が `status` 条件で抽出されることを確認した。
- 販売停止商品 `STOPPED` を含め、一覧取得から除外されることを確認した。