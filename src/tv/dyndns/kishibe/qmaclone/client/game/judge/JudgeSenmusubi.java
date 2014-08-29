package tv.dyndns.kishibe.qmaclone.client.game.judge;

import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class JudgeSenmusubi implements Judge {
	@Override
	public boolean judge(PacketProblem problem, String playerAnswer) {
		if (Strings.isNullOrEmpty(playerAnswer)) {
			return false;
		}

		Set<String> pairs = Sets.newHashSet(playerAnswer.split(Constant.DELIMITER_GENERAL));

		int count = 0;
		int numberOfAnswers = problem.getNumberOfAnswers();
		for (int i = 0; i < numberOfAnswers; ++i) {
			String pair = problem.choices[i] + Constant.DELIMITER_KUMIAWASE_PAIR
					+ problem.answers[i];

			if (pairs.contains(pair)) {
				++count;
			}
		}

		return count == problem.getNumberOfShuffledAnswers();
	}
}
