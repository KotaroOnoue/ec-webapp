# テスト結果-カート画面 Service

## 実施概要

- 実施日: 2026-09-10
- 対象テストクラス: `src/test/java/com/example/ec/service/CartServiceMockTest.java`
- 実行方法: JUnit 単体実行
- Repository / 依存Service: Mockito によるモック
- データベース接続: なし

## 実装内容

- カート画面向けの Service 単体テストクラスを新規作成した。
- `CartRepository` と `ProductService` をモックに置き換え、`CartService` のみを単体で検証した。
- 以下の観点を検証した。
  - `getCartItems()` がカート一覧を `CartItemModel` に変換し、小計を計算すること
  - `getTotalAmount()` が小計合計を返し、空カート時は `0` を返すこと
  - `getTotalQuantity()` が数量合計を返し、空カート時は `0` を返すこと
  - `updateCartItem()` が在庫チェック後に `delete -> insert` の順で更新すること
  - `updateCartItem()` が在庫超過時に後続の削除・再登録を行わないこと
  - `deleteCartItem()` が削除処理を委譲すること
  - `getCartQuantityByProductId()` が指定商品の数量を返すこと
  - `addCartItem()` が保存処理を委譲すること

## 実行結果

- 実行件数: 11
- 成功: 11
- 失敗: 0

## 補足

- `updateCartItem()` の重要分岐である正常更新経路と在庫超過の異常経路を、呼び出し順序まで含めて検証した。
- 項目表の Service 観点のうち、集計、更新、削除、数量取得の主要ケースをカバーしている。