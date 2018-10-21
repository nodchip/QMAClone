package tv.dyndns.kishibe.qmaclone.client;

import java.util.Date;

import org.junit.Test;

public class UtilityTest extends QMACloneGWTTestCaseBase {
	@Test
	public void testToDateFormat() {
		assertEquals("2000/01/01 00:00:00", Utility.toDateFormat(new Date(100, 0, 1, 0, 0, 0)));
		assertEquals("2000/12/31 23:59:59", Utility.toDateFormat(new Date(100, 11, 31, 23, 59, 59)));
	}
}
