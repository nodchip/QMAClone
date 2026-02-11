# 依存関係更新タスク再棚卸し v2 設計（2026-02-11）

## 目的

古い依存関係の更新タスクを再棚卸しし、`Security -> Compatibility -> Effect` の順で優先度を付け、上位10件を高優先から直列実行する。

## 前提と方針

- 優先度軸: `Security -> Compatibility -> Effect`
- 実行件数: 上位10件
- 実行方式: 1件ずつ `更新 -> build -> test -> コミット`
- 対象範囲: 依存 + `dependencyManagement` + plugin
- バージョン方針: 安定版のみ（`-M` / `-RC` / `-alpha` は除外）

## 上位10件（確定）

1. `commons-fileupload` `1.5 -> 1.6.0`
2. `commons-io` `2.4 -> 2.21.0`
3. `guice` `4.2.3 -> 7.0.0`
4. `guice-assistedinject` `4.2.3 -> 7.0.0`
5. `guava` `28.0-jre -> 33.5.0-jre`
6. `guava-gwt` `28.0-jre -> 33.5.0-jre`
7. `jna` `5.5.0 -> 5.18.1`
8. `jna-platform` `5.5.0 -> 5.18.1`
9. `mockito-core` `5.12.0 -> 5.21.0`
10. `mockito-junit-jupiter` `5.12.0 -> 5.21.0`

## 実行状況（再棚卸し時点）

- 完了済み: 1〜8
- 未実行: 9〜10

## 実行フロー

各依存ごとに、以下を完了条件とする。

1. `pom.xml` を対象依存のみ更新する。
2. `mvn -q -DskipTests test-compile` を実行する。
3. 対象依存に関係する最小テストを実行する。
4. 問題なければ 1件1コミットで確定する。

失敗時は原因を「環境要因 / 依存互換性 / 実装修正不足」に分類し、未解決のまま次件へ進まない。

## 検証対象（今回）

- Mockito 更新時: `ImageProxyServletStubTest` と Mockito 利用テストの最小セット
- 連結スモーク: `ChatManagerTest,GameTest,ImageProxyServletStubTest,RecognizerZinniaTest`

## 成果物

- 本設計: `docs/plans/2026-02-11-dependency-upgrade-priority-refresh-v2-design.md`
- 実装計画: `docs/plans/2026-02-11-dependency-upgrade-priority-refresh-v2-implementation-plan.md`
- 実行結果: `docs/plans/2026-02-11-dependency-upgrade-priority-refresh-v2-results.md`
