# 依存関係更新タスク再棚卸し・上位3件実装 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 依存関係を再棚卸しして優先度上位3件を直列で更新し、`build -> test` の成功ログ付きで完了させる。

**Architecture:** まず `versions-maven-plugin` の出力を固定化して優先度表を作成し、`Security -> Compatibility -> Effect` で上位3件を確定する。次に 1件ずつ「更新・最小修正・検証・記録」を実行し、未解決失敗がある場合は次件へ進まない。最後に連結検証と結果ドキュメントで完了判定する。

**Tech Stack:** Maven, Java 25, JUnit 5/JUnit Vintage, Guice, JNA, Commons FileUpload

---

### Task 1: 優先度表を作成して上位3件を確定する

**Files:**
- Create: `docs/plans/2026-02-11-dependency-upgrade-priority-refresh-priority-table.md`
- Read: `pom.xml`

**Step 1: 候補一覧を固定化する**

Run:
```bash
mvn -DskipTests versions:display-dependency-updates > .dependency-updates.txt
```

Expected: `.dependency-updates.txt` に更新候補が出力される

**Step 2: 優先度表のドラフトを作る**

`docs/plans/2026-02-11-dependency-upgrade-priority-refresh-priority-table.md` を作成し、最低限次の列を持つ表を書く。

```markdown
| Target | Current -> Target | Security | Compatibility | Effect | Priority |
| --- | --- | --- | --- | --- | --- |
```

**Step 3: 上位3件を確定する**

この計画では次を初期候補とする（必要なら Step 4 で調整）。
1. `commons-fileupload:commons-fileupload` `1.5 -> 1.6.0`（Security）
2. `com.google.inject:*` `4.2.3 -> 7.0.0`（Compatibility）
3. `net.java.dev.jna:*` `5.5.0 -> 5.18.1`（Effect + runtime compatibility）

**Step 4: Commit**

```bash
git add docs/plans/2026-02-11-dependency-upgrade-priority-refresh-priority-table.md
git commit -m "依存関係更新の優先度表と上位3件を確定"
```

### Task 2: Security最優先の FileUpload 系更新を実装する

**Files:**
- Modify: `pom.xml`
- Test: `src/main/java/tv/dyndns/kishibe/qmaclone/server/IconUploadServletStub.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/ImageProxyServletStubTest.java`

**Step 1: 失敗を先に確認する（現状）**

Run:
```bash
mvn -q -DskipTests test-compile
```

Expected: PASS（基準点）

**Step 2: 依存を更新する**

`pom.xml` を次のように更新する。

```xml
<dependency>
  <groupId>commons-fileupload</groupId>
  <artifactId>commons-fileupload</artifactId>
  <version>1.6.0</version>
</dependency>
<dependency>
  <groupId>commons-io</groupId>
  <artifactId>commons-io</artifactId>
  <version>2.21.0</version>
</dependency>
```

**Step 3: 影響箇所を最小確認する**

Run:
```bash
mvn -q -DskipTests test-compile
mvn -q "-Dsurefire.skip=false" "-Dtest=ImageProxyServletStubTest" "-DfailIfNoTests=false" test
```

Expected: PASS

**Step 4: Commit**

```bash
git add pom.xml
git commit -m "commons-fileuploadとcommons-ioを更新"
```

### Task 3: Guice 系を 7.0.0 に更新し、注入経路を安定化する

**Files:**
- Modify: `pom.xml`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/server/testing/GuiceInjectionExtension.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/server/testing/QMACloneTestEnv.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/server/**/*Test.java`（GuiceBerryRule 依存クラス）

**Step 1: 依存更新を適用する**

`pom.xml` で以下を更新する。

```xml
<dependency>
  <groupId>com.google.inject</groupId>
  <artifactId>guice</artifactId>
  <version>7.0.0</version>
</dependency>
<dependency>
  <groupId>com.google.inject.extensions</groupId>
  <artifactId>guice-assistedinject</artifactId>
  <version>7.0.0</version>
</dependency>
<dependency>
  <groupId>com.google.inject.extensions</groupId>
  <artifactId>guice-multibindings</artifactId>
  <version>7.0.0</version>
</dependency>
```

**Step 2: 失敗テストを書いてから移行する**

GuiceBerryRule を使う代表テスト（例: `GameTest`）を1件選び、`@ExtendWith(GuiceInjectionExtension.class)` へ変更して注入経路を Jupiter 側へ寄せる。

```java
@ExtendWith(GuiceInjectionExtension.class)
public class GameTest { ... }
```

**Step 3: 代表テストで失敗/成功を確認する**

Run:
```bash
mvn -q "-Dsurefire.skip=false" "-Dtest=GameTest" "-DfailIfNoTests=false" test
```

Expected:
- 変更前: 注入失敗または Rule 非適用
- 変更後: PASS

**Step 4: 残りの GuiceBerryRule 依存テストを同方式で移行する**

Run:
```bash
rg -n "GuiceBerryRule" src/test/java
```

Expected: 最終的に0件

**Step 5: Commit**

```bash
git add pom.xml src/test/java/tv/dyndns/kishibe/qmaclone/server/testing/GuiceInjectionExtension.java src/test/java/tv/dyndns/kishibe/qmaclone/server/testing/QMACloneTestEnv.java src/test/java
git commit -m "Guice 7へ更新しGuiceBerryRule依存テストをJupiter注入へ移行"
```

### Task 4: JNA 5.18.1 更新と zinnia 実行確認を実施する

**Files:**
- Modify: `pom.xml`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/handwriting/RecognizerZinniaTest.java`

**Step 1: JNA 依存を更新する**

`pom.xml` を次のように更新する。

```xml
<dependency>
  <groupId>net.java.dev.jna</groupId>
  <artifactId>jna</artifactId>
  <version>5.18.1</version>
</dependency>
<dependency>
  <groupId>net.java.dev.jna</groupId>
  <artifactId>jna-platform</artifactId>
  <version>5.18.1</version>
</dependency>
```

**Step 2: ネイティブ依存テストを実行する**

Run:
```bash
mvn -q "-Dsurefire.skip=false" "-Dtest=RecognizerZinniaTest" "-DfailIfNoTests=false" test
```

Expected: DLLありなら PASS、なしなら SKIP（既存ゲート）

**Step 3: Commit**

```bash
git add pom.xml
git commit -m "JNA依存を5.18.1へ更新"
```

### Task 5: 連結検証と結果記録

**Files:**
- Create: `docs/plans/2026-02-11-dependency-upgrade-priority-refresh-results.md`

**Step 1: 連結検証を実行する**

Run:
```bash
mvn -q -DskipTests test-compile
mvn -q "-Dsurefire.skip=false" "-Dtest=ChatManagerTest,GameTest,ImageProxyServletStubTest,RecognizerZinniaTest" "-DfailIfNoTests=false" test
```

Expected: PASS（または zinnia だけ SKIP）

**Step 2: 結果ドキュメントを作成する**

`docs/plans/2026-02-11-dependency-upgrade-priority-refresh-results.md` に次を記録する。
1. 実行コマンド
2. 成否
3. 失敗分類（環境要因/依存互換性/実装修正不足）
4. 次アクション

**Step 3: Commit**

```bash
git add docs/plans/2026-02-11-dependency-upgrade-priority-refresh-results.md
git commit -m "依存関係更新上位3件の検証結果を記録"
```
