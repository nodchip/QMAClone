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
import com.google.common.collect.Sets;

public class ValidatorSlot extends Validator {
	private static final int NUMBER_OF_ANSWERS = 4;

	public Evaluation check(PacketProblem problem) {
		Preconditions.checkArgument(problem.type == ProblemType.Slot);

		Evaluation eval = super.check(problem);
		List<String> warn = eval.warn;

		if (problem.getNumberOfAnswers() < NUMBER_OF_ANSWERS) {
			warn.add("解答は4つ必要です");
			return eval;
		}

		Set<Integer> lengthsOfAnswers = Sets.newHashSet();
		for (String answer : problem.getAnswerList()) {
			lengthsOfAnswers.add(answer.length());
		}

		if (lengthsOfAnswers.size() != 1) {
			warn.add("解答の長さがそろっていません");
			return eval;
		}

		int numberOfLetters = problem.answers[0].length();
		if (numberOfLetters < 2) {
			warn.add("解答は2文字以でなければなりません");
		} else if (6 < numberOfLetters) {
			warn.add("解答は6文字以下でなければなりません");
		}

		for (int column = 0; column < numberOfLetters; ++column) {
			Set<Character> letters = Sets.newHashSet();
			for (int row = 0; row < NUMBER_OF_ANSWERS; ++row) {
				letters.add(problem.answers[row].charAt(column));
			}

			if (letters.size() != NUMBER_OF_ANSWERS) {
				warn.add((column + 1) + "文字目の縦列に重複している文字があります");
			}
		}

		return eval;
	}
}
