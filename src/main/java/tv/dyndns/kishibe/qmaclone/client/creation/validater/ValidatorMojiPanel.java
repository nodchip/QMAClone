//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import java.util.List;
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;

public final class ValidatorMojiPanel extends Validator {
	public Evaluation check(PacketProblem problem) {
		Preconditions.checkArgument(problem.type == ProblemType.MojiPanel);

		Evaluation eval = super.check(problem);
		List<String> warn = eval.warn;

		List<String> answerList = problem.getAnswerList();
		if (answerList.isEmpty()) {
			warn.add("解答が入力されていません");
			return eval;
		}

		Set<Integer> answerLengths = Sets.newHashSet();
		for (int answerIndex = 0; answerIndex < answerList.size(); ++answerIndex) {
			String answer = answerList.get(answerIndex);

			if (answer.length() < 3) {
				warn.add((answerIndex + 1) + "番目の解答が短すぎます(3文字以上必要です)");
			}

			if (6 < answer.length()) {
				warn.add((answerIndex + 1) + "番目の解答が長すぎます(6文字以下でなければなりません)");
			}

			answerLengths.add(answer.length());
		}

		if (answerLengths.size() != 1) {
			warn.add("解答の長さがそろっていません");
		}

		List<String> choiceList = problem.getChoiceList();

		if (choiceList.isEmpty()) {
			warn.add("選択文字群が入力されていません");
			return eval;
		}

		String answer = answerList.get(0);
		String choice = choiceList.get(0);
		if (answer.length() == 3 && choice.length() != 6 && choice.length() != 8) {
			warn.add("解答が3文字の場合は選択文字群には6文字又は8文字必要です");
		} else if (4 <= answer.length() && answer.length() <= 6 && choice.length() != 10) {
			warn.add("解答が4～6文字の場合は選択文字群には10文字必要です");
		}

		Set<Character> letters = toCharacters(choice);
		if (letters.size() != choice.length()) {
			warn.add("選択文字群に同じ文字が含まれています");
		}

		for (int i = 0; i < answerList.size(); ++i) {
			Set<Character> answerLetters = toCharacters(answerList.get(i));

			if (!letters.containsAll(answerLetters)) {
				warn.add((i + 1) + "番目の解答の文字が選択文字群に含まれていません");
			}
		}

		return eval;
	}

	private Set<Character> toCharacters(String s) {
		// BugTrack-QMAClone/383 - QMAClone wiki
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F383
		Builder<Character> builder = new ImmutableSet.Builder<Character>();
		for (char ch : s.toCharArray()) {
			builder.add(ch);
		}
		return builder.build();
	}
}
