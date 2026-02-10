# テスト依存スコープ整理設計（最小リスク）

## 1. 目的
- テスト専用ライブラリのスコープを実態に合わせて `test` に整理し、実行時クラスパスへの不要混入を防ぐ。
- 変更影響を最小化するため、依存バージョンやテスト実装は変更しない。

## 2. 対象と非対象
### 2.1 対象
- `pom.xml` の以下 5 依存に `scope=test` を付与する。
1. `junit:junit`
2. `org.mockito:mockito-core`
3. `org.hamcrest:hamcrest-all`
4. `com.google.truth:truth`
5. `com.google.guiceberry:guiceberry`

### 2.2 非対象
- `hamcrest-all` から `hamcrest` への置換
- JUnit5 併用化（Vintage 導入含む）
- Surefire/Failsafe 設定変更
- テストコード修正

## 3. 変更方針
- 今回は「スコープ整理のみ」を実施し、挙動変更を伴う更新は行わない。
- 1コミット1目的を維持する。
- 依存関係がある作業は逐次実行する（修正 -> build -> test）。

## 4. 期待効果
- `test` スコープ依存が `compile/runtime` クラスパスから外れ、WAR への不要伝播を抑制できる。
- Tomcat 実行時の不要なクラス衝突リスクを低減できる。

## 5. 影響とリスク
### 5.1 影響
- 本番コードへの機能影響は想定しない。
- Maven の依存解決結果（スコープ境界）のみが変化する。

### 5.2 リスク
- 対象依存が本番コードから誤って参照されていた場合、`package` でコンパイルエラーになる。

### 5.3 検知と対処
- `mvn -DskipTests package` で即検知する。
- エラー発生時は該当依存のみ `compile` に戻し、理由を記録する。

## 6. 実施手順
1. `pom.xml` に対象 5 依存の `scope=test` を追加する。
2. `mvn -DskipTests package` を実行する。
3. `mvn test` を実行する。
4. `git diff --stat` で差分が目的範囲のみであることを確認する。
5. 実行コマンドと結果を検証ログへ記録する。

## 7. 完了条件
- `mvn -DskipTests package` が成功する。
- `mvn test` が成功する（現設定で skip の場合もフェーズ正常終了）。
- 差分が「テスト依存スコープ整理」の範囲内である。
