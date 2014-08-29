package tv.dyndns.kishibe.qmaclone.client.game.left;

import java.util.Collections;
import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class AnswerPopupGroup extends AnswerPopupCommon {
	private final PacketProblem problem;

	public AnswerPopupGroup(AbsolutePanel parentPanel, PacketProblem problem) {
		super(parentPanel);
		this.problem = problem;
	}

	public void show(String s) {
		if (isSystemMessage(s)) {
			super.show(s);
			return;
		}

		List<String> sortedAnswers = Lists
				.newArrayList(ImmutableSet.copyOf(problem.getAnswerList()));
		Collections.sort(sortedAnswers);

		String[] split = s.split(Constant.DELIMITER_GENERAL);
		int length = split.length;
		int[] indexArray = new int[length];
		for (int i = 0; i < length; ++i) {
			String[] strings = split[i].split(Constant.DELIMITER_KUMIAWASE_PAIR);
			// デバッグ実行で以下の処理でエラーが発生するため
			if (strings.length != 2) {
				return;
			}
			String left = strings[0];
			String right = strings[1];
			int leftIndex = problem.getShuffledChoiceIndex(left);
			int rightIndex = sortedAnswers.indexOf(right);
			indexArray[leftIndex] = rightIndex;
		}

		StringBuilder sb = new StringBuilder();
		for (int index : indexArray) {
			sb.append(getLetter(ChoiceMarkType.Alpha, index));
		}
		super.show(sb.toString());
	}
}
