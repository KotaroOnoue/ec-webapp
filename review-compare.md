# ec-webapp / ec-webapp-example 比較分析

## 0. 分析前提

- 比較対象は ec-webapp と ec-webapp-example。
- 共通仕様の基準は `software_architecture.md` および `software_architecture.md`。
- 仕様書に明記されていない機能要件は、両実装のコードから読み取れる実際の挙動を比較対象とし、どちらが正しい仕様かは判定しない。
- 分析対象は主要フロー単位とし、商品一覧、商品詳細、カート、注文確認、注文完了を扱う。
- 複数フローにまたがる例外処理、バリデーション、画面共通のエラーハンドリングは共通基盤として別枠で扱う。
- 対応関係は画面またはユースケース単位で特定した。今回の対象では Controller 名・テンプレート名・URL が概ね一致しており、大きな推定対応は不要だった。

## 1. フローごとの詳細分析

## 1-1. 商品一覧フロー

### 1. 構造

ec-webapp の商品一覧は ProductController.showProducts が入口で、Controller 内の `ProductController.java:132` が商品一覧とカート件数を Model に詰め、`products.html` を返す。商品データの取得は ProductService.getOnSaleProducts から ProductRepository.findOnSaleProducts を呼び、SQL は `ProductMapper.xml:17` にある。

ec-webapp-example の商品一覧は ProductController.showProducts が入口で、ProductService.getOnSaleProducts の結果をそのまま Model に入れ、`products.html` を返す。Repository は ProductRepository.findOnSaleProducts、SQL は `ProductMapper.xml:8`。

商品一覧からカート追加する POST は両者とも /cart/items だが、ec-webapp は ProductController.addToCart、ec-webapp-example は ProductController.addItem が担当する。

### 2. 設計

ec-webapp は商品一覧画面の表示責務を Controller 側にやや多く持たせている。具体的には、商品一覧の取得に加えて、セッション上のカート件数集計や追加フォーム初期化を ProductController.showProducts と `ProductController.java:132` に置いている。設計上は、テンプレートに近い場所で画面表示用属性を組み立てる方向である。

ec-webapp-example は Controller を薄くし、商品一覧表示では一覧データの取得にほぼ限定している。カート追加後の戻り先制御は `ProductController.java:164` と `ProductController.java:136` に閉じているため、一覧表示そのものは単純である。

同じ部分として、両者とも ProductEntity を直接 View に渡さず ProductModel を経由している点は共通仕様に沿う。異なる部分として、ec-webapp は画面ごとの補助属性を Controller で組み立てる比重が高く、ec-webapp-example は戻り先制御やエラー時の再描画も含めて Controller を薄く保つ方向だが、商品一覧自体にはカート情報を混ぜていない。

この差のメリットは、ec-webapp が画面に必要な派生値を Controller で明示しやすいこと、ec-webapp-example が一覧表示の責務を単純に保ちやすいことにある。デメリットは、ec-webapp が Controller 肥大化に寄りやすいこと、ec-webapp-example が別画面との表示共通項を Service ではなく個別 Controller の補助メソッドに残しやすいことである。

### 3. 品質

良い点として、ec-webapp は ProductController.addToCart で redirectTo を持ち回り、バリデーションエラー時にも一覧と詳細のどちらへ戻るかを制御している。ec-webapp-example も `ProductController.java:164` により同様の戻り先制御を行っている。

問題点として、ec-webapp はカートをセッション Map と DB の cart_item に二重管理している。ProductController.createCart でセッション上の cart を持ち、ProductController.addToCart で DB とセッションをそれぞれ更新しているため、状態不整合の余地がある。

もう一つの問題点は、ec-webapp の商品詳細公開条件が商品一覧と一致していないことだ。`ProductMapper.xml:17` の一覧取得は販売中条件を持つ一方、`ProductMapper.xml:34` の商品 ID 指定取得は status 条件を持たない。これに対して ec-webapp-example は `ProductMapper.xml:24` と `ProductMapper.xml:37` により商品詳細も販売中条件を明示している。

ec-webapp-example の問題点は、業務例外処理が各 Controller に分散していることだ。ProductController.addItem が BusinessException を catch して画面再描画しており、同種の処理が他 Controller にもある。共通化余地が残る。

