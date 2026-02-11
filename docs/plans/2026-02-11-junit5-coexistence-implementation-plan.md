# JUnit5 Coexistence Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** JUnit4 資産を維持したまま JUnit5 を実行可能にする併用基盤を導入する。

**Architecture:** `pom.xml` のテスト依存と Surefire を更新し、JUnit Platform で JUnit4/5 共存実行を成立させる。テストコード本体は原則変更せず、`build -> test` を直列で検証する。

**Tech Stack:** Maven, JUnit4, JUnit5 (Jupiter), JUnit Vintage, Maven Surefire

---

### Task 1: 事前確認

**Files:**
- Modify: なし
- Test: なし

**Step 1: 作業状態を確認**

Run: `git status --short`  
Expected: 想定外差分がない

**Step 2: 現行設定を確認**

Run: `rg -n "junit|surefire|vintage|jupiter" pom.xml`  
Expected: 現行の JUnit/Surefire 定義が把握できる

### Task 2: 依存追加（JUnit5 + Vintage）

**Files:**
- Modify: `pom.xml`
- Test: なし

**Step 1: 依存を追加**

`test` スコープで以下を追加する。
- `org.junit.jupiter:junit-jupiter`
- `org.junit.vintage:junit-vintage-engine`

**Step 2: 差分確認**

Run: `git diff -- pom.xml`  
Expected: 対象依存追加のみ

### Task 3: Surefire 更新

**Files:**
- Modify: `pom.xml`
- Test: なし

**Step 1: Surefire を JUnit Platform 対応版へ更新**

`maven-surefire-plugin` を 3 系（例: `3.2.5`）へ更新する。

**Step 2: 設定差分確認**

Run: `git diff -- pom.xml`  
Expected: Surefire 更新差分が最小である

### Task 4: 逐次検証

**Files:**
- Modify: 必要時のみ（局所修正）
- Test: 実行のみ

**Step 1: build**

Run: `mvn -DskipTests package`  
Expected: `BUILD SUCCESS`

**Step 2: test**

Run: `mvn test`  
Expected: `BUILD SUCCESS`

**Step 3: 任意の実効性確認**

Run: `mvn -DskipTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.UtilityTest test`  
Expected: 代表テストが実行される（環境制約で skip の場合はその旨を記録）

### Task 5: 仕上げコミット

**Files:**
- Modify: `pom.xml`

**Step 1: 最終差分確認**

Run: `git diff --stat`  
Expected: `pom.xml` 中心の最小差分

**Step 2: コミット**

```bash
git add pom.xml
git commit -m "JUnit5併用基盤を導入"
```

### Task 6: 完了報告

**Step 1: 報告項目を整理**

1. 変更ファイル
2. 実行コマンド
3. 成功/失敗
4. 既知制約（skip 設定など）
