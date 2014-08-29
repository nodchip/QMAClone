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

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;

public final class Validator4Taku extends Validator {
	private final boolean checkWait;

	public Validator4Taku(boolean checkWait) {
		this.checkWait = checkWait;
	}

	public Evaluation check(PacketProblem problem) {
		Preconditions.checkArgument(problem.type == ProblemType.YonTaku
				|| problem.type == ProblemType.Rensou);

		Evaluation eval = super.check(problem);
		List<String> warn = eval.warn;

		if (problem.getNumberOfChoices() < 4) {
			warn.add("選択肢は4つ必要です");
		}

		if (checkWait && !problem.sentence.contains("%w") && !problem.sentence.contains(" ")) {
			eval.info.add("３行目までの各行の末尾に%w等で適宜ウェイトを入れて下さい");
		}

		return eval;
	}
}
