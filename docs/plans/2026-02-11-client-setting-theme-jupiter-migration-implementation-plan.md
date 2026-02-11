# Client Setting Theme Jupiter Migration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `client/setting/theme` 配下の 3 テストを JUnit4 から JUnit5（Jupiter）へ最小差分で移行する。

**Architecture:** 対象を `ThemeProviderTest` / `ThemeQueryProviderTest` / `ThemeCellTest` に限定し、JUnit import / annotation を Jupiter 化する。既存 JUnit5 併用基盤を利用し、`build -> test -> 対象明示実行` を直列で検証する。

**Tech Stack:** Maven, JUnit4, JUnit5 (Jupiter), Maven Surefire

---

### Task 1: 事前確認

**Files:**
- Modify: なし

**Step 1: 作業状態を確認**

Run: `git status --short`  
Expected: 想定外差分なし

**Step 2: 対象3クラスを確認**

Run:
```bash
Get-Content src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeProviderTest.java
Get-Content src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeQueryProviderTest.java
Get-Content src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeCellTest.java
```
Expected: JUnit4 依存箇所（import / annotation）を把握できる

### Task 2: `ThemeProviderTest` を Jupiter 化

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeProviderTest.java`

**Step 1: JUnit API 置換**

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions`
- 必要に応じて `@RunWith` / `@Rule` 対応

**Step 2: 差分確認**

Run: `git diff -- src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeProviderTest.java`

### Task 3: `ThemeQueryProviderTest` を Jupiter 化

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeQueryProviderTest.java`

**Step 1: JUnit API 置換**

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions`
- 必要に応じて `@RunWith` / `@Rule` 対応

**Step 2: 差分確認**

Run: `git diff -- src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeQueryProviderTest.java`

### Task 4: `ThemeCellTest` を Jupiter 化

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeCellTest.java`

**Step 1: JUnit API 置換**

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions`
- 必要に応じて `@RunWith` / `@Rule` 対応

**Step 2: 差分確認**

Run: `git diff -- src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeCellTest.java`

### Task 5: 逐次検証

**Files:**
- Modify: 必要時のみ対象3ファイル

**Step 1: build**

Run: `mvn -DskipTests package`  
Expected: `BUILD SUCCESS`

**Step 2: 既定 test**

Run: `mvn test`  
Expected: `BUILD SUCCESS`

**Step 3: 対象3クラス明示実行**

Run:
```bash
mvn --% -Dsurefire.skip=false -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeProviderTest,tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeQueryProviderTest,tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeCellTest -DfailIfNoTests=false test
```
Expected: 対象3クラス成功（Failures/Errors 0）

### Task 6: コミット

**Files:**
- Modify:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeProviderTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeQueryProviderTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeCellTest.java`

**Step 1: 最終差分確認**

Run: `git diff --stat`

**Step 2: コミット**

```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeProviderTest.java
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeQueryProviderTest.java
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeCellTest.java
git commit -m "client/setting/themeテストをJUnit5へ移行"
```

### Task 7: 完了報告

**Step 1: 報告整理**

1. 変更ファイル
2. 実行コマンド
3. 成功/失敗
4. 既知制約（必要時）
