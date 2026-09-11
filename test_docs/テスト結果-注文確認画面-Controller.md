# テスト結果-注文確認画面-Controller

## 実施概要

- 実施日: 2026-09-10
- 対象テストクラス: src/test/java/com/example/ec/controller/OrderConfirmControllerMockTest.java
- 実行方法: JUnit 単体実行
- テスト方式: MockMvc による疑似 HTTP リクエスト + Mockito による Service モック

## 実装内容

- 注文確認画面の Controller と Thymeleaf 表示内容を検証するテストクラスを新規作成した。
- 実際のサーバーやデータベースは使用せず、Spring Boot の WebEnvironment.MOCK と MockMvc で GET /orders/confirm および POST /orders を検証する構成にした。
- CartService と OrderService はモックに置き換え、Controller の画面遷移、モデル設定、バリデーション、例外ハンドリングの責務だけを確認した。
- 主な検証観点は以下のとおり。
  - 注文確認画面のタイトル、ヘッダー、カート内容、合計金額、商品点数、配送先入力欄、操作ボタン表示
  - 初回表示時の orderForm 初期化
  - 空カート時の空メッセージ表示
  - 正常入力時の /orders/complete/{orderId} へのリダイレクト
  - 注文確定後のセッション cart クリア
  - 郵便番号 6 桁、8 桁、電話番号 9 桁、12 桁などの境界値バリデーション
  - 氏名、郵便番号、住所の未入力バリデーション
  - 空カート例外発生時の /orders/confirm へのエラーメッセージ付きリダイレクト

## 実行結果

- 実行件数: 18
- 成功: 18
- 失敗: 0

## 補足

- 項目表の Controller 観点に沿って、正常表示、入力エラー、境界値、例外経路、セッション更新を分けて検証した。
- 注文完了画面の個別表示検証は今回の項目表スコープ外とし、注文確認画面からのリダイレクト先 URL までを本テストの対象とした。