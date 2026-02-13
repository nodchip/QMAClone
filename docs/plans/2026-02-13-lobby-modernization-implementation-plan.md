# Lobby Modernization Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** ロビー画面を近未来ゲーム風に刷新し、中央導線・右チャット・下端サマリーのレイアウトを安定化する。  
**Architecture:** まず `LobbyUi.ui.xml` の構造を再編し、次に `Controller` と `QMAClone.css` で全体レイアウトを整える。既存ロジック（マッチング登録・設定保存）は変更せず、見た目とUI構造を段階的に差し替える。  
**Tech Stack:** GWT (UiBinder), Java 25, Maven, CSS

---

### Task 1: ロビー用デザイントークンと全体レイアウト土台

**Files:**
- Modify: `src/main/webapp/QMAClone.css`
- Test: `src/main/java/tv/dyndns/kishibe/qmaclone/client/Controller.java`（表示確認対象）

**Step 1: 先に失敗を確認する（現状確認）**

Run: `mvn "-Dgwt.skipCompilation=false" gwt:compile`  
Expected: PASS（この時点では見た目刷新未適用）

**Step 2: ロビー向けトークンを追加**

```css
:root {
  --lobby-bg-0: #07152b;
  --lobby-bg-1: #0e2745;
  --lobby-surface: #112a4a;
  --lobby-surface-2: #17375d;
  --lobby-accent: #54d8ff;
  --lobby-text: #eaf4ff;
  --lobby-text-subtle: #b7c9df;
  --lobby-border: #2f537a;
  --lobby-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
}
```

**Step 3: 全体コンテナのレイアウト土台を追加**

```css
.app-shell { min-height: 100vh; }
.app-main { display: grid; grid-template-columns: 220px minmax(720px, 1fr) 320px; gap: 20px; }
```

**Step 4: コンパイル確認**

Run: `mvn compile`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/webapp/QMAClone.css
git commit -m "ロビー刷新のデザイントークンと全体レイアウト土台を追加"
```

### Task 2: Controller のDOM構造をラップして3カラム化

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/Controller.java`
- Test: `src/main/webapp/QMAClone.css`

**Step 1: 現状でのDOM構成を確認**

Run: `rg -n "rootPanel.add\\(tabPanel\\)|rootPanel.add\\(panelChat\\)" src/main/java/tv/dyndns/kishibe/qmaclone/client/Controller.java`  
Expected: 既存の縦積み add が確認できる

**Step 2: ラップ用パネルを追加**

```java
private final SimplePanel panelMainCenter = new SimplePanel();
private final SimplePanel panelMainRight = new SimplePanel();
```

**Step 3: `tabPanel` と `panelChat` を 3カラム構造へ移動**

```java
// rootPanel直下に app-main 相当のラッパを追加し、
// center に tabPanel、right に panelChat を配置
```

**Step 4: 破綻しないことを確認**

Run: `mvn compile`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/Controller.java
git commit -m "ロビー刷新向けにControllerの画面ラッパ構造を再編"
```

### Task 3: LobbyUi の構造を「状態/CTA/条件/下端サマリー」に再編

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/lobby/LobbyUi.ui.xml`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/lobby/LobbyUi.java`

**Step 1: 現状構造の確認**

Run: `rg -n "panelInformation|spanWaiting|buttonGameVsCom|listBoxTheme" src/main/java/tv/dyndns/kishibe/qmaclone/client/lobby/LobbyUi.*`  
Expected: 旧テーブル中心構造の要素が確認できる

**Step 2: UiBinder をカード構造に変更**

```xml
<g:HTMLPanel styleName="{style.lobbyRoot}">
  <g:HTMLPanel styleName="{style.lobbyStatusRow}">...</g:HTMLPanel>
  <g:HTMLPanel styleName="{style.lobbyTopSection}">...</g:HTMLPanel>
  <g:HTMLPanel styleName="{style.lobbySummaryBottom}">...</g:HTMLPanel>
</g:HTMLPanel>
```

**Step 3: 不要情報ブロック（`panelInformation`）を除去し、Java 側の参照を no-op 化**

```java
public void updateInfomationPanel() {
  // ロビー刷新後は情報パネルを表示しない
}
```

**Step 4: コンパイル確認**

Run: `mvn compile`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/lobby/LobbyUi.ui.xml src/main/java/tv/dyndns/kishibe/qmaclone/client/lobby/LobbyUi.java
git commit -m "ロビーUiBinderを状態/CTA/条件/下端サマリー構成へ再編"
```

### Task 4: 近未来テーマのボタン・カード・フォーカス演出

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/lobby/LobbyUi.ui.xml`
- Modify: `src/main/webapp/QMAClone.css`

**Step 1: 主要UI部品にクラスを付与**

```xml
<g:Button styleName="{style.lobbyCtaPrimary}" ... />
```

**Step 2: 中程度演出（hover/focus/active）を実装**

```css
.lobby-cta-primary:hover { box-shadow: 0 0 14px rgba(84, 216, 255, 0.45); }
.lobby-cta-primary:focus { outline: 2px solid var(--lobby-accent); }
```

**Step 3: テキスト階層（見出し/本文/補助）を適用**

```css
.lobby-heading { color: var(--lobby-text); font-weight: 700; }
.lobby-meta { color: var(--lobby-text-subtle); }
```

**Step 4: GWT コンパイル確認**

Run: `mvn "-Dgwt.skipCompilation=false" gwt:compile`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/lobby/LobbyUi.ui.xml src/main/webapp/QMAClone.css
git commit -m "ロビーの近未来テーマと操作時演出を追加"
```

### Task 5: 右チャット非重なり・下端サマリー固定の最終調整

**Files:**
- Modify: `src/main/webapp/QMAClone.css`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/lobby/LobbyUi.ui.xml`

**Step 1: 下端サマリー固定のため縦フレックスを調整**

```css
.lobby-root { display: flex; flex-direction: column; min-height: 100%; }
.lobby-top-section { flex: 1 1 auto; min-height: 0; }
.lobby-summary-bottom { margin-top: auto; width: 100%; text-align: center; }
```

**Step 2: チャット領域を右カラム固定し、中央と重ならないよう gap/padding を調整**

```css
.app-main { grid-template-columns: 220px minmax(720px, 1fr) 320px; gap: 20px; }
```

**Step 3: 全量検証**

Run: `mvn compile`  
Expected: PASS  
Run: `mvn test`  
Expected: PASS  
Run: `mvn "-Dgwt.skipCompilation=false" gwt:compile`  
Expected: PASS

**Step 4: Commit**

```bash
git add src/main/webapp/QMAClone.css src/main/java/tv/dyndns/kishibe/qmaclone/client/lobby/LobbyUi.ui.xml
git commit -m "ロビー下端サマリー固定とチャット非重なりレイアウトを完成"
```

### Task 6: デプロイ・確認・記録

**Files:**
- Modify: `docs/plans/2026-02-13-ui-ux-refresh-design.md`（必要なら結果追記）
- Use: `deploy_qmaclone_tomcat9.ps1`

**Step 1: デプロイ**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\\deploy_qmaclone_tomcat9.ps1`  
Expected: `Done.` で終了

**Step 2: 目視確認**
- 中央パネルが中央列内で崩れない
- レーティング行が下端にある
- チャットが右列で重ならない
- CTAホバー時に中程度の発光

**Step 3: 結果記録**
- 実行コマンドと結果を報告に残す
- 未検証項目があれば明記する

**Step 4: Commit（必要時）**

```bash
git add docs/plans/2026-02-13-ui-ux-refresh-design.md
git commit -m "ロビーUI刷新の検証結果を追記"
```
