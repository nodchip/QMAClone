package tv.dyndns.kishibe.qmaclone.server.sns;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

public interface SnsClient {
	/**
	 * 投稿された問題をSNSサイトに投稿する
	 * 
	 * @param problem
	 *            問題
	 */
	void postProblem(PacketProblem problem);

	/**
	 * テーマモードの更新をSNSサイトに投稿する
	 * 
	 * @param THEME
	 */
	void postThemeModeUpdate(String theme);

	/**
	 * フォローしたユーザーをフォローし返す
	 */
	void followBack();
}