改善案として、ec-webapp はカート状態の正本を DB またはセッションのどちらかに寄せると整合性が上がる。ec-webapp-example は BusinessException を横断的に扱う仕組みを導入すると重複が減る。

### 4. テスト

ec-webapp は統合寄りの `ProductControllerTest.java:59` と Mock ベースの ProductControllerMockTest、ProductDetailControllerMockTest を分けている。正常系、在庫超過、詳細画面からの追加などは確認している。

ec-webapp-example は ProductControllerTest に一覧表示項目、ボタン状態、境界値、例外伝播、フォーム初期化など多数のケースを持つ。Controller レベルのケース分解は ec-webapp-example の方が細かい。

不足しているテストとして、両者とも戻り先判定への不正入力、販売停止商品の直接詳細アクセス時の期待挙動、Referer または redirectTo の異常値を体系的に確認しているかは、今回確認したコードからは十分ではない。

---

## 1-2. 商品詳細フロー

### 1. 構造

ec-webapp の商品詳細は ProductController.showProductDetail が入口で、ProductService.getProductById により商品を取得し、`ProductController.java:144` で purchasable と maxSelectableQuantity を Model に追加して `products-detail.html` を返す。

ec-webapp-example の商品詳細は ProductController.showProductDetail が入口で、ProductService.getOnSaleProduct を呼び、null の場合は /products へリダイレクトし、存在する場合に `products-detail.html` を返す。

### 2. 設計

ec-webapp は商品詳細画面用の派生属性を Controller で計算している。画面で必要な購入可否や最大選択数が `ProductController.java:144` にまとまっているため、テンプレートが必要とする条件が Controller に近い場所で分かる構造である。

ec-webapp-example は ProductModel をそのまま View に渡すため、Controller は取得結果の存在判定と遷移制御に集中している。商品公開条件そのものは Repository の SQL に寄せている。

同じ部分として、どちらも ProductModel を View に渡す。異なる部分として、ec-webapp は「画面表示に必要な派生値を Controller で作る」設計、ec-webapp-example は「公開条件を Repository と Service に閉じる」設計である。

メリットは、ec-webapp がテンプレート依存の判定値を追いやすいこと、ec-webapp-example が公開条件の一貫性を SQL 側に寄せやすいことである。デメリットは、ec-webapp が公開条件と表示条件を跨って持ちやすいこと、ec-webapp-example が派生値追加時に Service または Template の責務分担判断を要することである。

### 3. 品質

ec-webapp の良い点は、`ProductController.java:144` により購入可否と最大数量が明示されるため、UI 条件が読みやすいことだ。

ec-webapp-example の良い点は、販売中条件が `ProductMapper.xml:24` と `ProductMapper.xml:37` に閉じており、一覧と詳細で公開条件が揃っていることだ。

ec-webapp の問題点は、商品一覧は販売中のみ表示する一方で、詳細取得は `ProductMapper.xml:34` に販売中条件がないため、一覧と詳細の公開ルールに差があることだ。設計意図があれば中立だが、コード上は一貫性が弱い。

改善案として、ec-webapp は商品詳細取得の条件を商品一覧と揃えるか、あえて揃えないならその意図を Service の責務として明示した方がよい。

### 4. テスト

ec-webapp は `ProductControllerTest.java:111` と ProductDetailControllerMockTest により、詳細表示と詳細画面からのカート追加を分けて確認している。

ec-webapp-example は ProductControllerTest に詳細画面の表示要素、導線、フォーム、異常系を含む粒度の細かいテストを持つ。

不足しているテストとして、ec-webapp の purchasable や maxSelectableQuantity を直接検証するテストは今回確認範囲では見当たらなかった。

---

## 1-3. カートフロー

### 1. 構造

ec-webapp のカート表示は CartController.showCart が入口で、`CartController.java:110` が CartService.getCartItems、`CartService.java:119`、`CartService.java:53`、`CartService.java:64`、`CartService.java:73` を順に使って Model を構築する。更新は CartController.updateCartItem、削除は CartController.deleteCartItem が担当する。

