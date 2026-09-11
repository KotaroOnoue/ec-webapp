# テスト結果-カート画面-Controller

## 実施概要

- 実施日: 2026-09-10
- 対象テストクラス: src/test/java/com/example/ec/controller/CartControllerMockTest.java
- 実行方法: MockMvc による HTTP リクエストシミュレーション
- サーバー起動: なし
- Service: Mockito によるモック

## 実装内容

- カート画面向けの Controller / Thymeleaf テストクラスを新規作成した。
- CartService をモックに置き換え、 CartController の表示責務、画面遷移、バリデーション、Thymeleaf の描画結果に焦点を当てた。
- 以下の観点を検証した。
  - GET /cart のページタイトル、ヘッダー、商品一覧、価格、小計、合計金額の表示
  - ヘッダーの カート が押下不可表示であること
  - 買い物を続ける と 注文手続きへ の遷移リンク表示
  - 空カート時のメッセージ表示
  - 初期フォーム cartUpdateForm の設定
  - POST /cart/items/{productId}/update の正常リダイレクト
  - 数量更新時にパス変数の productId が優先されること
  - 在庫超過時のエラーメッセージ付きリダイレクト
  - 数量未指定、数量 0、数量 100 のバリデーションエラー再表示
  - POST /cart/items/{productId}/delete の正常リダイレクト
  - 存在しない商品削除でも /cart に戻ること

## 実行結果

- 実行件数: 14
- 成功: 14
- 失敗: 0

## 補足

- 数量プルダウンの 1..99 の詳細な出力文字列までは固定せず、Controller のモデル値と主要な表示要素を中心に検証している。
- 数量更新異常系では、CartService#updateCartItem が呼ばれないこともあわせて確認している。