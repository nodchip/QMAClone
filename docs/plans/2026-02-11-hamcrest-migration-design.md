# Hamcrest 置換設計（`hamcrest-all` -> `hamcrest`）

## 1. 目的
- テスト依存の古い集約アーティファクト `hamcrest-all:1.3` を、現行の `hamcrest:2.2` に置換する。
- 変更範囲を最小化し、必要が出た場合のみテストコードを局所修正する。

## 2. 方針
- まず `pom.xml` の依存置換のみを実施する。
- 検証は `build -> test` を直列で実施する。
- エラー発生時のみ、該当ファイルに限定して import / matcher 呼び出しを修正する。
- 1コミット1目的を維持する。

## 3. 対象・非対象
### 3.1 対象
- `pom.xml` の以下置換
1. `org.hamcrest:hamcrest-all:1.3`（`scope=test`）
2. `org.hamcrest:hamcrest:2.2`（`scope=test`）

### 3.2 非対象
- JUnit5 併用化
- Surefire/Failsafe の設定変更
- Hamcrest 以外の依存更新

## 4. 影響評価
- 主な影響点はテストコンパイル時の API 解決。
- 本番コードの実行時クラスパスへの影響は想定しない（`test` スコープ依存のため）。
- `Matchers`、`OrderingComparison`、`BaseMatcher` 等は通常 `hamcrest:2.2` でも利用可能であるため、無修正で通る可能性が高い。

## 5. 検証手順
1. `mvn -DskipTests package` を実行する。
2. `mvn test` を実行する。
3. `git diff --stat` で差分範囲を確認する。

## 6. 失敗時対処
- コンパイルエラーが発生した場合は、エラー箇所のテストファイルのみ修正する。
- 改行コード変換や広範囲自動整形を伴う変更は採用しない。
- 必要時は差分を戻し、局所修正で再適用する。

## 7. 完了条件
- `mvn -DskipTests package` が成功する。
- `mvn test` が成功する（現設定で skip の場合もフェーズ正常終了）。
- 差分が `hamcrest` 置換目的に対して最小範囲である。
