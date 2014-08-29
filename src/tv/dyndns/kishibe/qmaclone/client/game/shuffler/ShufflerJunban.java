package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ShufflerJunban implements Shuffleable {

	@Override
	public void shuffle(PacketProblem problem, int[] answerOrder, int[] choiceOrder) {
		Preconditions.checkNotNull(answerOrder);
		Preconditions.checkNotNull(choiceOrder);

		if (answerOrder.length < 3) {
			problem.shuffledAnswers = null;
			problem.shuffledChoices = null;
			return;
		}

		List<String> answers = Lists.newArrayList();
		int numberOfAnswers = Math.min(answerOrder.length, problem.numberOfDisplayedChoices);
		for (int i = 0; i < numberOfAnswers; ++i) {
			answers.add(problem.answers[answerOrder[i]]);
		}

		problem.shuffledChoices = answers.toArray(new String[0]);

		problem.shuffledAnswers = answers.toArray(new String[0]);
		final List<String> originalAnswers = problem.getAnswerList();
		Arrays.sort(problem.shuffledAnswers, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return originalAnswers.indexOf(o1) - originalAnswers.indexOf(o2);
			}
		});
	}

}
