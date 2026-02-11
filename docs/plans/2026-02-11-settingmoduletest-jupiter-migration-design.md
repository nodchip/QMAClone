# SettingModuleTest Jupiter移行 設計

## 概要
- 対象は `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/SettingModuleTest.java` のみ。
- 目的は、JUnit4 依存を JUnit5（Jupiter）へ最小差分で移行すること。
- テスト本文・期待値は変更しない。

## 背景
- `client/setting` 配下の JUnit5 移行を継続しており、本テストが最後の残件。
- 目的を明確化するため、今回はフレームワーク移行のみ実施する。

## 対象範囲
- 変更対象:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/SettingModuleTest.java`
- 非対象:
  - 本番コード
  - 他テスト
  - コメント文字化け修正（別コミットで扱う）

## 設計方針
- JUnit import の置換のみを実施する。
- テストロジック（`SettingModule` 生成 -> provider 呼び出し -> `instanceof` 判定）は不変。

## 変更方針（詳細）
1. import 置換
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Assert.assertTrue` -> `org.junit.jupiter.api.Assertions.assertTrue`

2. ロジック不変
- テストメソッド名、アサーション条件、期待クラスは変更しない。

## データフローと検証観点
- `provideExternalAccountConnector()` が `GoogleExternalAccountConnector` を返すことを検証する。
- 移行前後で同一の観点を維持する。

## リスクと対策
- リスク: import 置換漏れによるコンパイルエラー。
- 対策: `build -> test` を直列で実行し確認する。

## 完了条件
- 当該テストが Jupiter でコンパイル・実行できる。
- 既存検証意図が維持される。
- 変更ファイルが対象テスト1ファイルに限定される。

## 検証手順
1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.SettingModuleTest test`

## コミット方針
- 1コミット1目的を維持し、設計ドキュメントのみを先にコミットする。