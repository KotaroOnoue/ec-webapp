# テスト結果-注文完了画面-Repository

## 実施概要

- 実施日: 2026-09-10
- 対象テストクラス: src/test/java/com/example/ec/repository/OrderCompleteRepositoryTest.java
- 実行方法: JUnit 単体実行
- 使用 DB: H2 in-memory database

## 実装内容

- 注文完了画面に関わる Repository テストクラスを新規作成した。
- 実際に orders テーブルへテストデータを登録し、OrderRepository#findByOrderId() の取得結果を検証する構成にした。
- 以下の観点を検証した。
  - 注文IDを指定した正常取得
  - orders 定義に従った customerName、postalCode、address、phoneNumber、totalAmount、orderAt の取得
  - orders.adress 列から OrderEntity.address へのマッピング
  - 存在しない注文IDで null を返すこと
  - 最小境界の order_id=1 を取得できること

## 実行結果

- 実行件数: 6
- 成功: 6
- 失敗: 0

## 補足

- 注文完了画面の Repository 観点は新規保存ではなく既存注文の参照であるため、findByOrderId() の読取品質に集中して検証した。
- orders テーブルの住所カラム名は要件どおり adress のままであり、Entity の address フィールドへ正しく変換されることを実データで確認した。