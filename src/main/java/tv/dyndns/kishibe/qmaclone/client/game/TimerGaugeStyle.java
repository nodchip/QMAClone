package tv.dyndns.kishibe.qmaclone.client.game;

/**
 * 残り時間ゲージの表示計算を提供するユーティリティ。
 */
public final class TimerGaugeStyle {
	private TimerGaugeStyle() {
	}

	/**
	 * 経過時間から残量比率を計算する。
	 */
	public static double remainingRate(int elapsedMs, int maxMs) {
		if (maxMs <= 0) {
			return 0.0;
		}
		double rate = 1.0 - ((double) Math.max(0, elapsedMs) / (double) maxMs);
		if (rate < 0.0) {
			return 0.0;
		}
		if (rate > 1.0) {
			return 1.0;
		}
		return rate;
	}

	/**
	 * 残量比率に応じたゲージ色（青基調）を返す。
	 */
	public static String fillColor(double remainingRate) {
		if (remainingRate <= 0.20) {
			return "#FF7B76";
		}
		if (remainingRate <= 0.50) {
			return "#FFD257";
		}
		return "#39B7FF";
	}
}
