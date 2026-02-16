# 登録問題一覧 UI/UX 改善 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 「正解率統計」を「登録問題一覧」として再定義し、管理用途に最適化した説明文・列構成・既定ソートへ変更する。

**Architecture:** 画面固有の要件（名称、説明文、初期列、既定ソート）は `PanelRatioReport` 側で明示し、共通パーツ `ProblemReportUi` / `CellTableProblem` は設定駆動へ拡張する。検索画面・類似問題一覧の既存挙動は新オプションの既定値で維持する。ロジック追加は最小限（YAGNI）に留め、TDD で小さく進める。

**Tech Stack:** Java 25, GWT (UiBinder/CellTable), Maven (`surefire`, `gwt:test`), JUnit 4/5 混在環境

---

### Task 1: 画面名称と説明文を管理用途へ置換

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/Controller.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelRatioReport.java`
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/client/PanelRatioReportTextTest.java`

**Step 1: Write the failing test**

```java
package tv.dyndns.kishibe.qmaclone.client;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PanelRatioReportTextTest extends QMACloneGWTTestCaseBase {
	@Test
	public void testLeadTextContainsRegisteredProblemListGuidance() {
		PanelRatioReport panel = new PanelRatioReport();
		String html = panel.getWidget(0).toString();
		assertTrue(html.contains("登録した問題の正答率と状態を確認できます。"));
		assertTrue(html.contains("情報更新"));
		assertTrue(html.contains("一括消去は設定画面"));
	}
}
```

**Step 2: Run test to verify it fails**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.PanelRatioReportTextTest test`

Expected: FAIL（旧説明文のため一致しない）

**Step 3: Write minimal implementation**

```java
// Controller.java
tabPanel.add(panelRatioReport, "登録問題一覧");

// PanelRatioReport.java
add(new HTML(
    "登録した問題の正答率と状態を確認できます。<br/>"
  + "問題を登録・投稿した後は「情報更新」で最新状態を反映します。<br/>"
  + "登録されている問題の一括消去は設定画面より行えます。"));
```

**Step 4: Run test to verify it passes**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.PanelRatioReportTextTest test`

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/Controller.java \
        src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelRatioReport.java \
        src/test/java/tv/dyndns/kishibe/qmaclone/client/PanelRatioReportTextTest.java
git commit -m "登録問題一覧の名称と説明文を管理用途に更新"
```

### Task 2: 共通テーブルに「画面別初期列」設定を追加

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportUi.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/report/CellTableProblem.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportViewOptions.java`
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportViewOptionsTest.java`

**Step 1: Write the failing test**

```java
package tv.dyndns.kishibe.qmaclone.client.report;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProblemReportViewOptionsTest {
	@Test
	public void testRatioReportOptionsHideSimilarityCreatorRegister() {
		ProblemReportViewOptions options = ProblemReportViewOptions.forRatioReport();
		assertFalse(options.showSimilarity);
		assertFalse(options.showCreator);
		assertFalse(options.showRegister);
		assertTrue(options.showAccuracyRate);
		assertTrue(options.showAnswerCount);
		assertTrue(options.showIndication);
		assertTrue(options.showOperation);
	}
}
```

**Step 2: Run test to verify it fails**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.report.ProblemReportViewOptionsTest test`

Expected: FAIL（`ProblemReportViewOptions` 未実装）

**Step 3: Write minimal implementation**

```java
// ProblemReportViewOptions.java
public final class ProblemReportViewOptions {
	public final boolean showSimilarity;
	public final boolean showCreator;
	public final boolean showRegister;
	public final boolean showAnswerCount;
	// ...（必要列フラグ）

	public static ProblemReportViewOptions defaults() { /* 既存互換 */ }
	public static ProblemReportViewOptions forRatioReport() { /* 8列仕様 */ }
}

// ProblemReportUi.java
public ProblemReportUi(List<PacketProblem> problems, boolean regist, boolean initialSort,
		int maxProblemsPerPage, ProblemReportViewOptions options) { ... }

// CellTableProblem.java
public CellTableProblem(List<ProblemReportRow> rows, boolean regist, int pageSize,
		ProblemReportViewOptions options) { ... }
// options に応じて addColumn を条件分岐
```

**Step 4: Run test to verify it passes**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.report.ProblemReportViewOptionsTest test`

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportUi.java \
        src/main/java/tv/dyndns/kishibe/qmaclone/client/report/CellTableProblem.java \
        src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportViewOptions.java \
        src/test/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportViewOptionsTest.java
git commit -m "問題レポート共通テーブルに画面別列設定を追加"
```

### Task 3: 回答数列追加と既定ソート（回答数降順→正答率昇順）実装

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/report/CellTableProblem.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportRowSorter.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportUi.java`
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportRowSorterTest.java`

**Step 1: Write the failing test**

