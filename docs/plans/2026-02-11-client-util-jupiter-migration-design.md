# client/util テストの Jupiter 移行設計（3クラス）

## 1. 目的
- `client/util` 配下の残り 3 テストを JUnit4 から JUnit5（Jupiter）へ移行する。
- `StringUtilsTest` で確認済みの移行パターンを同一パッケージへ展開し、段階移行を進める。

## 2. 方針
- 対象は次の 3 クラスに限定する。
1. `CommandRunnerTest`
2. `ImageCacheTest`
3. `ImageUrlTest`
- テスト仕様は変更せず、JUnit API（import / annotation）のみ置換する。
- 変更は最小差分で行い、他パッケージへ波及させない。

## 3. 対象・非対象
### 3.1 対象
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions` 置換
- 必要に応じて `@RunWith(JUnit4.class)` の削除

### 3.2 非対象
- 他パッケージのテスト移行
- テスト仕様変更
- JUnit 基盤設定の再設計

## 4. 影響評価
- 影響範囲は対象 3 クラスのコンパイル/実行に限定される。
- 主リスクは JUnit4 固有記法の残存による実行エラー。
- 既存 JUnit4 資産は Vintage で維持する前提。

## 5. 検証手順
1. `mvn -DskipTests package`
2. `mvn test`
3. `mvn --% -Dsurefire.skip=false -Dtest=tv.dyndns.kishibe.qmaclone.client.util.CommandRunnerTest,tv.dyndns.kishibe.qmaclone.client.util.ImageCacheTest,tv.dyndns.kishibe.qmaclone.client.util.ImageUrlTest -DfailIfNoTests=false test`

## 6. 失敗時対処
- 失敗時は対象クラス内のみを修正対象とする。
- 広範囲置換は行わない。
- `gwt:test` の 0 件失敗は既知のため、明示実行時は `-DfailIfNoTests=false` を使用する。

## 7. 完了条件
- 対象 3 クラスが Jupiter で成功実行できる。
- `build` / `test` が成功する。
- 差分が対象 3 ファイル中心の最小範囲である。
- 1コミット1目的を維持する。
