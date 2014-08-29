package tv.dyndns.kishibe.qmaclone.server.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class NormalizerTest {
	@Test
	public final void testNormalize() {
		assertEquals("google", Normalizer.normalize("Google"));
		assertEquals("google", Normalizer.normalize("google"));
		assertEquals("google", Normalizer.normalize("Ｇｏｏｇｌｅ"));
		assertEquals("google", Normalizer.normalize("ＧＯＯＧＬＥ"));
		assertEquals("ファイナルファンタジー", Normalizer.normalize("ﾌｧｲﾅﾙﾌｧﾝﾀｼﾞｰ"));
		assertEquals("ドラゴンクエストix", Normalizer.normalize("ﾄﾞﾗｺﾞﾝｸｴｽﾄⅨ"));
		assertEquals("1234567890", Normalizer.normalize("１２３４５６７８９０"));
		assertEquals("%n", Normalizer.normalize("％Ｎ"));
		assertEquals("天上天下", Normalizer.normalize("天上天下"));
		assertEquals("i was born to love you.", Normalizer.normalize("Ｉ　ｗａｓ　ｂｏｒｎ　ｔｏ　ｌｏｖｅ　ｙｏｕ．"));
	}
}
