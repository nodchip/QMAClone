# GWT更新ベースライン検証結果

- 実施日: 2026-02-10
- 作業ディレクトリ: `.worktrees/gwt-upgrade-java25`

## 実行コマンド結果

1. `mvn -q -DskipTests dependency:tree > target/dependency-tree-before.txt`
- 結果: 成功
- 出力: `target/dependency-tree-before.txt`

2. `mvn clean compile`
- 結果: `BUILD SUCCESS`
- 補足: `javac [debug target 1.8]` が表示され、現行設定の `source/target` は 1.8

3. `mvn test`
- 結果: `BUILD SUCCESS`
- 補足: surefire は `Tests are skipped.`（既定設定）

4. `mvn -DskipTests gwt:compile`
- 結果: `BUILD SUCCESS`
- 補足: `GWT compilation is skipped`（`gwt.skipCompilation=true` が有効）

## 手動確認（本端末では未実施）
- DevMode: `http://127.0.0.1:8888/QMAClone.html`
- Tomcat: 配備URL
- 確認観点:
  - 画面表示可否
  - WebSocket の 101/404/500
  - 主要導線（ログイン・ロビー・チャット）

## Java 25 + GWT compile 補足

- `maven.compiler.source/target=25`、`gwt sourceLevel=11` で検証。
- `gwt:compile` は追加オプションなしだと Guice/CGLIB の `InaccessibleObjectException` で失敗。
- 次の `JAVA_TOOL_OPTIONS` を付与すると `gwt:compile` が成功。

```powershell
$env:JAVA_TOOL_OPTIONS='--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED'
mvn -DskipTests "-Dgwt.skipCompilation=false" gwt:compile
```
