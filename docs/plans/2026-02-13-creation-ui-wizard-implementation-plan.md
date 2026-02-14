# Creation UI Wizard Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 問題作成画面を4ステップのウィザードUIへ段階移行し、遷移時自動保存と段階バリデーションを導入する。  
**Architecture:** 既存 `CreationUi` / `WidgetProblemForm` のロジックを活かしつつ、UI骨格（ステッパー + ステップコンテナ + 共通フッター）を先に導入する。保存・送信ロジックは既存メソッドに委譲し、フェーズごとに可動状態を維持する。  
**Tech Stack:** GWT UiBinder, Java, Maven, CSS

---

### Task 1: ウィザード骨格UIを追加（Phase 1）

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java`

**Step 1: 既存挙動を固定（基準確認）**

Run: `mvn -q compile`  
Expected: PASS

**Step 2: UiBinderに4ステップ骨格を追加**

- ステッパー領域（`step1`〜`step4`表示）
- ステップコンテナ（基本情報/問題文/解答設定/確認）
- 共通フッター（戻る・次へ）
- 既存 `panelProblemForm` は Step2/3 で再利用できるよう配置

**Step 3: Java側にステップ状態を追加**

- `currentStep`（1〜4）
- `goToStep(int)`、`updateStepIndicator()`、`updateStepVisibility()`
- 初期表示は Step1

**Step 4: コンパイル確認**

Run: `mvn -q compile`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java
git commit -m "問題作成画面に4ステップウィザード骨格を追加"
```

### Task 2: ステップ遷移制御と段階バリデーション

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java`

**Step 1: 失敗テスト（または最小検証）を先に作る**

- `next` 押下で不正入力時に進まないことを確認する検証コードを追加
- 既存テストが難しければ小さなメソッド単位でテスト可能化

**Step 2: `validateStep(int step)` を実装**

- Step1: 基本情報の必須条件
- Step2: 問題文・基本整合
- Step3: 解答設定整合
- Step4: 最終確認（既存 `validateProblem()` を活用）

**Step 3: 遷移処理へ組み込み**

- `next` 時: `validateStep(currentStep)` が true の時のみ進む
- `back` は常に可能

**Step 4: 検証**

Run: `mvn -q test`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java
git commit -m "問題作成ウィザードに段階バリデーションを実装"
```

### Task 3: ステップ遷移時の自動保存（差分保存）

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java`

**Step 1: 保存前後の状態比較フィールドを追加**

- `lastSavedSnapshot`（必要項目のみ）
- `isDirty()` 判定

**Step 2: `next` 遷移時に自動保存を呼ぶ**

- `validate -> autoSaveIfDirty -> move` の順
- 保存失敗時は遷移停止 + 警告表示

**Step 3: 保存成功/失敗のユーザー通知を追加**

- 成功: 軽量メッセージ
- 失敗: 明示メッセージ + 再試行導線

**Step 4: 検証**

Run: `mvn -q compile`  
Expected: PASS  
Run: `mvn -q test`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java
git commit -m "問題作成ウィザードに遷移時自動保存を追加"
```

### Task 4: 確認ステップ（Step4）を整備

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java`

**Step 1: Step4に確認サマリー表示を追加**

- 入力内容の主要項目を一覧化
- セクションごとに「該当ステップへ戻る」導線を付ける

**Step 2: 送信ボタン活性条件をStep4通過に限定**

- 既存 `buttonSendProblem` 表示条件と整合を取る

**Step 3: 検証**

Run: `mvn -q compile`  
Expected: PASS

**Step 4: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java
git commit -m "問題作成ウィザードの確認ステップを実装"
```

### Task 5: UI仕上げとレスポンシブ調整

**Files:**
- Modify: `src/main/webapp/QMAClone.css`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`

**Step 1: ステッパー/カード/操作ボタンの見た目を統一**

- 既存ロビーのトークンに合わせる
- ステップ状態（現在/完了/未着手）を視認可能にする

**Step 2: 狭幅時の崩れを調整**

- 2カラム補助表示を1カラムへフォールバック
- 下部操作ボタンの折返し対応

**Step 3: 検証**

Run: `mvn -q "-Dgwt.skipCompilation=false" gwt:compile`  
Expected: PASS

**Step 4: Commit**

```bash
git add src/main/webapp/QMAClone.css src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml
git commit -m "問題作成ウィザードUIの見た目とレスポンシブを調整"
```

### Task 6: 最終検証とデプロイ

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

- Step1->2->3->4 の遷移
- 不正入力で遷移阻止
- 遷移時自動保存
- 送信完了までの導線

**Step 4: 完了報告に記録**

- 実行コマンドと結果
- 既知制約/未検証項目
