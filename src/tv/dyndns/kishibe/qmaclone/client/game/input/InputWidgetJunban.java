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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

public class InputWidgetJunban extends InputWidget implements ClickHandler {
	private static final String STYLE_NAME = "gwt-Button-junban";
	private static final String STYLE_NAME_SELECTED = "gwt-Button-junbanSelected";
	private static final String STYLE_NAME_IMAGE = "gwt-Button-junbanImage";
	private static final String STYLE_NAME_IMAGE_SELECTED = "gwt-Button-junbanImageSelected";
	private final Map<Button, Integer> buttonToIndex = new HashMap<Button, Integer>();
	private final List<Integer> selectedIndex = new ArrayList<Integer>();
	private final boolean isImage;

	public interface MyTemplate extends SafeHtmlTemplates {
		@Template("{0}<img src='{1}' width='160px' height='120px'/>")
		SafeHtml image(String letter, SafeUri imageUrl);
	}

	private static final MyTemplate TEMPLATE = GWT.create(MyTemplate.class);

	public InputWidgetJunban(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);
		this.isImage = problem.imageAnswer;

		// shuffledChoicesに表示予定の選択肢
		// shuffledAnswersに解答が含まれている
		int numberOfChoices = problem.getNumberOfShuffledChoices();

		if (problem.imageAnswer) {
			Grid grid = new Grid(2, 2);
			for (int i = 0; i < numberOfChoices; ++i) {
				String imageUrl = problem.getShuffledChoice4AsImageUrl(i);
				Button button = new Button(TEMPLATE.image(getLetter(ALPHA, i),
						UriUtils.fromString(imageUrl)), this);
				buttonToIndex.put(button, i);
				button.setStyleName(STYLE_NAME_IMAGE);
				grid.setWidget(i / 2, i % 2, button);
			}
			add(grid);

		} else {
			Grid grid = new Grid(numberOfChoices, 2);
			for (int i = 0; i < numberOfChoices; ++i) {
				Label label = new Label(getLetter(ALPHA, i));
				label.addStyleDependentName("buttonIndex");
				grid.setWidget(i, 0, label);

				String choice = problem.shuffledChoices[i];
				Button button = new Button(toMultilineSafeHtml(choice), this);
				buttonToIndex.put(button, i);
				button.setStyleName(STYLE_NAME);
				grid.setWidget(i, 1, button);
			}
			add(grid);
		}
	}

	public void enable(boolean b) {
		for (Button button : buttonToIndex.keySet()) {
			button.setEnabled(b);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		final Object sender = event.getSource();

		// 入力文字更新
		if (!buttonToIndex.containsKey(sender)) {
			return;
		}

		final Integer clickedIndex = buttonToIndex.get(sender);

		if (selectedIndex.contains(clickedIndex)) {
			selectedIndex.remove(clickedIndex);
		} else {
			selectedIndex.add(clickedIndex);
		}

		// 画面表示更新
		{
			final StringBuilder buffer = new StringBuilder();
			for (int index : selectedIndex) {
				buffer.append(getLetter(ALPHA, index));
			}
			answerView.set(buffer.toString(), false);
		}

		// ボタン表示変更
		for (Entry<Button, Integer> entry : buttonToIndex.entrySet()) {
			final Button button = entry.getKey();
			final int index = entry.getValue();

			if (isImage) {
				if (selectedIndex.contains(index)) {
					button.setStyleName(STYLE_NAME_IMAGE_SELECTED);
				} else {
					button.setStyleName(STYLE_NAME_IMAGE);
				}
			} else {
				if (selectedIndex.contains(index)) {
					button.setStyleName(STYLE_NAME_SELECTED);
				} else {
					button.setStyleName(STYLE_NAME);
				}
			}
		}

		// 回答送信
		if (selectedIndex.size() == problem.getNumberOfShuffledAnswers()) {
			List<String> order = Lists.newArrayList();
			for (int index : selectedIndex) {
				order.add(problem.shuffledChoices[index]);
			}
			sendAnswer(Joiner.on(Constant.DELIMITER_GENERAL).join(order));

		} else {
			playSound(Constant.SOUND_URL_BUTTON_PUSH);
		}
	}
}