ec-webapp-example のカート表示は CartController.showCart が入口で、CartService.getCart が CartModel 全体を返し、それを `cart.html` に渡す。更新は CartController.updateQuantity、削除は CartController.deleteItem が担当する。

Repository と SQL では、ec-webapp は `CartMapper.xml:14` で products と cart_item を JOIN し SUM 集計した結果を返す。ec-webapp-example は `CartMapper.xml:8` で cart_items を取得し、その後 `ProductMapper.xml:40` で商品情報をまとめて取得している。

### 2. 設計

ec-webapp はカート画面の表示値を個別メソッドの組み合わせで構築する設計である。合計金額、送料、請求金額、件数などをそれぞれ Service の別メソッドとして持つ。

ec-webapp-example はカート画面単位の凝集を重視し、CartModel に商品一覧、数量合計、金額合計、空判定をまとめている。

同じ部分として、どちらも Entity を直接 View に渡さない。異なる部分として、ec-webapp は集計値を部品化した設計、ec-webapp-example は画面モデル単位でまとめた設計である。

部品化設計のメリットは再利用しやすいことだが、同一画面内で重複計算を起こしやすい。画面モデル集約のメリットは 1 回の組み立てで済むことだが、別用途で一部だけ使いたい場合の粒度は粗くなる。

### 3. 品質

ec-webapp の良い点は、`CartMapper.xml:14` が JOIN と SUM を SQL 側で処理しており、N+1 のような逐次問い合わせにはなっていないことだ。

ec-webapp-example の良い点は、CartService.getCart が CartModel を 1 回で作るため、Controller が薄く、画面の取得単位も明確なことだ。

ec-webapp の問題点は、`CartController.java:110` が複数の集計メソッドを呼び、それぞれが内部で `CartService.java:42` を辿ることだ。`CartService.java:53`、`CartService.java:64`、`CartService.java:73`、`CartService.java:119` の構成上、同一リクエストで重複データ取得と再計算が発生する。

また ec-webapp はセッション上の cart と DB 上の cart_item を二重管理しており、CartController.updateCartItem と CartController.deleteCartItem も双方を書き換える。この構造は保守性と整合性の観点で弱い。

ec-webapp-example の問題点は、CartService.getCart が販売停止または取得不可の商品を表示対象外としてスキップする一方で、cart_items から自動削除するわけではないことだ。画面上見えないカート行が DB に残る可能性がある。

改善案として、ec-webapp は画面用 ViewModel を Service で 1 回生成する設計に寄せると無駄な再取得が減る。ec-webapp-example は非表示商品を残す方針ならその理由を明示し、残さない方針ならクリーンアップを追加した方がよい。

### 4. テスト

ec-webapp は CartControllerTest で統合的な表示・更新・削除を見ており、CartServiceMockTest で送料、割引、更新順序、在庫超過時の例外を確認している。

ec-webapp-example は CartControllerTest と CartServiceTest が非常に細かく、たとえば `CartServiceTest.java:142` で取得不可商品の除外、`CartServiceTest.java:212` で null 値の扱いを確認している。

テストの網羅性は ec-webapp-example の方が高い。ec-webapp はクーポンを含む金額計算の検証が強みだが、状態整合性や重複取得の観点のテストは見当たらない。

---

## 1-4. 注文確認フロー

### 1. 構造

ec-webapp の注文確認は OrderController.showOrderConfirm が入口で、`OrderController.java:159` が cartItems、件数、送料、割引額、請求額を構築し、クーポン適用フォームと注文フォームを初期化する。クーポン適用は OrderController.applyDiscountCode が別 POST として持つ。テンプレートは `order-confirm.html`。

ec-webapp-example の注文確認は OrderController.showOrderConfirm が入口で、OrderService.getOrderConfirmation の返す OrderModel をそのまま View に渡す。空カート時は /cart へ戻す。テンプレートは `order-confirm.html`。

### 2. 設計

ec-webapp は注文確認画面の集計とフォーム初期化を Controller に多く置いている。クーポンという追加機能も Controller と CartService の組み合わせで構成している。

ec-webapp-example は注文確認画面を 1 つのユースケースとして捉え、OrderModel に注文予定情報を集約している。Controller は空カート判定と画面遷移、入力値の変換に主に責務を持つ。

