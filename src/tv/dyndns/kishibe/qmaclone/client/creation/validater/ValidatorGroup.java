package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * グループ分けクイズの検証器
 * 
 * @author nodchip
 */
public final class ValidatorGroup extends Validator {
	public Evaluation check(PacketProblem problem) {
		Preconditions.checkArgument(problem.type == ProblemType.Group);

		Evaluation eval = super.check(problem, false);
		List<String> warn = eval.warn;

		int numberOfChoice = problem.getNumberOfChoices();
		if (numberOfChoice < 3) {
			warn.add("選択肢は3つ以上必要です");
		}

		int numberOfAnswer = problem.getNumberOfAnswers();
		if (numberOfAnswer < 3) {
			warn.add("解答は3つ以上必要です");
		}

		if (numberOfChoice != numberOfAnswer) {
			warn.add("左右の選択肢の数が違います");
		}

		int numberOfGroups = ImmutableSet.copyOf(problem.getAnswerList()).size();
		if (numberOfGroups < 2) {
			warn.add("解答欄のグループの数が少なすぎます。");
		}

		if (3 < numberOfGroups) {
			warn.add("解答欄のグループの数が多すぎます。");
		}

		return eval;
	}
}
