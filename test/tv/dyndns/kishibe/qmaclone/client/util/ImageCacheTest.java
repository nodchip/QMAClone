package tv.dyndns.kishibe.qmaclone.client.util;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;

public class ImageCacheTest extends QMACloneGWTTestCaseBase {
	@Test
	public void test() {
		assertEquals(
				"http://127.0.0.1:8888/image/url/006100620063/width/512/height/384/keepAspectRatio/false",
				ImageCache.getUrl("abc", 512, 384));
	}
}
