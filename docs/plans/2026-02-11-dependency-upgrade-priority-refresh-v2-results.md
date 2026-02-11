# 依存関係更新タスク再棚卸し v2 実行結果

## 1. 実行内容

- 9件目: `mockito-core` を `5.12.0 -> 5.21.0` に更新
- 10件目: `mockito-junit-jupiter` を `5.12.0 -> 5.21.0` に更新

## 2. 実行コマンド

```bash
mvn -q -DskipTests test-compile
mvn -q "-Dsurefire.skip=false" "-Dtest=BadUserDetectorTest,ImageProxyServletStubTest,ServiceServletStubTest,ServerStatusWebSocketServletTest,GameStatusWebSocketServletTest" "-DfailIfNoTests=false" test
mvn -q -DskipTests test-compile
mvn -q "-Dsurefire.skip=false" "-Dtest=BadUserDetectorTest,ImageProxyServletStubTest,ServiceServletStubTest,ServerStatusWebSocketServletTest,GameStatusWebSocketServletTest" "-DfailIfNoTests=false" test
mvn -q -DskipTests test-compile
mvn -q "-Dsurefire.skip=false" "-Dtest=ChatManagerTest,GameTest,ImageProxyServletStubTest,RecognizerZinniaTest" "-DfailIfNoTests=false" test
```

## 3. 成否

- 9件目: 成功（build/test ともに成功）
- 10件目: 成功（build/test ともに成功）
- 連結スモーク: 成功

## 4. 切り分け記録

- `GameManagerTest` は `mockito-core 5.12.0` でも同一失敗（`GameManager.getOrCreateMatchingSession` 起点の `NullPointerException`）を再現。
- よって今回の Mockito 更新回帰ではなく、既存不安定要因として別タスク化する。

## 5. 上位10件の達成状況

1. `commons-fileupload` `1.5 -> 1.6.0` 完了
2. `commons-io` `2.4 -> 2.21.0` 完了
3. `guice` `4.2.3 -> 7.0.0` 完了
4. `guice-assistedinject` `4.2.3 -> 7.0.0` 完了
5. `guava` `28.0-jre -> 33.5.0-jre` 完了
6. `guava-gwt` `28.0-jre -> 33.5.0-jre` 完了
7. `jna` `5.5.0 -> 5.18.1` 完了
8. `jna-platform` `5.5.0 -> 5.18.1` 完了
9. `mockito-core` `5.12.0 -> 5.21.0` 完了
10. `mockito-junit-jupiter` `5.12.0 -> 5.21.0` 完了
