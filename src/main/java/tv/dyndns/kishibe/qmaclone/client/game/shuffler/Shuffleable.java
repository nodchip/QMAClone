package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

/**
 * 問題の回答及び選択の順番を入れ替えるためのインターフェース
 * 
 * @author nodchip
 */
public interface Shuffleable {
	void shuffle(PacketProblem problem, int[] answerOrder, int[] choiceOrder);
}
