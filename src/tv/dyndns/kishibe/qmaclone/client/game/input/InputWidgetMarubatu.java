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
package tv.dyndns.kishibe.qmaclone.client.game.input;

import java.util.HashMap;
import java.util.Map;

import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;

public class InputWidgetMarubatu extends InputWidget implements ClickHandler {
	private static final String STYLE_MARUBATU = "gwt-Button-yesNo";
	private static final String STYLE_MARUBATU_IMAGE = "gwt-Button-yesNoImage";
	private final Grid grid = new Grid(1, 2);
	private final Button button[] = new Button[2];
	private final Map<Button, String> map = new HashMap<Button, String>();

	public InputWidgetMarubatu(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);

		if (problem.imageChoice) {
			for (int i = 0; i < 2; ++i) {
				final String choice = problem.shuffledChoices[i];
				final String html = getLetter(ALPHA, i) + "<img src='"
						+ problem.getShuffledChoice2AsImageUrl(i)
						+ "' width='200px' height='150px'/>";
				button[i] = new Button(html, this);
				button[i].setStyleName(STYLE_MARUBATU_IMAGE);
				map.put(button[i], choice);
			}
		} else {
			button[0] = new Button("○", this);
			button[1] = new Button("×", this);
			button[0].setStyleName(STYLE_MARUBATU);
			button[1].setStyleName(STYLE_MARUBATU);
			map.put(button[0], "○");
			map.put(button[1], "×");
		}

		grid.setWidget(0, 0, button[0]);
		grid.setWidget(0, 1, button[1]);

		add(grid);
		add(new HTML("○×で答えてください"));
	}

	public void enable(boolean b) {
		for (int i = 0; i < button.length; ++i) {
			button[i].setEnabled(false);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		final Object sender = event.getSource();
		final String answer = map.get(sender);
		if (problem.imageChoice) {
			final int index = problem.getShuffledChoiceIndex(answer);
			answerView.set(getLetter(ALPHA, index), false);
		} else {
			answerView.set(answer, false);
		}
		sendAnswer(answer);
	}
}
