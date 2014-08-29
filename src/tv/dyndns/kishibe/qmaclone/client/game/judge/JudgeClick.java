package tv.dyndns.kishibe.qmaclone.client.game.judge;

import tv.dyndns.kishibe.qmaclone.client.game.left.AnswerPopup;
import tv.dyndns.kishibe.qmaclone.client.geom.Point;
import tv.dyndns.kishibe.qmaclone.client.geom.Polygon;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

public class JudgeClick implements Judge {
	private static final ImmutableSet<String> INVALID_ANSWER = ImmutableSet.of(
			AnswerPopup.LABEL_ANSWERED, AnswerPopup.LABEL_NO_ANSWER, AnswerPopup.LABEL_TIME_UP);

	@Override
	public boolean judge(PacketProblem problem, String playerAnswer) {
		if (Strings.isNullOrEmpty(playerAnswer) || INVALID_ANSWER.contains(playerAnswer)) {
			return false;
		}

		Point point = Point.fromString(playerAnswer);

		// 無回答の場合に画面表示でエラーが発生するバグへの対処
		// BugTrack-QMAClone/382 - QMAClone wiki
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F382
		if (point == null) {
			return false;
		}

		for (String answer : problem.getShuffledAnswerList()) {
			Polygon polygon = Preconditions.checkNotNull(Polygon.fromString(answer));

			if (polygon.contains(point)) {
				return true;
			}
		}

		return false;
	}
}
