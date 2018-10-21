package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;

/**
 * 文字パメル用 {@link Shuffleable}.
 * 
 * @author nodchip
 */
public class ShufflerMojiPanel implements Shuffleable {
	@Override
	public void shuffle(PacketProblem problem, int[] answerOrder, int[] choiceOrder) {
		// BugTrack-QMAClone/429 - QMAClone wiki
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F429
		Preconditions.checkNotNull(answerOrder);
		Preconditions.checkNotNull(choiceOrder);

		// BugTrack-QMAClone/445 - QMAClone wiki
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F445
		problem.shuffledAnswers = problem.getAnswerList().toArray(new String[0]);

		problem.shuffledChoices = new String[choiceOrder.length];
		for (int i = 0; i < choiceOrder.length; ++i) {
			problem.shuffledChoices[choiceOrder[i]] = problem.choices[i];
		}
	}
}
