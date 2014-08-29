package tv.dyndns.kishibe.qmaclone.client.game.judge;

import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

public class JudgeTato implements Judge {

	@Override
	public boolean judge(PacketProblem problem, String playerAnswer) {
		if (Strings.isNullOrEmpty(playerAnswer)) {
			return false;
		}

		Set<String> selected = ImmutableSet.copyOf(playerAnswer.split(Constant.DELIMITER_GENERAL));
		Set<String> answerSet = ImmutableSet.copyOf(problem.getShuffledAnswerList());
		return selected.equals(answerSet);
	}

}
