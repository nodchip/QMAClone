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

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.ButtonLetter;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.util.Random;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;

public class InputWidgetMojiPanel extends InputWidget implements ClickHandler {
	private final List<ButtonLetter> buttons = Lists.newArrayList();
	private final int answerLength;

	public InputWidgetMojiPanel(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);
		this.answerLength = problem.shuffledAnswers[0].length();

		String choice = problem.choices[0];
		int numberOfChoices = choice.length();
		int perm[] = Random.get().makePermutationArray(numberOfChoices);
		Grid grid = new Grid(2, numberOfChoices / 2);

		for (int i = 0; i < numberOfChoices; ++i) {
			ButtonLetter button = new ButtonLetter(choice.substring(perm[i], perm[i] + 1),
					answerView);
			buttons.add(button);
			button.setStyleName("gwt-Button-mojipanel");
			button.addClickHandler(this);
			grid.setWidget(i / (numberOfChoices / 2), i % (numberOfChoices / 2), button);
		}

		add(grid);
		add(new HTML("上記から" + answerLength + "文字を選んで答えてください<br/>同じ文字を何度でも使えます"));
	}

	@Override
	public void enable(boolean b) {
		for (Button button : buttons) {
			button.setEnabled(b);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		if (answerView.get().length() == answerLength) {
			sendAnswer(answerView.get());
		} else {
			playSound(Constant.SOUND_URL_BUTTON_PUSH);
		}
	}
}
