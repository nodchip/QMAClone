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

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.util.Rand;

import com.google.common.base.Objects;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class InputWidgetNarabekae extends InputWidget implements ClickHandler {
	private static final String BUTTON_NARABEKAE = "gwt-Button-narabekae";
	private static final String BUTTON_NARABEKAE_SELECTED = "gwt-Button-narabekaeSelected";
	private VerticalPanel verticalPanel;
	private Grid grid;
	private Button buttons[];
	private Button buttonOk = new Button("OK", this);
	private static final int NOT_SELECTED = -1;
	private int selectedButton = NOT_SELECTED;

	private static class ClickHandlerButton implements ClickHandler {
		private InputWidgetNarabekae widget;
		private int index;

		public ClickHandlerButton(InputWidgetNarabekae widget, int index) {
			this.widget = widget;
			this.index = index;
		}

		@Override
		public void onClick(ClickEvent event) {
			widget.onClick(index);
		}
	}

	public InputWidgetNarabekae(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);
		verticalPanel = new VerticalPanel();

		final String answer = problem.shuffledAnswers[0];
		grid = new Grid(1, answer.length());
		buttons = new Button[answer.length()];
		for (int i = 0; i < answer.length(); ++i) {
			buttons[i] = new Button();
			buttons[i].addClickHandler(new ClickHandlerButton(this, i));
			buttons[i].setText(answer.substring(i, i + 1));
			buttons[i].setStyleName(BUTTON_NARABEKAE);
			grid.setWidget(0, i, buttons[i]);
		}

		verticalPanel.add(grid);

		buttonOk.setStyleName("gwt-Button-narabekaeControl");
		verticalPanel.add(buttonOk);
		verticalPanel.setCellHorizontalAlignment(buttonOk, VerticalPanel.ALIGN_CENTER);

		randomShuffle(problem);

		add(verticalPanel);
		add(new HTML("ボタンを押して文字を入れ替えて正しい順序に並び替えてください<br/>最後にOKを押してください"));
	}

	public void enable(boolean b) {
		for (int i = 0; i < buttons.length; ++i) {
			buttons[i].setEnabled(b);
		}
		buttonOk.setEnabled(false);
	}

	private void onClick(int i) {
		playSound(Constant.SOUND_URL_BUTTON_PUSH);

		if (selectedButton == NOT_SELECTED) {
			selectedButton = i;
			buttons[i].setEnabled(false);
			buttons[i].setEnabled(true);
			buttons[i].setStyleName(BUTTON_NARABEKAE_SELECTED);
		} else {
			swapButtonText(i, selectedButton);
			selectedButton = NOT_SELECTED;
		}
	}

	private void randomShuffle(PacketProblem problem) {
		Rand rand = new Rand(Objects.hashCode(problem.id, problem.shuffledChoices,
				problem.shuffledAnswers));
		for (int left = 0; left < buttons.length; ++left) {
			int right = rand.get(buttons.length - left) + left;
			swapButtonText(left, right);
		}
	}

	private void swapButtonText(int i, int j) {
		String sI = buttons[i].getText();
		String sJ = buttons[j].getText();

		buttons[i].setText(sJ);
		buttons[j].setText(sI);

		grid.clearCell(0, i);
		grid.setWidget(0, i, buttons[i]);
		grid.clearCell(0, j);
		grid.setWidget(0, j, buttons[j]);

		buttons[i].setEnabled(false);// 外側の黒枠を消すため
		buttons[i].setEnabled(true);
		buttons[i].setStyleName(BUTTON_NARABEKAE);
		buttons[j].setEnabled(false);
		buttons[j].setEnabled(true);
		buttons[j].setStyleName(BUTTON_NARABEKAE);
	}

	@Override
	public void onClick(ClickEvent event) {
		final Object sender = event.getSource();
		if (sender == buttonOk) {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < buttons.length; ++i) {
				sb.append(buttons[i].getText());
			}
			final String answer = sb.toString();
			answerView.set(answer, false);
			sendAnswer(answer);
		}
	}

	@Override
	protected void hideAnswer() {
		super.hideAnswer();
		for (Button button : buttons) {
			button.setText("");
		}
	}
}
