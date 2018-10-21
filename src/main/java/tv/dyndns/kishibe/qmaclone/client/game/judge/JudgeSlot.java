package tv.dyndns.kishibe.qmaclone.client.game.judge;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Strings;

public class JudgeSlot implements Judge {
	@Override
	public boolean judge(PacketProblem problem, String playerAnswer) {
		if (Strings.isNullOrEmpty(playerAnswer)) {
			return false;
		}

		return problem.answers[0].equals(playerAnswer);
	}
}
