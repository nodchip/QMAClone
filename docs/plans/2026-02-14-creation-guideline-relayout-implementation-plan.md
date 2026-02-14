# 問題作成ガイドライン再配置・配色調整 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 問題作成画面の導線を軽量化し、ガイドラインを専用ページに分離しつつ、ロビー基準トーンで用途別コントラスト差を適用する。

**Architecture:** `CreationUi.ui.xml` から長文ガイドラインを分離し、`src/main/webapp` に静的ガイドラインページを新設する。作成画面には短い注意文 + 新規タブリンクだけを残す。`QMAClone.css` に作成画面用/ガイドライン用のスタイルを追加して、共通トーンを維持しつつ可読性を用途別に最適化する。

**Tech Stack:** GWT UiBinder (`.ui.xml`), Java client (`CreationUi.java`), static HTML (`src/main/webapp`), CSS (`QMAClone.css`), Maven (`mvn`).

---

### Task 1: 作成画面ヘッダーの情報整理

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`

**Step 1: 既存案内文の位置を確認**

Run: `rg -n "問題ジャンルの偏り|問題作成ガイドライン" src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`
Expected: 冒頭案内文と長文ガイドラインブロック位置が確認できる。

**Step 2: 冒頭文言を短文化**

`htmlPanelMain` 先頭を次の2文に差し替える。

```xml
問題ジャンルの偏りを減らすため、問題数の少ないジャンルへの投稿にご協力ください。
<br />
投稿後は正解率統計で自作問題の状態をご確認いただき、指摘フラグが付いた場合は早めの修正をお願いします。
```

**Step 3: ガイドライン導線リンクを追加**

```xml
<g:Anchor ui:field="anchorCreationGuideline"
  href="creation-guideline.html"
  target="_blank"
  text="問題作成ガイドラインを見る（新規タブ）"
  styleName="creationGuidelineLink" />
```

**Step 4: 不要な長文ブロックを除去（移設前提）**

`<g:HTMLPanel> ... 問題作成ガイドライン ... </g:HTMLPanel>` を削除する（本文は Task 2 で専用ページへ移設）。

**Step 5: コミット**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml
git commit -m "問題作成画面の案内文を整理しガイドライン導線を追加"
```

### Task 2: ガイドライン専用ページの新設

**Files:**
- Create: `src/main/webapp/creation-guideline.html`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`

**Step 1: 専用ページの骨組みを作成**

```html
<!doctype html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <title>QMAClone 問題作成ガイドライン</title>
  <link rel="stylesheet" href="QMAClone.css" />
</head>
<body class="guidelinePage">
  <main class="guidelineContainer">
    <h1>QMAClone 問題作成ガイドライン</h1>
    <p class="guidelineOrigin">本ガイドラインは過去のQMA問題作成スレッド等を参考に整備しています。</p>
    <section class="guidelineSummary">...</section>
    <section class="guidelineBody">...</section>
  </main>
</body>
</html>
```

**Step 2: 既存長文ガイドライン本文を移設**

`CreationUi.ui.xml` から削除した本文を `guidelineBody` に貼り付ける。

**Step 3: 要点サマリーを先頭に追加**

箇条書きで 3-5 項目（重複回避/分類確認/著作権配慮/指摘優先対応/可読性配慮）を追加する。

**Step 4: 本文内リンクの target を明示**

外部リンクは `target="_blank" rel="noopener noreferrer"` に統一する。

**Step 5: コミット**

```bash
git add src/main/webapp/creation-guideline.html src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml
git commit -m "問題作成ガイドラインを専用ページへ分離"
```

### Task 3: ロビー基準トーン + 用途別コントラスト差のCSS適用

**Files:**
- Modify: `src/main/webapp/QMAClone.css`

**Step 1: 作成画面導線のスタイルを追加**

```css
.creationGuidelineLink {
  display: inline-block;
  margin: 8px 0 12px 0;
  color: #0f3f71;
  font-weight: 700;
}
```

**Step 2: ガイドラインページ用トーンを追加**

```css
.guidelinePage { background: #e8f1fb; color: #102840; }
.guidelineContainer { max-width: 980px; margin: 20px auto; padding: 20px; background: #f6fbff; border: 1px solid #9ec4e6; border-radius: 12px; }
.guidelineSummary { background: #eef6ff; border: 1px solid #c2dcf4; border-radius: 10px; padding: 12px; }
.guidelineBody h2, .guidelineBody h3 { color: #0f3f71; }
```

**Step 3: 既存ルールとの競合確認**

Run: `rg -n "guidelinePage|creationGuidelineLink|problemCreation" src/main/webapp/QMAClone.css`
Expected: 新規クラスが重複せず定義される。

**Step 4: コミット**

```bash
git add src/main/webapp/QMAClone.css
git commit -m "問題作成とガイドラインの配色を用途別に調整"
```

### Task 4: 動作確認と最終調整

**Files:**
- Verify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`
- Verify: `src/main/webapp/creation-guideline.html`
- Verify: `src/main/webapp/QMAClone.css`

**Step 1: ビルドとGWTコンパイル**

Run:
- `mvn compile`
- `mvn "-Dgwt.skipCompilation=false" gwt:compile`

Expected: どちらも成功。

**Step 2: 手動確認**

確認項目:
- 問題作成画面に短文 + 「ガイドラインを見る（新規タブ）」が表示される。
- リンク押下で新規タブに専用ページが開く。
- 作成画面の入力内容が維持される。
- ガイドラインが明背景 + 暗文字で読みやすい。
- スクロール時の背景急変が改善される。

**Step 3: 最終コミット**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml src/main/webapp/creation-guideline.html src/main/webapp/QMAClone.css
git commit -m "問題作成導線とガイドライン表示を再構成"
```
