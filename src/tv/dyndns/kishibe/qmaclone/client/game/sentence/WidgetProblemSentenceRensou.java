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
package tv.dyndns.kishibe.qmaclone.client.game.sentence;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;

public class WidgetProblemSentenceRensou extends WidgetProblemSentence {
	private static final int NUMBER_OF_LINES = 4;
	private static final int LETTER_UPDATE_DURATION = 50;
	private final RepeatingCommand commandUpdate = new RepeatingCommand() {
		@Override
		public boolean execute() {
			update(startTime, System.currentTimeMillis());
			return lineIndex < NUMBER_OF_LINES && index < sentenceLength && isAttached();
		}
	};
	private final String sentence;
	private final int sentenceLength;
	@VisibleForTesting
	final HTML htmlSentence = new HTML();
	private int index = 0;
	@VisibleForTesting
	StringBuilder[] lines;
	private long startTime;
	private int lineIndex;
	private boolean firstLetterInLine;

	public WidgetProblemSentenceRensou(PacketProblem problem) {
		sentence = problem.getPanelSentence().replaceAll("%c\n", "%c").trim();
		sentenceLength = sentence.length();

		clearSentence();

		// 初めから全て表示する場合
		if (sentence.startsWith("!")) {
			String[] sentences = sentence.replaceAll(" ", "").substring(1).split("\n");
			for (StringBuilder sb : lines) {
				sb.delete(0, sb.length());
			}
			int n = Math.min(sentences.length, NUMBER_OF_LINES);
			for (int i = 0; i < n; ++i) {
				lines[i].append(sentences[i]);
			}
			index = sentenceLength;
		}

		htmlSentence.addStyleDependentName("problemStatement");
		htmlSentence.addStyleDependentName("problemStatementCenter");
		htmlSentence.setSize("600px", "200px");

		DecoratorPanel decoratorPanel = new DecoratorPanel();
		decoratorPanel.setWidget(htmlSentence);
		add(decoratorPanel);
	}

	@VisibleForTesting
	void update(long startTime, long currentTime) {
		int expectedIndex = (int) (currentTime - startTime) / LETTER_UPDATE_DURATION;
		while (updateLetter() && index < expectedIndex)
			;

		htmlSentence.setHTML(new SafeHtmlBuilder().appendEscapedLines(Joiner.on('\n').join(lines))
				.toSafeHtml());
	}

	/**
	 * 内部状態を問題文1文字分更新する
	 * 
	 * @return 全て更新し終えたらfalse
	 */
	@VisibleForTesting
	boolean updateLetter() {
		if (lineIndex >= NUMBER_OF_LINES || index >= sentenceLength) {
			return false;
		}

		char ch = sentence.charAt(index++);
		switch (ch) {
		case ' ':
			break;

		case '\n':
			++lineIndex;
			firstLetterInLine = true;
			break;

		case '%':
			if (index < sentenceLength && sentence.charAt(index) == 'c') {
				clearSentence();
				++index;
			} else {
				addCharacter(ch);
			}
			break;

		default:
			addCharacter(ch);
			break;
		}

		return index < sentenceLength;
	}

	private void addCharacter(char ch) {
		if (firstLetterInLine) {
			lines[lineIndex] = new StringBuilder();
			firstLetterInLine = false;
		}
		lines[lineIndex].append(ch);
	}

	private void clearSentence() {
		lines = createInitialSentences();
		lineIndex = 0;
		firstLetterInLine = true;
	}

	private StringBuilder[] createInitialSentences() {
		StringBuilder[] sentences = new StringBuilder[NUMBER_OF_LINES];
		for (int i = 0; i < NUMBER_OF_LINES; ++i) {
			sentences[i] = new StringBuilder("『ヒント" + (i + 1) + "』");
		}
		return sentences;
	}

	protected void onLoad() {
		super.onLoad();
		startTime = System.currentTimeMillis();
		Scheduler.get().scheduleFixedPeriod(commandUpdate, LETTER_UPDATE_DURATION);
		update(startTime, startTime);
	}

	protected void onUnload() {
		// 過去のブラウザで前の問題の動画が残ってししまう件への対応
		htmlSentence.setHTML("");
		super.onUnload();
	}
}
