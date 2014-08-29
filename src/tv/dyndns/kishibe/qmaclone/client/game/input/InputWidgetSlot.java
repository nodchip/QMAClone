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
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

public class InputWidgetSlot extends InputWidget implements ClickHandler {
	private static final int ALL_DRUMS = -1;
	private final int width;
	private final int height;
	private final String[][] letters;
	private final Grid gridDrums;
	private final Map<Button, int[]> buttonToRotate = new HashMap<Button, int[]>();
	private final Button buttonOk = new Button("OK", this);

	public InputWidgetSlot(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);
		this.width = problem.shuffledAnswers[0].length();
		this.height = problem.getNumberOfShuffledAnswers();
		this.letters = new String[width][height];

		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				letters[x][y] = problem.shuffledAnswers[y].substring(x, x + 1);
			}
			for (int y = 0; y < height; ++y) {
				final int index = Random.nextInt(height);
				String s = letters[x][y];
				letters[x][y] = letters[x][index];
				letters[x][index] = s;
			}
		}

		gridDrums = new Grid(5, width);
		for (int x = 0; x < width; ++x) {
			{
				final Button button = new Button("▲", this);
				button.setStyleName("gwt-Button-slot");
				gridDrums.setWidget(0, x, button);
				buttonToRotate.put(button, new int[] { x, 1 });
			}
			{
				final Button button = new Button("▼", this);
				button.setStyleName("gwt-Button-slot");
				gridDrums.setWidget(4, x, button);
				buttonToRotate.put(button, new int[] { x, 0 });
			}
			for (int y = 1; y <= 3; ++y) {
				final Label label = new Label();
				label.addStyleDependentName("slot");
				gridDrums.setWidget(y, x, label);
			}

			for (int y = 0; y < 5; ++y) {
				gridDrums.getCellFormatter().setAlignment(y, x, ALIGN_CENTER, ALIGN_MIDDLE);
			}
		}
		gridDrums.getRowFormatter().addStyleName(2, "gwt-Grid-Row-slotCenter");

		setHorizontalAlignment(ALIGN_CENTER);
		add(gridDrums);

		buttonOk.setStyleName("gwt-Button-slotControl");
		add(buttonOk);

		updateSlot(ALL_DRUMS);
	}

	private void rotate(int x, boolean up) {
		if (up) {
			String temp = letters[x][0];
			for (int i = 0; i < height - 1; ++i) {
				letters[x][i] = letters[x][i + 1];
			}
			letters[x][height - 1] = temp;

		} else {
			String temp = letters[x][height - 1];
			for (int i = height - 1; i > 0; --i) {
				letters[x][i] = letters[x][i - 1];
			}
			letters[x][0] = temp;
		}

		updateSlot(x);
	}

	private void updateSlot(int x) {
		final int start = x == ALL_DRUMS ? 0 : x;
		final int end = x == ALL_DRUMS ? width : x + 1;

		for (int xx = start; xx < end; ++xx) {
			for (int y = 0; y < 3; ++y) {
				final Label label = (Label) gridDrums.getWidget(y + 1, xx);
				label.setText(letters[xx][y]);
			}
		}
	}

	public void enable(boolean b) {
		for (Button button : buttonToRotate.keySet()) {
			button.setEnabled(b);
		}

		buttonOk.setEnabled(b);
	}

	private String getAnswer() {
		final StringBuilder b = new StringBuilder();
		for (int x = 0; x < width; ++x) {
			b.append(letters[x][1]);
		}
		return b.toString();
	}

	@Override
	public void onClick(ClickEvent event) {
		final Object sender = event.getSource();
		if (buttonToRotate.containsKey(sender)) {
			final int[] args = (int[]) buttonToRotate.get(sender);
			final int x = args[0];
			final boolean up = args[1] == 1;
			rotate(x, up);

		} else if (sender == buttonOk) {
			final String answer = getAnswer();
			answerView.set(answer, false);
			sendAnswer(answer);
		}
	}
}
