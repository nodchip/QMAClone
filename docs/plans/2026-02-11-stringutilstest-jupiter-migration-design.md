# StringUtilsTest の JUnit Jupiter 移行設計

## 1. 目的
- `StringUtilsTest` 1 クラスを JUnit4 から JUnit5（Jupiter）へ移行し、段階移行の実行パターンを確立する。
- 既存 JUnit4 資産を維持し、変更範囲を最小化する。

## 2. 方針
- 対象は `src/test/java/tv/dyndns/kishibe/qmaclone/client/util/StringUtilsTest.java` のみ。
- 既存テスト観点は維持し、Jupiter 用の import / annotation 置換を行う。
- `pom.xml` は原則変更しない（必要時のみ最小変更）。
- 検証は `build -> test -> 対象テスト明示実行` を直列で行う。

## 3. 対象・非対象
### 3.1 対象
- `StringUtilsTest` の JUnit4 依存箇所
1. `org.junit.Test` -> `org.junit.jupiter.api.Test`
2. 必要に応じて `Assert` -> `Assertions` への置換

### 3.2 非対象
- 他テストクラスの移行
- JUnit4 資産の一括書換え
- テスト仕様変更

## 4. 影響評価
- 影響は対象クラスのテスト実行に限定される。
- 既存 JUnit4 テスト群は `vintage` により継続実行可能な前提。
- 主リスクは JUnit4 固有記法の残存によるコンパイル/実行エラー。

## 5. 検証手順
1. `mvn -DskipTests package`
2. `mvn test`
3. `mvn --% -Dsurefire.skip=false -Dtest=tv.dyndns.kishibe.qmaclone.client.util.StringUtilsTest test`

## 6. 失敗時対処
- 失敗時は `StringUtilsTest` 内だけを修正対象とする。
- 広範囲置換は行わない。
- エラー原因が基盤設定の場合のみ `pom.xml` の最小修正を検討する。

## 7. 完了条件
- `StringUtilsTest` が Jupiter で成功実行できる。
- `build` / `test` が成功する。
- 差分が対象クラス中心の最小範囲である。
- 1コミット1目的を維持する。
