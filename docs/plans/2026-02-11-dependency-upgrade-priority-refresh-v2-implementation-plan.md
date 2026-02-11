# 依存関係更新タスク再棚卸し v2 実装計画

## ゴール

優先度 `Security -> Compatibility -> Effect` で確定した上位10件を、1件ずつ `更新 -> build -> test -> コミット` で完了させる。

## タスク一覧

1. 優先度表の再確認と対象10件の確定
2. `mockito-core` の更新（9件目）
3. `mockito-junit-jupiter` の更新（10件目）
4. 連結スモーク実行
5. 結果記録の作成

## 実行ルール

- 依存関係がある作業は直列で実行する。
- 1コミット1目的を維持する。
- 失敗時は「環境要因 / 依存互換性 / 実装修正不足」に分類し、未解決のまま次へ進まない。

## 検証コマンド

```bash
mvn -q -DskipTests test-compile
mvn -q "-Dsurefire.skip=false" "-Dtest=BadUserDetectorTest,ImageProxyServletStubTest,ServiceServletStubTest,ServerStatusWebSocketServletTest,GameStatusWebSocketServletTest" "-DfailIfNoTests=false" test
mvn -q "-Dsurefire.skip=false" "-Dtest=ChatManagerTest,GameTest,ImageProxyServletStubTest,RecognizerZinniaTest" "-DfailIfNoTests=false" test
```

## 補足

`GameManagerTest` は `mockito-core 5.12.0` と `5.21.0` の双方で同一の `NullPointerException` が発生し、今回更新による回帰ではないことを確認した。
