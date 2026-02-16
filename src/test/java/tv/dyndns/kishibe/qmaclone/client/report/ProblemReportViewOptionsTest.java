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
		assertTrue(options.useRatioDefaultSort);
	}
}
