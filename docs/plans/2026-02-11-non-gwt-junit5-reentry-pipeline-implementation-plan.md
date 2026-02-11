# 非GWTサーバーテスト再投入パイプライン Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `DatabaseTest` / `FullTextSearchTest` / `RecognizerZinniaTest` を、既存安定バッチを壊さず段階的に再投入できる状態へ移行する。

**Architecture:** まず JDK 25 での Guice 実行互換性を回復し、Baseline Gate を実行可能にする。その上で「DB最小データ」「FTS一時インデックス」「zinnia.dll 事前ゲート」を独立モジュールとして実装し、各段階で単体成功 -> 連結成功を確認する。失敗は `環境エラー / 選定不適合 / 実装修正` に分類して記録する。

**Tech Stack:** Java 8 source + Maven Surefire + JUnit 5 (Jupiter) + Guice/GuiceBerry + Lucene + JNA (zinnia)

---

### Task 1: Baseline Gate を JDK 25 で実行可能にする

**Files:**
- Modify: `pom.xml`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/ChatManagerTest.java`

**Step 1: 失敗を再現する**

Run: `mvn -q "-Dsurefire.skip=false" "-Dtest=ChatManagerTest" "-DfailIfNoTests=false" test`  
Expected: FAIL with `InaccessibleObjectException` (`java.lang.ClassLoader#defineClass`)

**Step 2: 最小修正を追加する**

`pom.xml` の `argLine` に次を追加する。

```xml
--add-opens java.base/java.lang=ALL-UNNAMED
```

必要最小限で開始し、他モジュール open は失敗ログが出た場合にのみ追加する。

**Step 3: 再実行して回復を確認する**

Run: `mvn -q "-Dsurefire.skip=false" "-Dtest=ChatManagerTest" "-DfailIfNoTests=false" test`  
Expected: `InaccessibleObjectException` が消える（他要因失敗なら分類して記録）

**Step 4: Commit**

```bash
git add pom.xml
git commit -m "JDK25でGuice実行に必要なadd-opensを追加"
```

### Task 2: FullTextSearch のインデックス保存先をテストで差し替え可能にする

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/database/FullTextSearch.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/database/FullTextSearchTest.java`

**Step 1: 失敗テスト（または不安定性）を固定化する**

`FullTextSearchTest` に「インデックスディレクトリが固定絶対パスでないこと」を検証するテストを追加する。

```java
@Test
void indexDirectoryShouldBeOverridableForTest() {
  Path dir = FullTextSearch.resolveIndexDirectory();
  assertNotNull(dir);
}
```

**Step 2: 最小実装を追加する**

`FullTextSearch` の固定値を直接参照せず、次優先で解決するメソッドを追加する。
1. `System.getProperty("qmaclone.lucene.index.dir")`
2. 既存デフォルト (`/home/tomcat/qmaclone/lucene`)

```java
static Path resolveIndexDirectory() {
  String overridden = System.getProperty("qmaclone.lucene.index.dir");
  if (overridden != null && !overridden.isEmpty()) {
    return FileSystems.getDefault().getPath(overridden);
  }
  return DEFAULT_INDEX_FILE_DIRECTORY;
}
```

**Step 3: テスト側で一時ディレクトリを使う**

`@BeforeEach` で `Files.createTempDirectory(...)` を作り、  
`System.setProperty("qmaclone.lucene.index.dir", tempDir.toString())` を設定。  
`@AfterEach` で property を clear し、作成物を削除する。

**Step 4: 検証**

Run: `mvn -q "-Dsurefire.skip=false" "-Dtest=FullTextSearchTest" "-DfailIfNoTests=false" test`  
Expected: PASS（実行ごとにインデックス汚染しない）

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/database/FullTextSearch.java src/test/java/tv/dyndns/kishibe/qmaclone/server/database/FullTextSearchTest.java
git commit -m "FullTextSearchのインデックス保存先をテストで上書き可能にする"
```

