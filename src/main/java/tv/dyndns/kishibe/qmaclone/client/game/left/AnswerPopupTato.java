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
package tv.dyndns.kishibe.qmaclone.client.game.left;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.user.client.ui.AbsolutePanel;

public class AnswerPopupTato extends AnswerPopupCommon {
	private final PacketProblem problem;

	public AnswerPopupTato(AbsolutePanel parentPanel, PacketProblem problem) {
		super(parentPanel);
		this.problem = problem;
	}

	public void show(String s) {
		if (isSystemMessage(s)) {
			super.show(s);
			return;
		}

		final String[] split = s.split(Constant.DELIMITER_GENERAL);
		final int length = split.length;
		final int[] indexes = new int[length];
		for (int i = 0; i < length; ++i) {
			final int index = problem.getShuffledChoiceIndex(split[i]);
			indexes[i] = index;
		}
		sort(indexes);

		final StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < length; ++i) {
			final int index = indexes[i];
			buffer.append(getLetter(ChoiceMarkType.Digit, index));
		}

		s = buffer.toString();
		super.show(s);
	}

	private void sort(int[] a) {
		final int n = a.length;
		for (int i = 0; i < n; ++i) {
			for (int j = n - 1; j > i; --j) {
				if (a[i] > a[j]) {
					int temp = a[i];
					a[i] = a[j];
					a[j] = temp;
				}
			}
		}
	}
}
