package tv.dyndns.kishibe.qmaclone.client.game.left;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * プレイヤーリスト移動の補間計算を検証するテスト。
 */
public class PlayerListMotionTest {
	/**
	 * 目的地まで十分近い場合は最終位置に吸着する。
	 */
	@Test
	public void nextPositionSnapsToDestinationWhenCloseEnough() {
		assertEquals(100, PlayerListMotion.nextPosition(99, 100));
		assertEquals(100, PlayerListMotion.nextPosition(101, 100));
	}

	/**
	 * 目的地まで距離がある場合は、現在値から目的地方向へ進む。
	 */
	@Test
	public void nextPositionMovesTowardDestination() {
		assertEquals(104, PlayerListMotion.nextPosition(100, 200));
		assertEquals(196, PlayerListMotion.nextPosition(200, 100));
	}

	/**
	 * 更新間隔は高フレームレート設定になっている。
	 */
	@Test
	public void frameIntervalIsOptimizedForSmoothAnimation() {
		assertEquals(16, PlayerListMotion.FRAME_INTERVAL_MS);
	}
}
