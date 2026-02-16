package tv.dyndns.kishibe.qmaclone.client.statistics;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;

public class PanelStatisticsUiStyleTest extends QMACloneGWTTestCaseBase {
	@Test
	public void testPanelStatisticsHasModernRootStyle() {
		PanelStatistics panel = new PanelStatistics();
		assertTrue(panel.getStyleName().contains("statisticsRoot"));
	}

	@Test
	public void testPanelStatisticsTopHasModernCardStyle() {
		PanelStatisticsTop panel = new PanelStatisticsTop();
		assertTrue(panel.getStyleName().contains("statisticsTopCard"));
	}

	@Test
	public void testAllStatisticsPanelsHaveModernCardStyles() {
		assertTrue(new PanelStatisticsNumberOfProblems().getStyleName().contains("statisticsCard"));
		assertTrue(new PanelStatisticsAccuracyRate().getStyleName().contains("statisticsCard"));
		assertTrue(PanelStatisticsUserAccuracyRate.getInstance().getStyleName()
				.contains("statisticsCard"));
		assertTrue(new PanelStatisticsPrefectureRatingRanking().getStyleName()
				.contains("statisticsCard"));
		assertTrue(PanelStatisticsRatingHistory.getInstance().getStyleName()
				.contains("statisticsCard"));
		assertTrue(new PanelStatisticsRatingDistribution().getStyleName()
				.contains("statisticsCard"));
	}

	@Test
	public void testAllStatisticsGridsHaveStatisticsTableStyle() {
		assertTrue(new GridNumberOfProblems().getStyleName().contains("statisticsTable"));
		assertTrue(new GridAccuracyRate().getStyleName().contains("statisticsTable"));
		assertTrue(new GridUserAccuracyRate().getStyleName().contains("statisticsTable"));
		assertTrue(new GridPrefectureRanking().getStyleName().contains("statisticsTable"));
	}

	@Test
	public void testStatisticsRootAndTopDoNotUseHardcodedFixedWidths() {
		PanelStatistics root = new PanelStatistics();
		PanelStatisticsTop top = new PanelStatisticsTop();
		assertFalse("800px".equals(root.getElement().getStyle().getWidth()));
		assertFalse("600px".equals(top.getElement().getStyle().getWidth()));
	}

	@Test
	public void testStatisticsTopUsesCardIndexMarkup() {
		PanelStatisticsTop top = new PanelStatisticsTop();
		String html = top.getHTML();
		assertTrue(html.contains("statisticsTopTiles"));
		assertTrue(html.contains("statisticsTopTile"));
		assertTrue(html.contains("statisticsTopTileUse"));
	}

	@Test
	public void testNumberGridUsesModernStylesAndWrappedGenreHeader() {
		GridNumberOfProblems grid = new GridNumberOfProblems();
		assertTrue(grid.getStyleName().contains("statisticsNumberGrid"));
		assertTrue(grid.getCellFormatter().getStyleName(0, ProblemGenre.Anige.getIndex())
				.contains("statisticsNumberHeaderCell"));
		assertTrue(grid.getHTML(0, ProblemGenre.Anige.getIndex()).contains("<br"));
	}

	@Test
	public void testAccuracyGridUsesModernStylesAndWrappedGenreHeader() {
		GridAccuracyRate grid = new GridAccuracyRate();
		assertTrue(grid.getStyleName().contains("statisticsAccuracyGrid"));
		assertTrue(grid.getCellFormatter().getStyleName(0, ProblemGenre.Anige.getIndex())
				.contains("statisticsAccuracyHeaderCell"));
		assertTrue(grid.getHTML(0, ProblemGenre.Anige.getIndex()).contains("<br"));
	}

	@Test
	public void testUserAccuracyGridUsesModernStylesAndWrappedGenreHeader() {
		GridUserAccuracyRate grid = new GridUserAccuracyRate();
		assertTrue(grid.getStyleName().contains("statisticsUserAccuracyGrid"));
		assertTrue(grid.getCellFormatter().getStyleName(0, ProblemGenre.Anige.getIndex())
				.contains("statisticsUserAccuracyHeaderCell"));
		assertTrue(grid.getHTML(0, ProblemGenre.Anige.getIndex()).contains("<br"));
	}
}