同じ部分として、どちらも注文者情報入力を Form で受け、Service を通して Repository へ進む。異なる部分として、ec-webapp はクーポン付き請求内訳のような派生値を Controller と CartService で作るのに対し、ec-webapp-example はまず注文確認画面の情報を OrderModel 単位でまとめている。

メリットは、ec-webapp がクーポンなどの追加軸を小さく差し込みやすいこと、ec-webapp-example が注文確認画面全体の責務をまとまりよく扱えることだ。デメリットは、ec-webapp が複数の集計メソッドに依存しやすいこと、ec-webapp-example が機能追加に伴って OrderModel の責務が肥大化しやすいことだ。

### 3. 品質

ec-webapp の良い点は、CouponApplyForm と `messages.properties` を用いて、クーポン入力とメッセージ管理を分離していることだ。テンプレート `order-confirm.html` も請求内訳を明示的に表示している。

ec-webapp-example の良い点は、空カート判定を OrderController.showOrderConfirm と OrderController.placeOrder の両方で行っており、直接 POST に対しても防御していることだ。

ec-webapp の問題点は、注文確認画面でもカート系集計を複数メソッド経由で取得しており、カートフローと同じ重複読み出しを引き継ぐことだ。`OrderController.java:159` は集計値ごとに CartService の別メソッドに依存している。

ec-webapp-example の問題点は、BusinessException のハンドリングやメッセージ解決が各 Controller に散っていることだ。OrderController.placeOrder でも、同じ構図が商品一覧やカートに見られる。

改善案として、ec-webapp は注文確認専用の ViewModel を Service でまとめると見通しがよくなる。ec-webapp-example は共通例外処理の導入で重複を減らせる。

### 4. テスト

ec-webapp は `OrderControllerTest.java:102` で注文保存と割引後金額を確認し、`OrderConfirmControllerMockTest.java:122` で請求内訳、`OrderConfirmControllerMockTest.java:216` でクーポン適用リダイレクトを確認している。

ec-webapp-example は OrderControllerTest に、正常系、空カート、バリデーション、郵便番号と電話番号の組合せ、表示要素など幅広いケースを持つ。

両者とも注文確認画面の主要動作はテストされているが、全体のケース数と境界値の厚さは ec-webapp-example が上回る。一方で、クーポン適用に関する個別検証は ec-webapp の特徴である。

---

## 1-5. 注文完了フロー

### 1. 構造

ec-webapp の注文完了は OrderController.showOrderComplete が入口で、OrderService.getOrderComplete を呼び、`OrderCompleteModel.java:9` を Model に詰めて `order-complete.html` を返す。Model の内容は OrderCompleteModel.orderId が中心である。

ec-webapp-example の注文完了は OrderController.showOrderCompleteWithoutId と OrderController.showOrderComplete が担当し、OrderService.getCompletedOrder が注文ヘッダと注文明細を復元して `order-complete.html` に渡す。

### 2. 設計

ec-webapp は注文完了を注文番号確認の最小画面として扱っている。Service は注文完了画面向けに最小限のモデルだけを返す。

ec-webapp-example は注文完了画面にも注文ヘッダと注文明細の表示可能性を残す設計であり、OrderItemRepository と `schema.sql:30` を利用している。

同じ部分として、どちらも注文 ID をキーに完了画面を表示する。異なる部分として、ec-webapp は最小情報のみを持ち、ec-webapp-example は注文内容を再構成できる。

メリットは、ec-webapp が単純で実装量が少ないこと、ec-webapp-example が再利用性と拡張性に優れることだ。デメリットは、ec-webapp が完了画面の情報量や後続機能拡張に弱いこと、ec-webapp-example が永続化対象とテスト対象を増やすことである。

### 3. 品質

ec-webapp の良い点は、完了フローが非常に単純で追いやすいことだ。注文が存在しない場合は OrderController.showOrderComplete で /products に戻す。

ec-webapp-example の良い点は、OrderService.placeOrder が Transaction 内で注文ヘッダ保存、在庫減算、注文明細保存、カート削除まで実行していることだ。在庫更新は `ProductMapper.xml:63`、注文明細保存は `OrderItemMapper.xml` が担う。

