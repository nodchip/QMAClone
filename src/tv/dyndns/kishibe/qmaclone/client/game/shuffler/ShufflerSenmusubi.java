package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ShufflerSenmusubi implements Shuffleable {

	private final int maxNumberOfAnswers;

	public ShufflerSenmusubi(int maxNumberOfAnswers) {
		this.maxNumberOfAnswers = maxNumberOfAnswers;
	}

	@Override
	public void shuffle(PacketProblem problem, int[] answerOrder, int[] choiceOrder) {
		Preconditions.checkNotNull(answerOrder);
		Preconditions.checkNotNull(choiceOrder);

		if (answerOrder.length < 3 || choiceOrder.length < 3) {
			problem.shuffledAnswers = null;
			problem.shuffledChoices = null;
			return;
		}

		List<String> answers = Lists.newArrayList();
		List<String> choices = Lists.newArrayList();

		// 線結び・グループ分け共用のため特殊処理
		int numberOfAnswers = Math.min(answerOrder.length,
				problem.numberOfDisplayedChoices == 3 ? 3 : maxNumberOfAnswers);
		for (int i = 0; i < numberOfAnswers; ++i) {
			answers.add(problem.answers[answerOrder[i]]);
			choices.add(problem.choices[answerOrder[i]]);
		}

		problem.shuffledAnswers = shuffle(answers, answerOrder);
		problem.shuffledChoices = shuffle(choices, choiceOrder);
	}

	private String[] shuffle(List<String> strings, int[] order) {
		List<String> result = Lists.newArrayList();
		for (int index : order) {
			if (index < strings.size()) {
				result.add(strings.get(index));
			}
		}
		return result.toArray(new String[0]);
	}

}
