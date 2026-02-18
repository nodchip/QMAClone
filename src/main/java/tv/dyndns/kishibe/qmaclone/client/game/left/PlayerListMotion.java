package tv.dyndns.kishibe.qmaclone.client.game.left;

/**
 * プレイヤーリストの移動補間パラメータと補間計算を提供する。
 */
public final class PlayerListMotion {
	/**
	 * 約60fpsで更新するためのフレーム間隔。
	 */
	public static final int FRAME_INTERVAL_MS = 16;
	private static final int EASING_RATIO_NUMERATOR = 4;
	private static final int EASING_RATIO_DENOMINATOR = 100;
	private static final int SNAP_DISTANCE = 1;

	private PlayerListMotion() {
	}

	/**
	 * 現在値から目的値へ、最小1pxで確実に近づく次フレーム位置を返す。
	 */
	public static int nextPosition(int current, int destination) {
		int delta = destination - current;
		if (Math.abs(delta) <= SNAP_DISTANCE) {
			return destination;
		}

		int step = (delta * EASING_RATIO_NUMERATOR) / EASING_RATIO_DENOMINATOR;
		if (step == 0) {
			step = delta > 0 ? 1 : -1;
		}
		return current + step;
	}
}
