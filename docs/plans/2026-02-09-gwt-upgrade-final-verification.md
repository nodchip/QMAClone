# GWT関連更新 最終検証結果

## 実施ブランチ

- `gwt-upgrade-java25`

## 実施コマンドと結果

1. `mvn clean compile`
- 結果: `BUILD SUCCESS`
- 条件: `JAVA_HOME=C:\Program Files\Java\jdk-25.0.2`

2. `mvn test`
- 結果: `BUILD SUCCESS`
- 補足: `maven-surefire-plugin` の設定で `Tests are skipped.`

3. `mvn -DskipTests "-Dgwt.skipCompilation=false" gwt:compile`
- 結果: `BUILD SUCCESS`
- 条件: `JAVA_TOOL_OPTIONS` に以下が必要
  - `--add-opens=java.base/java.lang=ALL-UNNAMED`
  - `--add-opens=java.base/java.lang.reflect=ALL-UNNAMED`
  - `--add-opens=java.base/java.io=ALL-UNNAMED`

## 反映した主な変更

- `pom.xml`
  - `maven.compiler.source/target` を `25` に変更
  - `gwt-maven-plugin` の `sourceLevel` を `11` に変更
  - `gwt-maven-plugin` に `jvmArgs`（add-opens）を追加
- `docs/plans/2026-02-09-gwt-upgrade-baseline-results.md`
  - Java 25 実行条件と検証結果を追記
- `docs/plans/2026-02-09-gwt-upgrade-high-risk-assessment.md`
  - 高リスク依存の現状維持判断と Java 25 条件を追記

## 手動確認（未実施）

- DevMode 画面表示
- Tomcat 配備後の画面表示
- RPC 応答
- WebSocket 101

## 結論

- Java 25 での Maven build/test は成功。
- GWT compile も add-opens 付与で成功。
- 実運用前に DevMode/Tomcat の手動スモーク確認が必要。
