package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;

public class ShufflerDefault implements Shuffleable {
	@Override
	public void shuffle(PacketProblem problem, int[] answerOrder, int[] choiceOrder) {
		Preconditions.checkNotNull(answerOrder);
		Preconditions.checkNotNull(choiceOrder);

		problem.shuffledAnswers = new String[answerOrder.length];
		for (int i = 0; i < answerOrder.length; ++i) {
			problem.shuffledAnswers[answerOrder[i]] = problem.answers[i];
		}

		problem.shuffledChoices = new String[choiceOrder.length];
		for (int i = 0; i < choiceOrder.length; ++i) {
			problem.shuffledChoices[choiceOrder[i]] = problem.choices[i];
		}
	}
}
