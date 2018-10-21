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

import tv.dyndns.kishibe.qmaclone.client.game.WidgetBackgroundImage;
import tv.dyndns.kishibe.qmaclone.client.game.WidgetBackgroundYouTube;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class WidgetProblemSentenceNormal extends WidgetProblemSentence {
	// TODO(nodchip):マッチング終了後に全画像のpreload
	private static final int LETTER_UPDATE_DURATION = 50;
	private final String sentence;
	private final int sentenceLength;
	private final HTML htmlSentence = new HTML();
	private final StringBuilder html;
	private int index = 0;
	private boolean finished = false;
	private final RepeatingCommand commandUpdate = new RepeatingCommand() {
		@Override
		public boolean execute() {
			update();
			// 問題文をすべて表示した後はupdateを行わない
			return !finished;
		}
	};
	private long startTime;

	public WidgetProblemSentenceNormal(PacketProblem problem) {
		sentence = problem.getPanelSentence().replaceAll("%c\n", "%c");
		sentenceLength = sentence.length();

		html = new StringBuilder(sentenceLength * 2);

		final HorizontalPanel panel = new HorizontalPanel();
		panel.add(htmlSentence);

		// 画像または動画を使用する場合は問題文の長さを調整する
		htmlSentence.getElement().setAttribute("oncopy", "return false;");
		htmlSentence.getElement().setAttribute("oncut", "return false;");
		htmlSentence.addStyleDependentName("problemStatement");
		if (problem.imageUrl != null) {
			htmlSentence.setPixelSize(360, 200);
			panel.add(new WidgetBackgroundImage(problem, 240, 180));
		} else if (problem.movieUrl != null) {
			htmlSentence.setPixelSize(360, 200);
			panel.add(new WidgetBackgroundYouTube(problem.movieUrl, 240, 200));
		} else {
			htmlSentence.setPixelSize(600, 200);
		}

		final DecoratorPanel decoratorPanel = new DecoratorPanel();
		decoratorPanel.setWidget(panel);
		add(decoratorPanel);
	}

	private void update() {
		long currentTime = System.currentTimeMillis();
		int expectedIndex = (int) (currentTime - startTime) / LETTER_UPDATE_DURATION;
		while (!(finished = !updateLetter()) && index < expectedIndex)
			;

		// BugTrack-QMAClone/622 - QMAClone wiki
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F622
		htmlSentence
				.setHTML(new SafeHtmlBuilder().appendEscapedLines(html.toString()).toSafeHtml());
	}

	private boolean updateLetter() {
		if (index >= sentenceLength) {
			return false;
		}

		final char ch = sentence.charAt(index++);

		switch (ch) {
		case ' ':
			break;
		case '%':
			if (index != sentenceLength && sentence.charAt(index++) == 'c') {
				html.setLength(0);
			}
			break;
		default:
			html.append(ch);
			break;
		}

		return index < sentenceLength;
	}

	protected void onLoad() {
		super.onLoad();
		startTime = System.currentTimeMillis();
		finished = false;
		Scheduler.get().scheduleFixedPeriod(commandUpdate, LETTER_UPDATE_DURATION);
		update();
	}

	protected void onUnload() {
		finished = true;

		// 過去のブラウザで前の問題の動画が残ってししまう件への対応
		htmlSentence.setHTML("");
		super.onUnload();
	}
}
