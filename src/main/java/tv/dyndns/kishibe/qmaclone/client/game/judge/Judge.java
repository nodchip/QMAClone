package tv.dyndns.kishibe.qmaclone.client.game.judge;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

/**
 * 解答の正誤判定を行うためのインタフェース
 * 
 * @author nodchip
 */
public interface Judge {
	/**
	 * 問題の正誤判定を行う
	 * 
	 * @param problem
	 *            問題
	 * @param playerAnswer TODO
	 * @return 正解なら{@code true}、それ以外は{@code false}
	 */
	boolean judge(PacketProblem problem, String playerAnswer);
}
