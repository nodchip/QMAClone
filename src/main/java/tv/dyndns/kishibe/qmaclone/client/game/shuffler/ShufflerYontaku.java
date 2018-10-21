package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ShufflerYontaku implements Shuffleable {
	private static int NUMBER_OF_CHOICES = 4;

	@Override
	public void shuffle(PacketProblem problem, int[] answerOrder, int[] choiceOrder) {
		Preconditions.checkNotNull(answerOrder);
		Preconditions.checkNotNull(choiceOrder);

		if (answerOrder.length < 1 || choiceOrder.length < 4) {
			problem.shuffledAnswers = null;
			problem.shuffledChoices = null;
			return;
		}

		// 解答を含めた選択肢を4つ選ぶ
		List<String> choices = Lists.newArrayList();
		choices.add(problem.answers[0]);
		int numberOfChoices = problem.getNumberOfChoices();
		for (int i = 0; choices.size() < NUMBER_OF_CHOICES && i < numberOfChoices; ++i) {
			String answer = problem.choices[choiceOrder[i]];
			if (!choices.contains(answer)) {
				choices.add(answer);
			}
		}

		// 4つの選択肢を並び替える
		List<Integer> secondOrder = Lists.newArrayList();
		for (int order : choiceOrder) {
			if (order < NUMBER_OF_CHOICES) {
				secondOrder.add(order);
			}
		}

		problem.shuffledChoices = new String[4];
		for (int i = 0; i < NUMBER_OF_CHOICES; ++i) {
			problem.shuffledChoices[i] = choices.get(secondOrder.get(i));
		}
		problem.shuffledAnswers = new String[] { problem.answers[0] };
	}
}
