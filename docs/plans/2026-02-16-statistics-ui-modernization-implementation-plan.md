# 統計画面 UI/UX モダン化（全サブパネル）Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 統計タブ配下の全サブパネルを、既存機能を変えずに濃紺テーマで統一し、可読性・操作性・狭幅時の見やすさを改善する。

**Architecture:** 既存の統計ロジック（RPC/集計/データ構築）は変更せず、`PanelStatistics*` と `Grid*` に限定してスタイル適用を強化する。`QMAClone.css` では統計専用クラスを `statisticsRoot` 配下にスコープし、ヒートマップ配色（`accuracyRate*`）を維持する。テストは GWT クライアントテストで「スタイル適用の回帰」を担保し、実画面確認はデプロイ後に行う。

**Tech Stack:** Java 25, GWT (Widget/Chart/Grid), Maven (`surefire`, `gwt:test`), JUnit4 (`QMACloneGWTTestCaseBase`), CSS

---

### Task 1: 統計UIスタイル回帰テストを先に拡張する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsUiStyleTest.java`

**Step 1: Write the failing test**

```java
@Test
public void testAllStatisticsPanelsHaveModernCardStyles() {
	assertTrue(new PanelStatisticsNumberOfProblems().getStyleName().contains("statisticsCard"));
	assertTrue(new PanelStatisticsAccuracyRate().getStyleName().contains("statisticsCard"));
	assertTrue(PanelStatisticsUserAccuracyRate.getInstance().getStyleName().contains("statisticsCard"));
	assertTrue(new PanelStatisticsPrefectureRatingRanking().getStyleName().contains("statisticsCard"));
	assertTrue(PanelStatisticsRatingHistory.getInstance().getStyleName().contains("statisticsCard"));
	assertTrue(new PanelStatisticsRatingDistribution().getStyleName().contains("statisticsCard"));
}

@Test
public void testAllStatisticsGridsHaveStatisticsTableStyle() {
	assertTrue(new GridNumberOfProblems().getStyleName().contains("statisticsTable"));
	assertTrue(new GridAccuracyRate().getStyleName().contains("statisticsTable"));
	assertTrue(new GridUserAccuracyRate().getStyleName().contains("statisticsTable"));
	assertTrue(new GridPrefectureRanking().getStyleName().contains("statisticsTable"));
}
```

**Step 2: Run test to verify it fails**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatisticsUiStyleTest test`

Expected: FAIL（未適用スタイルがあれば失敗）

**Step 3: Write minimal implementation**

- 失敗したパネル/グリッドに対して `addStyleName("statisticsCard")` / `addStyleName("statisticsTable")` を最小追加する。
- 既存機能ロジック（`onLoad`, RPC callback, データセット）には触れない。

**Step 4: Run test to verify it passes**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatisticsUiStyleTest test`

Expected: PASS

**Step 5: Commit**

```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsUiStyleTest.java \
        src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/*.java
git commit -m "統計サブパネルのモダンスタイル適用をテストで固定"
```

### Task 2: 固定幅を縮小し、統計タブ全体をレスポンシブ前提へ寄せる

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatistics.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsTop.java`
- Modify: `src/main/webapp/QMAClone.css`

**Step 1: Write the failing test**

```java
@Test
public void testStatisticsRootAndTopDoNotUseHardcodedFixedWidths() {
	PanelStatistics root = new PanelStatistics();
	PanelStatisticsTop top = new PanelStatisticsTop();
	assertFalse("800px".equals(root.getWidth()));
	assertFalse("600px".equals(top.getWidth()));
}
```

**Step 2: Run test to verify it fails**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatisticsUiStyleTest test`

Expected: FAIL（現状固定幅が残っているため）

**Step 3: Write minimal implementation**

```java
// PanelStatistics.java
setWidth("100%");

// PanelStatisticsTop.java
setWidth("100%");
```

```css
/* QMAClone.css */
.statisticsRoot {
  width: 100%;
  max-width: 980px;
}

.statisticsTopCard {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}
```

**Step 4: Run test to verify it passes**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatisticsUiStyleTest test`

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatistics.java \
        src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsTop.java \
        src/main/webapp/QMAClone.css \
        src/test/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsUiStyleTest.java
git commit -m "統計画面の固定幅を解消してレスポンシブ化"
```

