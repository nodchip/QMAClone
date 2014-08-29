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

import java.util.Set;
import java.util.TreeSet;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class InputWidgetTato extends InputWidget implements ClickHandler {
	private static final String STYLE_NAME = "gwt-Button-tato";
	private static final String STYLE_NAME_SELECTED = "gwt-Button-tatoSelected";
	private static final String STYLE_NAME_IMAGE = "gwt-Button-tatoImage";
	private static final String STYLE_NAME_IMAGE_SELECTED = "gwt-Button-tatoImage";
	private final Button[] buttonChoice;
	private final Button buttonOk = new Button("OK", this);
	private final Set<Integer> selectedIndex = new TreeSet<Integer>();
	private final boolean isImage;
	private final int numberOfChoice;

	public interface MyTemplate extends SafeHtmlTemplates {
		@Template("{0}<img src='{1}' width='160px' height='120px'/>")
		SafeHtml image(String letter, SafeUri imageUrl);
	}

	private static final MyTemplate TEMPLATE = GWT.create(MyTemplate.class);

	public InputWidgetTato(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);
		this.isImage = problem.imageChoice;
		this.numberOfChoice = problem.getNumberOfShuffledChoices();
		this.buttonChoice = new Button[numberOfChoice];

		if (problem.imageChoice) {
			Grid grid = new Grid(2, 2);
			for (int i = 0; i < numberOfChoice; ++i) {
				String imageUrl = problem.getShuffledChoice4AsImageUrl(i);
				buttonChoice[i] = new Button(TEMPLATE.image(getLetter(DIGIT, i),
						UriUtils.fromString(imageUrl)), this);
				buttonChoice[i].setStyleName(STYLE_NAME_IMAGE);
				grid.setWidget(i / 2, i % 2, buttonChoice[i]);
			}
			add(grid);
		} else {
			Grid grid = new Grid(numberOfChoice, 2);
			for (int i = 0; i < numberOfChoice; ++i) {
				Label label = new Label(getLetter(DIGIT, i));
				label.addStyleDependentName("buttonIndex");
				grid.setWidget(i, 0, label);

				String choice = problem.shuffledChoices[i];
				buttonChoice[i] = new Button(new SafeHtmlBuilder().appendEscapedLines(
						choice.replaceAll("%n", "\n")).toSafeHtml(), this);
				buttonChoice[i].setStyleName(STYLE_NAME);
				grid.setWidget(i, 1, buttonChoice[i]);
			}
			add(grid);
		}

		buttonOk.setStyleName("gwt-Button-tatoControl");
		add(buttonOk);
	}

	public void enable(boolean b) {
		for (int i = 0; i < buttonChoice.length; ++i) {
			buttonChoice[i].setEnabled(b);
		}
		buttonOk.setEnabled(b);
	}

	@Override
	public void onClick(ClickEvent event) {
		Widget sender = (Widget) event.getSource();
		if (sender == buttonOk) {
			StringBuilder answerSend = new StringBuilder();
			for (int i = 0; i < numberOfChoice; ++i) {
				if (selectedIndex.contains(i)) {
					if (answerSend.length() != 0) {
						answerSend.append(Constant.DELIMITER_GENERAL);
					}
					answerSend.append(problem.shuffledChoices[i]);
				}
			}
			sendAnswer(answerSend.toString());

		} else {
			int clickedIndex = 0;
			for (; clickedIndex < problem.getNumberOfShuffledChoices(); ++clickedIndex) {
				if (sender == buttonChoice[clickedIndex]) {
					break;
				}
			}

			// 表示書き換え
			if (selectedIndex.contains(clickedIndex)) {
				selectedIndex.remove(clickedIndex);
			} else {
				selectedIndex.add(clickedIndex);
			}

			StringBuilder buffer = new StringBuilder();
			for (int index : selectedIndex) {
				buffer.append(getLetter(DIGIT, index));
			}
			answerView.set(buffer.toString(), false);

			// 音を鳴らす
			playSound(Constant.SOUND_URL_BUTTON_PUSH);

			// ボタン表示を更新する
			if (isImage) {
				if (selectedIndex.contains(clickedIndex)) {
					sender.setStyleName(STYLE_NAME_IMAGE_SELECTED);
				} else {
					sender.setStyleName(STYLE_NAME_IMAGE);
				}
			} else {
				if (selectedIndex.contains(clickedIndex)) {
					sender.setStyleName(STYLE_NAME_SELECTED);
				} else {
					sender.setStyleName(STYLE_NAME);
				}
			}
		}
	}
}
