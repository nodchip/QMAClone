# ThemeQueryCellTest Jupiter移行 設計

## 概要
- 対象は `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeQueryCellTest.java` のみとする。
- 目的は、テスト意図を変えずに JUnit4 依存を JUnit5（Jupiter）へ置換すること。
- 変更は最小範囲とし、Hamcrest アサーションは現状維持する。

## 背景
- `client/setting/theme` 配下の主要テストは順次 Jupiter 化済みであり、残件の `ThemeQueryCellTest` も同様の統一が必要。
- 小粒な単体移行により、失敗時の切り分けコストを最小化する。

## 対象範囲
- 変更対象:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeQueryCellTest.java`
- 非対象:
  - 本番コード
  - 他テスト
  - `pom.xml` などビルド設定

## 設計方針
- JUnit4 アノテーション・ランナーを削除し、Jupiter のアノテーションへ置換する。
- テストロジック（セットアップ、`render` 呼び出し、出力検証）は不変とする。
- アサーションは `containsString` を継続利用し、期待値・意味を変更しない。

## 変更方針（詳細）
1. import 置換
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Assert.assertThat` -> `org.hamcrest.MatcherAssert.assertThat`

2. ランナー削除
- `@RunWith(JUnit4.class)` を削除。
- `org.junit.runner.RunWith` / `org.junit.runners.JUnit4` の import を削除。

3. ライフサイクル注釈置換
- `@Before` を `@BeforeEach` に置換。

## データフローと検証観点
- `setUp` で `PacketThemeQuery` にテーマ・クエリを設定。
- `ThemeQueryCell#render` 実行後、`SafeHtmlBuilder` 文字列に `FAKE_QUERY` が含まれることを検証。
- 上記フローは移行前後で同一。

## リスクと対策
- リスク: import 置換ミスによるコンパイルエラー。
- 対策: 変更後に `build -> test` を直列実行して確認する。

## 完了条件
- 当該テストが Jupiter でコンパイル・実行できる。
- 既存のテスト意図（クエリ出力検証）が維持される。
- 変更ファイルが対象テスト1ファイルに限定される。

## 検証手順
1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeQueryCellTest test`

## コミット方針
- 1コミット1目的を維持し、設計ドキュメントのみを先にコミットする。