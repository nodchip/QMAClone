# Hamcrest Migration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `hamcrest-all:1.3` を `hamcrest:2.2` に置換し、最小差分でビルド整合を維持する。

**Architecture:** まず `pom.xml` の依存置換のみを実施し、`build -> test` を直列実行する。コンパイルエラー発生時のみ該当テストファイルを局所修正し、広範囲変更を避ける。

**Tech Stack:** Maven, JUnit4, Hamcrest, Mockito

---

### Task 1: ベースライン確認

**Files:**
- Modify: なし
- Test: なし

**Step 1: 作業ツリー確認**

Run: `git status --short`  
Expected: 変更なし

**Step 2: 現行依存確認**

Run: `rg -n "hamcrest-all|hamcrest" pom.xml`  
Expected: `hamcrest-all` のみ定義されている

### Task 2: 依存置換

**Files:**
- Modify: `pom.xml`
- Test: なし

**Step 1: 依存を置換**

`org.hamcrest:hamcrest-all:1.3` を `org.hamcrest:hamcrest:2.2` に変更し、`scope=test` は維持する。

**Step 2: 差分確認**

Run: `git diff -- pom.xml`  
Expected: hamcrest 依存行のみ差分

### Task 3: 逐次検証

**Files:**
- Modify: 必要時のみ `src/test/java/**`
- Test: 実行のみ

**Step 1: build**

Run: `mvn -DskipTests package`  
Expected: `BUILD SUCCESS`

**Step 2: test**

Run: `mvn test`  
Expected: `BUILD SUCCESS`（現設定で skip でも正常終了）

**Step 3: 失敗時のみ局所修正**

コンパイルエラーのあるファイルだけ修正し、再度 Step 1 から実行する。

### Task 4: コミット

**Files:**
- Modify: `pom.xml`（+ 必要時の局所修正ファイル）
- Test: なし

**Step 1: 最終差分確認**

Run: `git diff --stat`  
Expected: hamcrest 置換目的に対する最小差分

**Step 2: コミット**

```bash
git add pom.xml
git add src/test/java/...   # 必要時のみ
git commit -m "hamcrest依存を2.2へ置換"
```

### Task 5: 完了報告

**Files:**
- Modify: なし
- Test: なし

**Step 1: 検証結果を報告**

1. 変更ファイル
2. 実行コマンド
3. 結果（成功/失敗）
4. 追加修正があれば理由
