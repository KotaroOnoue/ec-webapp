# テスト結果-注文完了画面-Controller

## 実施概要

- 実施日: 2026-09-10
- 対象テストクラス: src/test/java/com/example/ec/controller/OrderCompleteControllerMockTest.java
- 実行方法: JUnit 単体実行
- テスト方式: MockMvc による疑似 HTTP リクエスト + Mockito による Service モック

## 実装内容

- 注文完了画面の Controller と Thymeleaf 表示内容を検証するテストクラスを新規作成した。
- 実際のサーバーやデータベースは使用せず、Spring Boot の WebEnvironment.MOCK と MockMvc で GET /orders/complete/{orderId} を検証する構成にした。
- OrderService はモックに置き換え、OrderController の画面遷移、モデル設定、パス変
数受け渡し、異常系リダイレクトの責務だけを確認した。
- 主な検証観点は以下のとおり。
  - 注文完了画面のタイトル、ヘッダー、注文完了メッセージ、注文番号表示
  - モデルへの order 設定
  - 「商品一覧に戻る」リンクの表示と遷移先
  - 存在しない注文IDで /products にリダイレクトすること
  - 最小境界の orderId=1 を表示できること
  - 数値以外の orderId で /products にリダイレクトすること
  - パス変数の orderId が Service へ渡されること

## 実行結果

- 実行件数: 8
- 成功: 8
- 失敗: 0

## 補足

- 不正なパス変数形式のケースは、項目表の初期想定では 400 系も候補だったが、実装上の実際の挙動は /products へのリダイレクトだったため、その実挙動に合わせて検証した。
- 注文完了画面は表示責務が小さいため、注文番号表示と異常系リダイレクトの確認を中心に構成した。