### Task 3: Database 再投入用の最小スモークを追加する

**Files:**
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseReentrySmokeTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseTest.java`

**Step 1: 失敗テストを書く（最小期待値）**

`DatabaseReentrySmokeTest` に 2-3 ケースを追加する。
- `setUserData/getUserData` 往復
- `addProblem/getProblem` 往復
- `searchProblem` の基本1件ヒット

```java
@Test
void addAndLoadProblemShouldRoundTrip() throws Exception { ... }
```

**Step 2: 必要最小限のデータ初期化を導入する**

テストごとに干渉しないよう、`@BeforeEach` で対象テーブルを限定初期化する。  
既存巨大 `DatabaseTest` の全件再投入はこの段階では行わない。

**Step 3: 検証**

Run: `mvn -q "-Dsurefire.skip=false" "-Dtest=DatabaseReentrySmokeTest" "-DfailIfNoTests=false" test`  
Expected: PASS

**Step 4: 連結検証**

Run: `mvn -q "-Dsurefire.skip=false" "-Dtest=ChatManagerTest,DatabaseReentrySmokeTest" "-DfailIfNoTests=false" test`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseReentrySmokeTest.java src/test/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseTest.java
git commit -m "Database再投入の最小スモークテストを追加"
```

### Task 4: RecognizerZinnia の DLL ゲートを追加する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/server/handwriting/RecognizerZinniaTest.java`

**Step 1: 前提チェックを先に書く**

`zinnia.dll` が見つからない場合は失敗ではなく skip とする。

```java
@BeforeEach
void requireZinniaNative() {
  assumeTrue(isZinniaAvailable(), "zinnia.dll が見つからないためスキップ");
}
```

**Step 2: 最小推論1ケースを明示**

既存 `testRecognize` を「再投入ゲート」として残し、ランダム負荷系は別扱いにする（必要なら `@Tag("slow")`）。

**Step 3: 検証**

Run: `mvn -q "-Dsurefire.skip=false" "-Dtest=RecognizerZinniaTest" "-DfailIfNoTests=false" test`  
Expected: DLL ありなら PASS、なしなら SKIPPED

**Step 4: Commit**

```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/server/handwriting/RecognizerZinniaTest.java
git commit -m "RecognizerZinniaTestにネイティブDLLゲートを追加"
```

### Task 5: 再投入パイプライン連結検証と記録

**Files:**
- Create: `docs/plans/2026-02-11-non-gwt-junit5-reentry-pipeline-results.md`

**Step 1: 段階実行（必ず直列）**

Run:
1. `mvn -q "-Dsurefire.skip=false" "-Dtest=ChatManagerTest" "-DfailIfNoTests=false" test`
2. `mvn -q "-Dsurefire.skip=false" "-Dtest=DatabaseReentrySmokeTest" "-DfailIfNoTests=false" test`
3. `mvn -q "-Dsurefire.skip=false" "-Dtest=FullTextSearchTest" "-DfailIfNoTests=false" test`
4. `mvn -q "-Dsurefire.skip=false" "-Dtest=RecognizerZinniaTest" "-DfailIfNoTests=false" test`
5. `mvn -q "-Dsurefire.skip=false" "-Dtest=ChatManagerTest,DatabaseReentrySmokeTest,FullTextSearchTest,RecognizerZinniaTest" "-DfailIfNoTests=false" test`

**Step 2: 結果記録**

`docs/plans/2026-02-11-non-gwt-junit5-reentry-pipeline-results.md` に以下を記録する。
- 実行コマンド
- PASS/FAIL/SKIP
- 失敗分類（環境エラー/選定不適合/実装修正）
- 次アクション

**Step 3: Commit**

```bash
git add docs/plans/2026-02-11-non-gwt-junit5-reentry-pipeline-results.md
git commit -m "非GWT再投入パイプラインの検証結果を記録"
```
