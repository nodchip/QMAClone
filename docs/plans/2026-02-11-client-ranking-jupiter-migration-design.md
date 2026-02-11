# client/ranking テストの Jupiter 移行設計（3クラス）

## 1. 目的
- `client/ranking` 配下の 3 テストを JUnit4 から JUnit5（Jupiter）へ移行する。
- 既存の段階移行方針を維持し、変更範囲を最小化する。

## 2. 方針
- 対象は次の 3 クラスに限定する。
1. `DateRangeSelectorPresenterTest`
2. `ThemeSelectorPresenterTest`
3. `GeneralRankingPresenterTest`
- テスト仕様は変更せず、JUnit API（import / annotation）のみ置換する。
- 必要最小限で `@RunWith` / `@Rule` を Jupiter 方式へ置換する。

## 3. 対象・非対象
### 3.1 対象
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions` 置換
- 必要に応じて `@RunWith` / `@Rule` の置換

### 3.2 非対象
- 他パッケージのテスト移行
- テスト仕様変更
- JUnit 基盤設定の再設計

## 4. 影響評価
- 影響範囲は対象 3 クラスのコンパイル/実行に限定される。
- 主リスクは JUnit4 固有記法の残存による実行エラー。
- Java 25 環境では Mockito inline と Byte Buddy の組み合わせで失敗する場合がある。

## 5. 検証手順
1. `mvn -DskipTests package`
2. `mvn test`
3. `mvn --% -Dsurefire.skip=false -Dtest=tv.dyndns.kishibe.qmaclone.client.ranking.DateRangeSelectorPresenterTest,tv.dyndns.kishibe.qmaclone.client.ranking.ThemeSelectorPresenterTest,tv.dyndns.kishibe.qmaclone.client.ranking.GeneralRankingPresenterTest -DfailIfNoTests=false test`
4. 必要時: `-DargLine=-Dnet.bytebuddy.experimental=true` を追加して再実行

## 6. 失敗時対処
- 失敗時は対象クラス内のみを修正対象とする。
- 広範囲置換は行わない。
- Java 25 + Mockito inline 由来の失敗は、既知制約としてログを残して再現手順を明記する。

## 7. 完了条件
- 対象 3 クラスが Jupiter で成功実行できる。
- `build` / `test` が成功する。
- 差分が対象 3 ファイル中心の最小範囲である。
- 1コミット1目的を維持する。
