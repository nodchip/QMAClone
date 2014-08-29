package tv.dyndns.kishibe.qmaclone.client.game.judge;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class JudgeJunban implements Judge {

	@Override
	public boolean judge(PacketProblem problem, String playerAnswer) {
		if (Strings.isNullOrEmpty(playerAnswer)) {
			return false;
		}

		List<String> selected = ImmutableList
				.copyOf(playerAnswer.split(Constant.DELIMITER_GENERAL));
		List<String> answerList = problem.getShuffledAnswerList();
		return selected.equals(answerList);
	}

}