```java
package tv.dyndns.kishibe.qmaclone.client.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import com.google.common.collect.Lists;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

public class ProblemReportRowSorterTest {
	@Test
	public void testSortByAnswerCountDescThenAccuracyAsc() {
		PacketProblem a = new PacketProblem(); a.id = 1; a.good = 10; a.bad = 10; // 20, 50%
		PacketProblem b = new PacketProblem(); b.id = 2; b.good = 6; b.bad = 14; // 20, 30%
		PacketProblem c = new PacketProblem(); c.id = 3; c.good = 1; c.bad = 1; // 2, 50%
		List<ProblemReportRow> rows = Lists.newArrayList(
			new ProblemReportRow(a, null, null),
			new ProblemReportRow(b, null, null),
			new ProblemReportRow(c, null, null)
		);
		ProblemReportRowSorter.sortForRatioReport(rows);
		assertEquals(2, rows.get(0).problem.id); // 回答数同値なら低正答率優先
		assertEquals(1, rows.get(1).problem.id);
		assertEquals(3, rows.get(2).problem.id);
		assertTrue(rows.get(0).problem.good + rows.get(0).problem.bad
			>= rows.get(1).problem.good + rows.get(1).problem.bad);
	}
}
```

**Step 2: Run test to verify it fails**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.report.ProblemReportRowSorterTest test`

Expected: FAIL（ソート関数未実装）

**Step 3: Write minimal implementation**

```java
// ProblemReportRowSorter.java
public static void sortForRatioReport(List<ProblemReportRow> rows) {
	Collections.sort(rows, BY_ANSWER_COUNT_DESC_THEN_ACCURACY_ASC_THEN_ID_DESC);
}

private static final Comparator<ProblemReportRow> BY_ANSWER_COUNT_DESC_THEN_ACCURACY_ASC_THEN_ID_DESC =
    new Comparator<ProblemReportRow>() {
      @Override
      public int compare(ProblemReportRow left, ProblemReportRow right) {
        int leftAnswers = safeProblem(left).good + safeProblem(left).bad;
        int rightAnswers = safeProblem(right).good + safeProblem(right).bad;
        int byCount = rightAnswers - leftAnswers;
        if (byCount != 0) return byCount;
        int byRate = safeProblem(left).getAccuracyRate() - safeProblem(right).getAccuracyRate();
        if (byRate != 0) return byRate;
        return safeProblemId(right) - safeProblemId(left);
      }
    };

// CellTableProblem.java
// options.showAnswerCount のとき「回答数」列を追加（Comparator: good+bad）
```

**Step 4: Run test to verify it passes**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.report.ProblemReportRowSorterTest test`

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/report/CellTableProblem.java \
        src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportRowSorter.java \
        src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportUi.java \
        src/test/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportRowSorterTest.java
git commit -m "登録問題一覧向けの回答数列と既定ソートを実装"
```

### Task 4: PanelRatioReport に新オプション適用と回帰検証

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelRatioReport.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelSearchProblem.java`（既存呼び出し互換確認）
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportUiCompatibilityTest.java`

**Step 1: Write the failing test**

```java
package tv.dyndns.kishibe.qmaclone.client.report;

import static org.junit.Assert.assertNotNull;
import java.util.Collections;
import org.junit.Test;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

public class ProblemReportUiCompatibilityTest {
	@Test
	public void testLegacyConstructorStillWorksForSearchScreen() {
		PacketProblem p = new PacketProblem();
		ProblemReportUi ui = new ProblemReportUi(Collections.singletonList(p), false, true, 20);
		assertNotNull(ui);
	}
}
```

**Step 2: Run test to verify it fails**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.report.ProblemReportUiCompatibilityTest test`

Expected: FAIL（互換が崩れている場合のみ）

**Step 3: Write minimal implementation**

```java
// PanelRatioReport.java
panelGrid.setWidget(new ProblemReportUi(
    result, false, true, MAX_PROBLEMS_PER_PAGE, ProblemReportViewOptions.forRatioReport()));
ProblemReportRowSorter.sortForRatioReport(...); // RatioReport専用で適用

// ProblemReportUi.java
// 既存コンストラクタは defaults() を使って委譲し、呼び出し元互換を維持
```

**Step 4: Run test to verify it passes**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.report.ProblemReportUiCompatibilityTest test`

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelRatioReport.java \
        src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelSearchProblem.java \
        src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportUi.java \
        src/test/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportUiCompatibilityTest.java
git commit -m "登録問題一覧へ画面別設定を適用し互換性を維持"
```

### Task 5: 統合検証とデプロイ

**Files:**
- Modify: なし（検証のみ）

**Step 1: Run focused tests**

Run:  
`mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.PanelRatioReportTextTest,tv.dyndns.kishibe.qmaclone.client.report.ProblemReportViewOptionsTest,tv.dyndns.kishibe.qmaclone.client.report.ProblemReportRowSorterTest,tv.dyndns.kishibe.qmaclone.client.report.ProblemReportUiCompatibilityTest test`

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
`powershell -ExecutionPolicy Bypass -File .\\deploy_qmaclone_tomcat9.ps1`

Expected:  
- `/QMAClone-1.0-SNAPSHOT/` が HTTP 200  
- `/tv.dyndns.kishibe.qmaclone.QMAClone/service` が HTTP 405

**Step 5: Commit**

```bash
git add -A
git commit -m "登録問題一覧UI改善の最終検証と配備完了"
```

