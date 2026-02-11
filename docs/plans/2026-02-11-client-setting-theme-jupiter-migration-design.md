# client/setting/theme テストの Jupiter 移行設計（3クラス）

## 1. 目的
- `client/setting/theme` 配下の 3 テストを JUnit4 から JUnit5（Jupiter）へ移行する。
- 既存の段階移行方針を維持し、変更範囲を最小化する。

## 2. 方針
- 対象は次の 3 クラスに限定する。
1. `ThemeProviderTest`
2. `ThemeQueryProviderTest`
3. `ThemeCellTest`
- テスト仕様は変更せず、JUnit API（import / annotation）のみ置換する。
- 他パッケージへ変更を波及させない。

## 3. 対象・非対象
### 3.1 対象
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions` 置換
- 必要に応じて `@RunWith` / `@Rule` を Jupiter 方式へ置換

### 3.2 非対象
- 他パッケージのテスト移行
- テスト仕様変更
- JUnit 基盤設定の再設計

## 4. 影響評価
- 影響範囲は対象 3 クラスのコンパイル/実行に限定される。
- 主リスクは JUnit4 固有記法の残存による実行エラー。
- 既存 JUnit4 資産は Vintage で継続実行する前提。

## 5. 検証手順
1. `mvn -DskipTests package`
2. `mvn test`
3. `mvn --% -Dsurefire.skip=false -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeProviderTest,tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeQueryProviderTest,tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeCellTest -DfailIfNoTests=false test`

## 6. 失敗時対処
- 失敗時は対象クラス内のみを修正対象とする。
- 広範囲置換は行わない。
- 既知の実行環境制約が出た場合は再現ログを残し、設計へ反映する。

## 7. 完了条件
- 対象 3 クラスが Jupiter で成功実行できる。
- `build` / `test` が成功する。
- 差分が対象 3 ファイル中心の最小範囲である。
- 1コミット1目的を維持する。
