package tv.dyndns.kishibe.qmaclone.client.statistics;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;

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
}
