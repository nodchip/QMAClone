# 高リスク依存 更新可否メモ

## 対象

- `com.google.gwt:gwt-incubator`
- `local.legacy:gwt-plus`
- `local.legacy:gwt-visualization`

## 調査結果

- `gwt-incubator`
  - `2.0.1` は取得可能
  - `2.0.2` は Maven Central に存在せず取得不可
  - 現行 `2.0.1` 維持
- `gwt-plus`
  - `system scope` のローカル JAR (`third_party/gwt-plus-v1-0.3-alpha.jar`) に依存
  - Maven で更新候補を機械的取得できない
  - 現行維持
- `gwt-visualization`
  - `system scope` のローカル JAR (`third_party/gwt-visualization-1.1.2.jar`) に依存
  - Maven で更新候補を機械的取得できない
  - 現行維持

## 影響

- 高リスク依存はこのバッチではバージョン変更なし。
- ただし GWT 本体更新後も以下は成功:
  - `mvn -DskipTests compile`
  - `JAVA_TOOL_OPTIONS=--add-opens... mvn -DskipTests "-Dgwt.skipCompilation=false" gwt:compile`
  - `mvn test`
- 注意:
  - Java 25 では Piriti generator（Guice/CGLIB）が強カプセル化に抵触するため、
    `gwt:compile` 実行時に `JAVA_TOOL_OPTIONS` で `--add-opens` 指定が必要。

## 次フェーズ案

- `gwt-plus` / `gwt-visualization` の Maven 管理化（社内リポジトリまたは置換）を別タスク化。
- `gwt-incubator` は API 置換を含む中期タスクとして扱う。
