package tv.dyndns.kishibe.qmaclone.client.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 残り時間ゲージの見た目計算を検証するテスト。
 */
public class TimerGaugeStyleTest {
	/**
	 * 経過時間から残量比率を計算できる。
	 */
	@Test
	public void remainingRateIsClampedByElapsedTime() {
		assertEquals(1.0, TimerGaugeStyle.remainingRate(0, 30000), 0.0001);
		assertEquals(0.5, TimerGaugeStyle.remainingRate(15000, 30000), 0.0001);
		assertEquals(0.0, TimerGaugeStyle.remainingRate(50000, 30000), 0.0001);
	}

	/**
	 * 残量比率に応じて青基調の色から危険色に遷移する。
	 */
	@Test
	public void fillColorChangesByRemainingRate() {
		assertEquals("#39B7FF", TimerGaugeStyle.fillColor(0.80));
		assertEquals("#FFD257", TimerGaugeStyle.fillColor(0.40));
		assertEquals("#FF7B76", TimerGaugeStyle.fillColor(0.10));
	}
}
