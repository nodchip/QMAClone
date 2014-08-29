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

import java.util.Arrays;

import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class InputWidget4Taku extends InputWidget implements ClickHandler {
	private static final int NUMBER_OF_CHOICES = 4;
	private static final String STYLE_4TAKU = "gwt-Button-4taku";
	private static final String STYLE_4TAKU_IMAGE = "gwt-Button-4takuImage";
	private final Button buttonChoice[];
	private final int letterType;

	public interface MyTemplate extends SafeHtmlTemplates {
		@Template("{0}<img src='{1}' width='160px' height='120px'/>")
		SafeHtml image(String letter, SafeUri imageUrl);
	}

	private static final MyTemplate TEMPLATE = GWT.create(MyTemplate.class);

	public InputWidget4Taku(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);
		letterType = (problem.type == ProblemType.YonTaku) ? DIGIT : ALPHA;
		buttonChoice = new Button[NUMBER_OF_CHOICES];

		if (problem.imageChoice) {
			Grid grid = new Grid(2, 2);
			int letterType = (problem.type == ProblemType.YonTaku) ? DIGIT : ALPHA;

			for (int i = 0; i < NUMBER_OF_CHOICES; ++i) {
				String imageUrl = problem.getShuffledChoice4AsImageUrl(i);
				buttonChoice[i] = new Button(TEMPLATE.image(getLetter(letterType, i),
						UriUtils.fromString(imageUrl)), this);
				buttonChoice[i].setStyleName(STYLE_4TAKU_IMAGE);
				grid.setWidget(i / 2, i % 2, buttonChoice[i]);
			}
			add(grid);

		} else {
			Grid grid = new Grid(NUMBER_OF_CHOICES, 2);
			int letterType = (problem.type == ProblemType.YonTaku) ? DIGIT : ALPHA;

			for (int i = 0; i < NUMBER_OF_CHOICES; ++i) {
				Label label = new Label(getLetter(letterType, i));
				label.addStyleDependentName("buttonIndex");
				grid.setWidget(i, 0, label);

				String choice = problem.shuffledChoices[i];
				buttonChoice[i] = new Button(new SafeHtmlBuilder().appendEscapedLines(
						choice.replaceAll("%n", "\n")).toSafeHtml(), this);
				buttonChoice[i].setStyleName(STYLE_4TAKU);
				grid.setWidget(i, 1, buttonChoice[i]);
			}

			add(grid);
		}

		add(new HTML("4つの選択肢から正しい答えを選んでください"));
	}

	public void enable(boolean b) {
		for (Button button : buttonChoice) {
			button.setEnabled(b);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		int index = Arrays.asList(buttonChoice).indexOf(sender);
		String letter = getLetter(letterType, index);
		answerView.set(letter, false);
		sendAnswer(problem.shuffledChoices[index]);
	}
}
