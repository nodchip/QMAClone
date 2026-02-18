package tv.dyndns.kishibe.qmaclone.client.game.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.geom.Point;

/**
 * 画像クリック座標の拡大率補正を検証するテスト。
 */
public class ClickCoordinateScalerTest {
	@Test
	public void toOriginalScalesDisplayedPointToOriginalPoint() {
		Point converted = ClickCoordinateScaler.toOriginal(new Point(180, 135), 288, 216, 320, 240);
		assertEquals(200, converted.x);
		assertEquals(150, converted.y);
	}

	@Test
	public void toOriginalHandlesDifferentCoordinateSpace() {
		Point converted = ClickCoordinateScaler.toOriginal(new Point(150, 112), 300, 225, 512, 384);
		assertEquals(256, converted.x);
		assertEquals(191, converted.y);
	}

	@Test
	public void toOriginalReturnsNullWhenSizeIsInvalid() {
		assertNull(ClickCoordinateScaler.toOriginal(new Point(10, 10), 0, 225, 512, 384));
	}

	@Test
	public void toDisplayedScalesOriginalPointToDisplayedPoint() {
		Point converted = ClickCoordinateScaler.toDisplayed(new Point(256, 192), 512, 384, 300, 225);
		assertEquals(150, converted.x);
		assertEquals(113, converted.y);
	}
}