ec-webapp の問題点は、注文時の保存対象が orders 中心であり、在庫減算や注文明細永続化に相当する Repository が確認できないことだ。実際に Repository も OrderRepository のみで、注文完了モデルも最小限である。仕様書に注文詳細保存要件は明示されていないため正誤判定はしないが、実装上の情報保持量は ec-webapp-example の方が多い。

また ec-webapp の orders スキーマには `schema.sql:25` の adress という綴りがあり、Mapper も `OrderMapper.xml:12` と `OrderMapper.xml:30` で追随している。動作上の不整合ではないが、命名品質は低い。

改善案として、ec-webapp は現在の最小設計を維持するなら意図の明文化が必要であり、将来的に注文詳細の参照や監査が必要なら、注文明細と在庫更新を Transaction に含める構成への拡張余地を持たせた方がよい。

### 4. テスト

ec-webapp は OrderControllerTest で注文保存と完了画面表示を確認している。Repository テストも OrderRepositoryTest があるが、対象は orders である。

ec-webapp-example は `OrderRepositoryTest.java:49`、`OrderItemRepositoryTest.java:60`、OrderServiceTest により、ヘッダ、明細、Service 挙動を層ごとに確認している。

テストの深さは ec-webapp-example が上回る。ec-webapp は最小フローの正常動作は押さえているが、保持データの粒度が小さい分、後続参照や整合性の検証対象も限定的である。

---

## 2. 共通基盤の比較

## 2-1. 例外処理

### 構造

ec-webapp は `GlobalExceptionHandler.java:34` を持ち、`GlobalExceptionHandler.java:34`、`GlobalExceptionHandler.java:45`、`GlobalExceptionHandler.java:56` で例外を一元処理する。エラーメッセージは `GlobalExceptionHandler.java:71` が Flash 属性に詰め、`GlobalExceptionHandler.java:83` が戻り先を決める。

ec-webapp-example は共通 Advice は確認できず、`BusinessException.java:9` を各 Controller が catch して MessageSource で解決している。例として ProductController.addItem、CartController.updateQuantity、OrderController.placeOrder がある。

### 設計

ec-webapp は横断関心を共通基盤として集約している。画面ごとの差より、例外種別ごとの一貫処理を優先した設計である。

ec-webapp-example は画面ごとの戻り方やメッセージ表示を Controller 単位で制御している。共通例外型はあるが、ハンドリングは局所化されている。

集約設計のメリットは重複削減と一貫性であり、デメリットは URI 文字列判定などに依存しやすいことだ。局所設計のメリットは各画面に最適化しやすいこと、デメリットは同種ロジックの重複である。

### 品質

ec-webapp の良い点は、一元的な例外処理により業務例外と入力異常のルーティングが統一されていることだ。

ec-webapp の問題点は、`GlobalExceptionHandler.java:83` が request URI や request parameter の文字列判定に依存していることだ。ルーティング変更時に壊れやすい。

ec-webapp-example の良い点は、画面単位で最適な戻り方を実装しやすいことだ。

ec-webapp-example の問題点は、MessageSource を使った例外解決と Model への errorMessage 設定が複数 Controller に散っていることだ。

## 2-2. バリデーション

### 構造

ec-webapp は CartAddForm、CartUpdateForm、CouponApplyForm、OrderForm に Bean Validation を付与し、メッセージキーを明示している。

ec-webapp-example は ProductListItemForm、CartItemForm、OrderForm に Bean Validation を付与し、メッセージは既定キー解決に寄せている。

### 設計

ec-webapp はメッセージキーをフォーム定義に明示し、`messages.properties` を主導にしている。ec-webapp-example は制約アノテーションの標準的なキー解決に寄せ、`messages.properties` で上書きしている。

明示的キー方式のメリットはメッセージ定義の意図が読みやすいこと、デメリットはアノテーション記述が冗長になりやすいことだ。既定キー方式のメリットは簡潔さ、デメリットはキー命名規則に依存することだ。

### 品質

どちらも数量 1〜99、郵便番号 7 桁、電話番号 10〜11 桁など主要制約はフォームに置いており、共通仕様と整合している。

ec-webapp はクーポン用フォームを独立させており、注文確認画面の責務分割が明瞭である。ec-webapp-example はフォーム数が少なく単純だが、クーポン機能相当は持たない。

