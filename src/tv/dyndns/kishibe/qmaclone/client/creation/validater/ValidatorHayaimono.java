package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import java.util.List;
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public class ValidatorHayaimono extends Validator {
	@Override
	public Evaluation check(PacketProblem problem) {
		Preconditions.checkArgument(problem.type == ProblemType.Hayaimono);

		Evaluation eval = super.check(problem);
		List<String> warn = eval.warn;

		int numberOfChoices = problem.getNumberOfChoices();
		if (numberOfChoices != 6 && numberOfChoices != 8) {
			warn.add("選択肢は6つ又は8つ必要です");
		}

		int numberOfAnswers = problem.getNumberOfAnswers();
		if (numberOfChoices == 6) {
			if (numberOfAnswers != 2) {
				warn.add("選択肢が6つの場合は解答は2つ必要です");
			}
		} else if (numberOfChoices == 8) {
			if (numberOfAnswers != 3 && numberOfAnswers != 4) {
				warn.add("選択肢が8つの場合は解答は3つ又は4つ必要です");
			}
		}

		Set<String> choices = ImmutableSet.copyOf(problem.getChoiceList());

		List<String> answerList = problem.getAnswerList();
		for (int i = 0; i < answerList.size(); ++i) {
			String answer = answerList.get(i);

			if (!choices.contains(answer)) {
				warn.add((i + 1) + "個目の解答が選択肢に含まれていません");
			}
		}

		return eval;
	}
}
