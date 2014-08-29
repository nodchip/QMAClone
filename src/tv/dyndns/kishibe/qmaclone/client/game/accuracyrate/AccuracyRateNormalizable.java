package tv.dyndns.kishibe.qmaclone.client.game.accuracyrate;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;

public interface AccuracyRateNormalizable {

	/**
	 * 正解率を確率を考慮して 0.0 - 1.0 に正規化する
	 * 
	 * @param problem
	 *            問題
	 * @return 正規化後の正解率
	 */
	public double normalize(PacketProblemMinimum problem);

}
