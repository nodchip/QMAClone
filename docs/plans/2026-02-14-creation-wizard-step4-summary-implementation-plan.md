# 問題作成ウィザード確認ステップ情報整理 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 問題作成ウィザード Step4 をセクション要約中心の確認画面に変更し、誤送信を減らしつつ修正導線を明確化する。

**Architecture:** Step4 を 3 つの summary card（基本情報 / 問題文 / 解答設定）に再構成し、各カードに「詳細開閉」と「編集へ戻る」を持たせる。`CreationUi` でサマリー構築とエラー件数表示を制御し、`WidgetProblemForm` から取得した `PacketProblem` を表示専用データへ整形する。

**Tech Stack:** GWT UiBinder, Java, Maven, CSS

---

### Task 1: Step4 のカードUI骨格を追加

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`
- Modify: `src/main/webapp/QMAClone.css`

**Step 1: 既存コンパイル確認**

Run: `mvn -q compile`
Expected: PASS

**Step 2: UiBinder へ3カード構造を追加**
- 基本情報カード
- 問題文カード
- 解答設定カード
- 各カードに `詳細を開く` / `編集へ戻る` ボタン

**Step 3: スタイル追加**
- カード枠、タイトル、要約行、エラー強調クラス

**Step 4: コンパイル確認**

Run: `mvn -q compile`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml src/main/webapp/QMAClone.css
git commit -m "問題作成Step4のサマリーカードUI骨格を追加"
```

### Task 2: サマリー生成ロジックと開閉制御を実装

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUiStep4SummaryTest.java`

**Step 1: 失敗テスト作成**
- Step4表示で3セクション要約が描画される
- 問題文40文字省略が動作する

**Step 2: 失敗確認**

Run: `mvn -q -Dtest=CreationUiStep4SummaryTest test`
Expected: FAIL

**Step 3: 最小実装**
- `buildSummarySections(PacketProblem)` を追加
- `updateStep4Summary()` をカード描画方式へ置換
- 詳細開閉状態フラグを `CreationUi` に保持

**Step 4: テスト成功確認**

Run: `mvn -q -Dtest=CreationUiStep4SummaryTest test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUiStep4SummaryTest.java
git commit -m "問題作成Step4のサマリー生成と詳細開閉を実装"
```

### Task 3: 編集戻り導線とエラー連動を実装

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUiStep4SummaryTest.java`

**Step 1: 失敗テスト追加**
- 各カードの `編集へ戻る` で対応ステップへ遷移する
- セクションエラー時に警告表示される

**Step 2: 失敗確認**

Run: `mvn -q -Dtest=CreationUiStep4SummaryTest test`
Expected: FAIL

**Step 3: 実装**
- カード別 `編集へ戻る` ハンドラ
- セクション単位のエラー件数表示
- エラー時クラス `creationSummaryError` 付与

**Step 4: テスト成功確認**

Run: `mvn -q -Dtest=CreationUiStep4SummaryTest test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUiStep4SummaryTest.java
git commit -m "問題作成Step4に編集戻り導線とエラー連動を追加"
```

### Task 4: 最終検証とデプロイ

**Files:**
- Use: `deploy_qmaclone_tomcat9.ps1`

**Step 1: 直列検証**

Run: `mvn -q compile`
Expected: PASS

Run: `mvn -q test`
Expected: PASS

Run: `mvn -q "-Dgwt.skipCompilation=false" gwt:compile`
Expected: PASS

**Step 2: デプロイ**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\\deploy_qmaclone_tomcat9.ps1`
Expected: `Done.`

**Step 3: 手動確認**
- Step4で3カード表示
- 詳細開閉がセクション単位で機能
- 問題文サマリーの省略表示
- 編集戻り導線が各ステップへ遷移
- エラー時にカード警告表示

**Step 4: 完了報告**
- 実行コマンドと結果
- 未検証項目があれば明記
