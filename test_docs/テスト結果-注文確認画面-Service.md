# テスト結果-注文確認画面-Service

## 実施概要

- 実施日: 2026-09-10
- 対象テストクラス: src/test/java/com/example/ec/service/OrderConfirmServiceMockTest.java
- 実行方法: JUnit 単体実行
- テスト方式: Mockito によるモックベース単体テスト

## 実装内容

- 注文確認画面に関わる Service の単体テストクラスを新規作成した。
- データベースには接続せず、OrderRepository、CartRepository、CartService をモックに置き換えて OrderService の責務のみを検証する構成にした。
- 以下の観点を検証した。
  - placeOrder がカート合計金額を参照し、次注文IDを採番して注文保存後にカートを全削除すること
  - カート合計金額が 0 円のとき placeOrder が IllegalArgumentException を送出し、保存や削除を行わないこと
  - getOrderComplete が注文IDをもとに注文完了画面用モデルを返すこと
  - getOrderComplete が存在しない注文IDで null を返すこと

## 実行結果

- 実行件数: 5
- 成功: 5
- 失敗: 0

## 補足

- 初回実行時に Mockito matcher の指定方法に起因するテストコード不備があり、verify の全引数を matcher に統一して修正した。
- 技術要件どおり、Repository の振る舞いはすべてモック化し、Service 層のビジネスロジックだけを切り出して検証している。