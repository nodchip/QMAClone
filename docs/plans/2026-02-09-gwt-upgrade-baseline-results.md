# GWT譖ｴ譁ｰ繝吶・繧ｹ繝ｩ繧､繝ｳ讀懆ｨｼ邨先棡

- 螳滓命譌･: 2026-02-10
- 菴懈･ｭ繝・ぅ繝ｬ繧ｯ繝医Μ: `.worktrees/gwt-upgrade-java25`

## 螳溯｡後さ繝槭Φ繝臥ｵ先棡

1. `mvn -q -DskipTests dependency:tree > target/dependency-tree-before.txt`
- 邨先棡: 謌仙粥
- 蜃ｺ蜉・ `target/dependency-tree-before.txt`

2. `mvn clean compile`
- 邨先棡: `BUILD SUCCESS`
- 陬懆ｶｳ: `javac [debug target 1.8]` 縺ｧ螳溯｡後＆繧後∫樟譎らせ縺ｮ `source/target` 縺ｯ 1.8

3. `mvn test`
- 邨先棡: `BUILD SUCCESS`
- 陬懆ｶｳ: surefire 縺ｯ `Tests are skipped.`・育樟陦瑚ｨｭ螳夲ｼ・
4. `mvn -DskipTests gwt:compile`
- 邨先棡: `BUILD SUCCESS`
- 陬懆ｶｳ: `GWT compilation is skipped`・・gwt.skipCompilation=true` 縺梧怏蜉ｹ・・
## 謇句虚遒ｺ隱搾ｼ域悽遶ｯ譛ｫ縺ｧ縺ｯ譛ｪ螳滓命・・
- DevMode: `http://127.0.0.1:8888/QMAClone.html`
- Tomcat: 驟榊ｙURL
- 遒ｺ隱崎ｦｳ轤ｹ:
  - 逕ｻ髱｢陦ｨ遉ｺ蜿ｯ蜷ｦ
  - WebSocket 縺ｮ 101/404/500
  - 荳ｻ隕∝ｰ守ｷ夲ｼ医Ο繧ｰ繧､繝ｳ繝ｻ繝ｭ繝薙・繝ｻ繝√Ε繝・ヨ・・

## Java 25 + GWT compile 補足

- `maven.compiler.source/target=25`、`gwt sourceLevel=11` で検証。
- `gwt:compile` は追加オプションなしだと Guice/CGLIB の `InaccessibleObjectException` で失敗。
- 次の `JAVA_TOOL_OPTIONS` を付与すると `gwt:compile` が成功。

```powershell
$env:JAVA_TOOL_OPTIONS='--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED'
mvn -DskipTests "-Dgwt.skipCompilation=false" gwt:compile
```