### Task 3: カード・見出し・説明文の情報階層を全サブパネルで統一する

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsNumberOfProblems.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsAccuracyRate.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsUserAccuracyRate.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsPrefectureRatingRanking.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsRatingHistory.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsRatingDistribution.java`
- Modify: `src/main/webapp/QMAClone.css`

**Step 1: Write the failing test**

```java
@Test
public void testSectionPanelsHaveSectionCardStyle() {
	assertTrue(new PanelStatisticsNumberOfProblems().getStyleName().contains("statisticsSectionCard"));
	assertTrue(new PanelStatisticsAccuracyRate().getStyleName().contains("statisticsSectionCard"));
	assertTrue(PanelStatisticsUserAccuracyRate.getInstance().getStyleName().contains("statisticsSectionCard"));
	assertTrue(new PanelStatisticsPrefectureRatingRanking().getStyleName().contains("statisticsSectionCard"));
	assertTrue(PanelStatisticsRatingHistory.getInstance().getStyleName().contains("statisticsSectionCard"));
	assertTrue(new PanelStatisticsRatingDistribution().getStyleName().contains("statisticsSectionCard"));
}
```

**Step 2: Run test to verify it fails**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatisticsUiStyleTest test`

Expected: FAIL（未統一箇所が残っている場合）

**Step 3: Write minimal implementation**

- 各パネルでタイトルに `statisticsSectionTitle` を付与。
- 補足テキスト（説明）に `statisticsDescription` を統一適用。
- `QMAClone.css` に以下を補完（不足時のみ）：

```css
.statisticsSectionCard > * + * {
  margin-top: 8px;
}

.statisticsSectionTitle {
  font-size: 18px;
  font-weight: 700;
}

.statisticsDescription {
  font-size: 14px;
  line-height: 1.55;
}
```

**Step 4: Run test to verify it passes**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatisticsUiStyleTest test`

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/*.java \
        src/main/webapp/QMAClone.css \
        src/test/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsUiStyleTest.java
git commit -m "統計サブパネルの情報階層スタイルを統一"
```

### Task 4: テーブルの可読性統一とヒートマップ維持を両立する

**Files:**
- Modify: `src/main/webapp/QMAClone.css`

**Step 1: Write the failing test**

- このタスクは CSS スコープ調整が中心のため、まず既存の統計UIテストを実行して基準を固定する（回帰防止）。

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatisticsUiStyleTest test`

Expected: PASS（この時点のベースライン）

**Step 2: Write minimal implementation**

```css
.statisticsRoot .statisticsTable td {
  border-color: #d9e8f5 !important;
}

.statisticsRoot .statisticsTable tr:first-child td,
.statisticsRoot .statisticsTable td:first-child {
  background: #edf6ff;
  color: #1a486f;
  font-weight: 700;
}

/* ヒートマップ配色は維持するため、accuracyRate系を上書きしない */
```

**Step 3: Run focused verification**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatisticsUiStyleTest test`

Expected: PASS

**Step 4: Commit**

```bash
git add src/main/webapp/QMAClone.css
git commit -m "統計テーブルの可読性を向上しヒートマップ配色を維持"
```

### Task 5: 統合検証・デプロイ・目視確認

**Files:**
- Modify: なし（検証のみ）

**Step 1: Run focused tests**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatisticsUiStyleTest test`

Expected: PASS

**Step 2: Run build**

Run:  
`mvn -DskipTests package`

Expected: BUILD SUCCESS

**Step 3: Run project test command**

Run:  
`mvn test`

Expected: BUILD SUCCESS（このプロジェクト設定では `Tests are skipped` を確認）

**Step 4: Deploy and readiness checks**

Run:  
`powershell -ExecutionPolicy Bypass -File .\deploy_qmaclone_tomcat9.ps1`

Expected:
- `/QMAClone-1.0-SNAPSHOT/` が HTTP 200
- `/tv.dyndns.kishibe.qmaclone.QMAClone/service` が HTTP 405

**Step 5: Visual verification checklist**

- 統計タブ全サブパネルで見出し・余白・罫線・本文サイズが統一されている。
- 正解率ヒートマップ（`accuracyRate*`）の意味色が崩れていない。
- 1100px 境界付近で1カラム化され、横スクロールが悪化していない。
- ロビー/チャット画面とトーンが一致している。

**Step 6: Commit**

```bash
git add -A
git commit -m "統計画面UIモダン化を全サブパネルへ適用"
```
