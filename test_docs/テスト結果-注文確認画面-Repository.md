# テスト結果-注文確認画面-Repository

## 実施概要

- 実施日: 2026-09-10
- 対象テストクラス: src/test/java/com/example/ec/repository/OrderConfirmRepositoryTest.java
- 実行方法: JUnit 単体実行
- 使用 DB: H2 in-memory database

## 実装内容

- 注文確認画面向けの Repository テストクラスを新規作成した。
- 各テスト前に products、cart_item、orders を初期化し、JdbcTemplate で明示的にテストデータを登録する構成にした。
- 以下の観点を検証した。
  - CartRepository#findCartItems が注文確認画面表示用のカート内容を取得できること
  - CartRepository#findCartItems が空カート時に空リストを返すこと
  - OrderRepository#findNextOrderId が空テーブル時に 1、既存データあり時に次の採番値を返すこと
  - OrderRepository#insertOrder が customer_name、postal_code、adress、phone_number、total_amount を正しく保存すること
  - OrderRepository#findByOrderId が存在する注文を取得し、存在しない注文では null を返すこと
  - CartRepository#deleteAllCartItems がカート内商品を全削除すること

## 実行結果

- 実行件数: 9
- 成功: 9
- 失敗: 0

## 補足

- 注文確認画面の Repository 観点は orders だけでなくカート内容取得も含むため、OrderRepository と CartRepository の両方を 1 クラスでまとめて検証した。
- orders テーブルの住所カラムは要件どおり adress で保存されることも実データで確認している。