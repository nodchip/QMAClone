# 非GWTサーバーテスト再投入パイプライン 検証結果

- 日付: 2026-02-11
- ブランチ: `feature/non-gwt-junit5-reentry-pipeline`
- 判定: 継続可能（段階ゲート通過）

## 実行結果サマリ

| 段階 | コマンド | 結果 | 分類 | 次アクション |
| --- | --- | --- | --- | --- |
| Baseline 前提確認 | `mvn -q "-Dsurefire.skip=false" "-Dtest=ChatManagerTest" "-DfailIfNoTests=false" test` | PASS | - | 継続 |
| Stage-2 事前失敗再現 | `mvn -q "-Dsurefire.skip=false" "-Dtest=FullTextSearchTest" "-DfailIfNoTests=false" test` | FAIL (`fullTextSearch == null`) | 実装修正 | Jupiter 注入へ切替 |
| Stage-2 修正後 | `mvn -q "-Dsurefire.skip=false" "-Dtest=FullTextSearchTest" "-DfailIfNoTests=false" test` | PASS | - | 継続 |
| Stage-1 単体 | `mvn -q "-Dsurefire.skip=false" "-Dtest=DatabaseReentrySmokeTest" "-DfailIfNoTests=false" test` | PASS | - | 継続 |
| Stage-1 既存代表 | `mvn -q "-Dsurefire.skip=false" "-Dtest=DatabaseTest#testUserData" "-DfailIfNoTests=false" test` | PASS | - | 継続 |
| Stage-3 単体 | `mvn -q "-Dsurefire.skip=false" "-Dtest=RecognizerZinniaTest" "-DfailIfNoTests=false" test` | PASS | - | 継続 |
| 連結検証 | `mvn -q "-Dsurefire.skip=false" "-Dtest=ChatManagerTest,DatabaseReentrySmokeTest,FullTextSearchTest,RecognizerZinniaTest" "-DfailIfNoTests=false" test` | PASS | - | 継続 |

## 失敗分類ログ

1. `FullTextSearchTest` 初回失敗
- 症状: `NullPointerException` (`this.fullTextSearch` が null)
- 分類: 実装修正
- 原因: JUnit5 で `@Rule` の `GuiceBerryRule` が機能せず DI 注入されない
- 対応: `GuiceInjectionExtension` へ移行し、Jupiter の注入経路へ統一

## 実施した安定化変更

1. JDK 25 実行互換性
- `pom.xml` の `argLine` に `--add-opens java.base/java.lang=ALL-UNNAMED` を追加

2. FullTextSearch の再投入安定化
- `qmaclone.lucene.index.dir` でインデックス保存先を上書き可能化
- テスト側で一時ディレクトリを設定し、実行間の汚染を抑制

3. Database 再投入ゲート
- `DatabaseReentrySmokeTest` を追加（ユーザーデータ往復・問題登録取得・検索ヒット）

4. zinnia ネイティブ依存ゲート
- `RecognizerZinniaTest` に `zinnia.dll` 存在確認の前提チェックを追加
- 不在時は失敗ではなく skip 扱いに変更
