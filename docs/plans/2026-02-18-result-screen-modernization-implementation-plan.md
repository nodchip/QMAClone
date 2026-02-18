# Result Screen Modernization Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 結果表示画面を情報優先カード型に置き換え、自分の結果の即時判読性と全体順位の比較しやすさを改善する。

**Architecture:** 既存の `PanelResult` の `Grid` 中心構造を `FlowPanel`/`VerticalPanel` ベースの3層レイアウト（hero/ranking/footer）へ段階的に置換する。GWTクラスの直接UIテストは重く壊れやすいため、まずはソース検証テストで構造とCSS契約を固定し、その後に最小実装で通す。既存の `SceneResult` の非同期取得ロジックは維持し、表示責務のみを `PanelResult` に集約する。

**Tech Stack:** Java 25, GWT, Maven, JUnit4, CSS (`src/main/webapp/QMAClone.css`)

---

### Task 1: 結果画面UI契約テストを先に作る

**Files:**
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/client/PanelResultModernLayoutSourceTest.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/PanelResultModernLayoutSourceTest.java`

**Step 1: Write the failing test**

```java
@Test
public void panelResultUsesHeroAndRankingContainers() throws Exception {
  String source = Files.readString(Paths.get(
      "src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelResult.java"),
      StandardCharsets.UTF_8);
  assertTrue(source.contains("resultHero"));
  assertTrue(source.contains("resultRankingList"));
  assertFalse(source.contains("new Grid("));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=PanelResultModernLayoutSourceTest" test`
Expected: FAIL（`resultHero` など未実装）

**Step 3: Commit**

```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/PanelResultModernLayoutSourceTest.java
git commit -m "結果画面モダン構造の契約テストを追加"
```

### Task 2: PanelResult をカード型レイアウトへ最小置換

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelResult.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/PanelResultModernLayoutSourceTest.java`

**Step 1: Write the failing test (追加ケース)**

```java
@Test
public void panelResultHighlightsMyRow() throws Exception {
  String source = Files.readString(Paths.get(
      "src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelResult.java"),
      StandardCharsets.UTF_8);
  assertTrue(source.contains("resultRankingCardMine"));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=PanelResultModernLayoutSourceTest" test`
Expected: FAIL（`resultRankingCardMine` 未実装）

**Step 3: Write minimal implementation**

- `Grid` を撤去し、以下を追加。
  - `FlowPanel resultHero`
  - `FlowPanel resultRankingList`
  - `FlowPanel resultFooter`
- `setPlayerList(...)` でカードを生成。
- 自分のカードには `resultRankingCardMine` を付与。
- 既存の「ロビーに戻る」導線と `ProblemReportUi` は維持。

**Step 4: Run test to verify it passes**

Run: `mvn "-Dtest=PanelResultModernLayoutSourceTest" test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelResult.java src/test/java/tv/dyndns/kishibe/qmaclone/client/PanelResultModernLayoutSourceTest.java
git commit -m "結果表示をカード型レイアウトへ置換"
```

### Task 3: 結果画面専用CSSを追加して視認性を整える

**Files:**
- Modify: `src/main/webapp/QMAClone.css`
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/client/ResultScreenStyleContractTest.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/ResultScreenStyleContractTest.java`

**Step 1: Write the failing test**

```java
@Test
public void resultScreenDefinesModernCardStyles() throws Exception {
  String css = Files.readString(Paths.get("src/main/webapp/QMAClone.css"), StandardCharsets.UTF_8);
  assertTrue(css.contains(".resultHero"));
  assertTrue(css.contains(".resultRankingCard"));
  assertTrue(css.contains(".resultRankingCardMine"));
  assertTrue(css.contains(".resultRatingBadgeUp"));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=ResultScreenStyleContractTest" test`
Expected: FAIL（未定義クラス）

**Step 3: Write minimal implementation**

- `QMAClone.css` に結果画面専用クラスを追加。
  - `.resultRoot`, `.resultHero`, `.resultRankingList`, `.resultRankingCard`, `.resultRankingCardMine`
  - `.resultRatingBadgeUp`, `.resultRatingBadgeDown`, `.resultRatingBadgeFlat`
- `box-sizing: border-box;` と幅制約を明示し、チャット展開時のはみ出しを防ぐ。

**Step 4: Run test to verify it passes**

Run: `mvn "-Dtest=ResultScreenStyleContractTest" test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/webapp/QMAClone.css src/test/java/tv/dyndns/kishibe/qmaclone/client/ResultScreenStyleContractTest.java
git commit -m "結果画面のモダンカードスタイルを追加"
```

### Task 4: レーティング変動表示をバッジ形式に統一

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelResult.java`
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/client/PanelResultRatingBadgeSourceTest.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/PanelResultRatingBadgeSourceTest.java`

**Step 1: Write the failing test**

```java
@Test
public void panelResultContainsRatingBadgeClasses() throws Exception {
  String source = Files.readString(Paths.get(
      "src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelResult.java"),
      StandardCharsets.UTF_8);
  assertTrue(source.contains("resultRatingBadgeUp"));
  assertTrue(source.contains("resultRatingBadgeDown"));
  assertTrue(source.contains("resultRatingBadgeFlat"));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=PanelResultRatingBadgeSourceTest" test`
Expected: FAIL

**Step 3: Write minimal implementation**

- 既存 `SafeHtmlTemplates` の矢印表示を、バッジ用クラスと `+N/-N/±0` 表記に変更。
- `newRating <= 0` の場合は「変動なし」扱いに統一。

**Step 4: Run test to verify it passes**

Run: `mvn "-Dtest=PanelResultRatingBadgeSourceTest" test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelResult.java src/test/java/tv/dyndns/kishibe/qmaclone/client/PanelResultRatingBadgeSourceTest.java
git commit -m "結果画面のレート変動表示をバッジ形式に統一"
```

### Task 5: 統合検証と配備

**Files:**
- Modify: なし（必要時のみ微修正）
- Test: 既存 + 追加テスト

**Step 1: Run targeted tests**

Run: `mvn "-Dtest=PanelResultModernLayoutSourceTest,ResultScreenStyleContractTest,PanelResultRatingBadgeSourceTest" "-DfailIfNoTests=false" test`
Expected: PASS

**Step 2: Run compile + gwt compile**

Run: `mvn -DskipTests compile`
Expected: BUILD SUCCESS

Run: `mvn "-Dgwt.skipCompilation=false" gwt:compile`
Expected: BUILD SUCCESS

**Step 3: Deploy**

Run: `powershell -ExecutionPolicy Bypass -File .\deploy_qmaclone_tomcat9.ps1`
Expected: `/QMAClone-1.0-SNAPSHOT/` が HTTP 200、`/service` が HTTP 405

**Step 4: Manual verification checklist**

- 成績発表画面で自分サマリーが最上段に表示される。
- 自分のランキングカードだけ強調される。
- レート変動バッジが増減維持で色分けされる。
- チャット展開時にカードが横はみ出ししない。

**Step 5: Commit (if fixes were needed)**

```bash
git add <adjusted-files>
git commit -m "結果画面モダン化の最終調整"
```