## 2-3. 認証・認可・その他

- 認証・認可の実装は今回確認したコードからは判断できない。
- セキュリティ面では、両者とも Spring MVC と Thymeleaf を用いており、テンプレート内での基本的なエスケープ機構は利用される前提だが、CSRF 設定や認証連携などの全体設定は今回の範囲では確認していない。
- 実行計測が必要なパフォーマンス評価は行っていない。静的コード読解で判断できる範囲では、ec-webapp に重複取得の可能性があり、両者とも典型的な N+1 問題は主要フローでは確認しなかった。

## 3. テスト全体比較

ec-webapp は機能ごとに統合寄りテストと Mock ベーステストを併用しており、クーポンや画面別要素に対応したテストを持つ。層を分けている点は明確だが、機能フローごとのケース分解は ec-webapp-example ほど細かくない。

ec-webapp-example は Controller、Service、Repository の各層でケース数が多く、さらに `WebSpecificationTest.java:46` により外部仕様レベルの画面遷移・表示確認まで行っている。たとえば `WebSpecificationTest.java:112` や `WebSpecificationTest.java:176` のように、ユーザー視点の仕様確認が明示されている。

評価としては、テストの網羅性と保守性は ec-webapp-example が優勢である。ec-webapp はクーポンを含む独自要素に対するテストがある一方、ケースマトリクスの広さでは劣る。

不足しているテストとしては、両者とも並行更新や競合時の振る舞い、ロケール差異、予期しない RuntimeException の表示方針などは、今回の確認範囲では十分に読み取れなかった。

## 4. 最終サマリ表

| 観点 | ec-webapp | ec-webapp-example | 比較結果（中立／評価の別を明記） |
|---|---|---|---|
| 構造 | セッション Map と DB を併用してカート状態を扱う。discount_codes を持つ。注文完了は最小モデル中心。 | CartModel と OrderModel に画面単位で集約。order_items を持つ。HTTP エラー画面テンプレートもある。 | 中立: 同じレイヤード構成だが、状態管理粒度と永続化対象が異なる。 |
| 設計 | Controller 側で画面表示用データを組み立てる比重が高い。GlobalExceptionHandler で例外処理を共通化。 | Service 側で画面単位モデルを組み立てる比重が高い。BusinessException を各 Controller で解決。 | 中立: ec-webapp は横断共通化寄り、ec-webapp-example はユースケース凝集寄り。 |
| 品質 | 良い点は共通例外処理、明示的メッセージ管理、クーポン機能の分離。問題点は session/DB 二重管理、集計の重複取得、adress 命名、注文完了情報の少なさ。 | 良い点は画面モデル凝集、Transaction 境界、在庫更新と注文明細保存、公開条件の一貫性。問題点は Controller ごとの例外処理重複、非表示カート行の残存余地。 | 評価: 総合的な内部整合性と拡張余地は ec-webapp-example が高い。ec-webapp は共通例外処理とクーポン設計に強みがある。 |
| テスト | 統合寄りテストと Mock テストを併用し、クーポン関連まで確認している。 | 仕様テストと層別テストの双方が厚く、正常系、異常系、境界値の網羅が広い。 | 評価: テストの広さと細かさは ec-webapp-example が優位。ec-webapp は独自機能に対するテストが特徴。 |

## 5. 総括

両実装は共通仕様書に沿って同じ画面フローを持つが、内部設計の重心が異なる。ec-webapp は Controller 主導で画面表示に必要な値を組み立て、例外処理を共通化し、クーポン機能を加えた構成である。ec-webapp-example は Service 側で画面単位モデルを構築し、注文ヘッダ、在庫、注文明細まで一連の業務処理としてまとめる構成である。

評価の観点では、品質とテストの総合バランスは ec-webapp-example の方が高い。理由は、商品公開条件の一貫性、画面モデルの凝集、Transaction を伴う注文処理、注文明細の保持、境界値や異常系を含めたテストの厚さにある。一方で ec-webapp には、GlobalExceptionHandler による横断処理、明示的なバリデーションメッセージ管理、クーポン適用という独自拡張のまとまりという明確な強みがある。