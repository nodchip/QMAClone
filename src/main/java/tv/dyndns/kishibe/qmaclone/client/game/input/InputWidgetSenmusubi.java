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

import tv.dyndns.kishibe.qmaclone.client.SoundPlayer;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.ui.PopupCanvas;

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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.widgetideas.graphics.client.Color;

public class InputWidgetSenmusubi extends InputWidget implements ClickHandler {
	private static final String BUTTON_SENMUSUBI = "gwt-Button-senmusubi";
	private static final String BUTTON_SENMUSUBI_SELECTED = "gwt-Button-senmusubiSelected";
	private static final String BUTTON_SENMUSUBI_IMAGE = "gwt-Button-senmusubiImage";
	private static final String BUTTON_SENMUSUBI_IMAGE_SELECTED = "gwt-Button-senmusubiImageSelected";
	private static final int EMPTY = -1;
	private static final double LINE_WIDTH = 3;
	private static final Color LINE_COLOR = Color.RED;
	private final Button[] buttonLeft;
	private final Button[] buttonRight;
	private final Map<Button, Integer> mapRow = new HashMap<Button, Integer>();
	private final Map<Button, Integer> mapCol = new HashMap<Button, Integer>();
	private final int select[];
	private final SimplePanel spacer = new SimplePanel();
	private final int numberOfChoice;
	private int lastRow = EMPTY;
	private int lastCol = EMPTY;
	private final Button buttonOk = new Button("OK", this);
	private final boolean isImageChoice;
	private final boolean isImageAnswer;
	private PopupCanvas canvas;

	public interface MyTemplate extends SafeHtmlTemplates {
		@Template("{0}<img src='{1}' width='120px' height='90px'/>")
		SafeHtml image(String letter, SafeUri imageUrl);
	}

	private static final MyTemplate TEMPLATE = GWT.create(MyTemplate.class);

	public InputWidgetSenmusubi(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);
		this.numberOfChoice = problem.getNumberOfShuffledChoices();
		this.isImageChoice = problem.imageChoice;
		this.isImageAnswer = problem.imageAnswer;

		buttonLeft = new Button[numberOfChoice];
		buttonRight = new Button[numberOfChoice];
		select = new int[numberOfChoice];

		HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlignment(ALIGN_MIDDLE);
		add(panel);

		for (int i = 0; i < numberOfChoice; ++i) {
			select[i] = EMPTY;
		}

		// 左側ボタン群
		if (problem.imageChoice) {
			Grid gridLeft = new Grid(numberOfChoice, 1);
			for (int i = 0; i < numberOfChoice; ++i) {
				String imageUrl = problem.getShuffledChoice4SmallAsImageUrl(i);
				buttonLeft[i] = new Button(TEMPLATE.image(getLetter(ALPHA, i),
						UriUtils.fromString(imageUrl)), this);
				buttonLeft[i].setStyleName(BUTTON_SENMUSUBI_IMAGE);
				mapCol.put(buttonLeft[i], 0);
				mapRow.put(buttonLeft[i], i);
				gridLeft.setWidget(i, 0, buttonLeft[i]);
			}
			panel.add(gridLeft);
		} else {
			Grid gridLeft = new Grid(numberOfChoice, 2);
			for (int i = 0; i < numberOfChoice; ++i) {
				Label label = new Label(getLetter(ALPHA, i));
				label.addStyleDependentName("buttonIndexSmall");
				gridLeft.setWidget(i, 1, label);

				String choice = problem.shuffledChoices[i];
				buttonLeft[i] = new Button(new SafeHtmlBuilder().appendEscapedLines(
						choice.replaceAll("%n", "\n")).toSafeHtml(), this);
				buttonLeft[i].setStyleName(BUTTON_SENMUSUBI);
				mapCol.put(buttonLeft[i], 0);
				mapRow.put(buttonLeft[i], i);
				gridLeft.setWidget(i, 0, buttonLeft[i]);
			}
			panel.add(gridLeft);
		}

		// 線を表示する部分
		int canvasWidth = 40;
		if (problem.imageAnswer) {
			canvasWidth += 40;
		}
		if (problem.imageChoice) {
			canvasWidth += 40;
		}
		int canvasHeight = (problem.imageAnswer || problem.imageChoice) ? 360 : 200;
		spacer.setPixelSize(canvasWidth, canvasHeight);
		panel.add(spacer);

		// 右側ボタン群
		if (problem.imageAnswer) {
			Grid gridRight = new Grid(numberOfChoice, 1);
			for (int i = 0; i < numberOfChoice; ++i) {
				String imageUrl = problem.getShuffledAnswer4SmallAsImageUrl(i);
				buttonRight[i] = new Button(TEMPLATE.image(getLetter(DIGIT, i),
						UriUtils.fromString(imageUrl)), this);
				buttonRight[i].setStyleName(BUTTON_SENMUSUBI_IMAGE);
				mapCol.put(buttonRight[i], 1);
				mapRow.put(buttonRight[i], i);
				gridRight.setWidget(i, 0, buttonRight[i]);
			}
			panel.add(gridRight);
		} else {
			Grid gridRight = new Grid(numberOfChoice, 2);
			for (int i = 0; i < numberOfChoice; ++i) {
				Label label = new Label(getLetter(DIGIT, i));
				label.addStyleDependentName("buttonIndexSmall");
				gridRight.setWidget(i, 0, label);

				String answer = problem.shuffledAnswers[i];
				buttonRight[i] = new Button(new SafeHtmlBuilder().appendEscapedLines(
						answer.replaceAll("%n", "\n")).toSafeHtml(), this);
				buttonRight[i].setStyleName(BUTTON_SENMUSUBI);
				mapCol.put(buttonRight[i], 1);
				mapRow.put(buttonRight[i], i);
				gridRight.setWidget(i, 1, buttonRight[i]);
			}
			panel.add(gridRight);
		}

		// OKボタン
		buttonOk.setStyleName("gwt-Button-senmusubiControl");
		add(buttonOk);
	}

	public void enable(boolean b) {
		for (int i = 0; i < numberOfChoice; ++i) {
			buttonLeft[i].setEnabled(b);
			buttonRight[i].setEnabled(b);
		}
		buttonOk.setEnabled(b);
	}

	private void updateLine() {
		if (canvas == null) {
			canvas = new PopupCanvas(spacer, spacer.getOffsetWidth(), 400);
			canvas.show();
		}

		canvas.setPopupPosition(spacer.getAbsoluteLeft(), spacer.getAbsoluteTop());
		canvas.prepare();

		int offsetCanvasY = canvas.getPopupTop();

		for (int i = 0; i < numberOfChoice; ++i) {
			if (select[i] == EMPTY) {
				continue;
			}

			Button left = buttonLeft[i];
			Button right = buttonRight[select[i]];

			int leftX = 0;
			int leftY = left.getAbsoluteTop() + left.getOffsetHeight() / 2 - offsetCanvasY;
			int rightX = spacer.getOffsetWidth();
			int rightY = right.getAbsoluteTop() + right.getOffsetHeight() / 2 - offsetCanvasY;

			canvas.setLineWidth(LINE_WIDTH);
			canvas.setStrokeStyle(LINE_COLOR);
			canvas.moveTo(leftX, leftY);
			canvas.lineTo(rightX, rightY);
			canvas.stroke();
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		if (sender == buttonOk) {
			sendAnswer("");
			return;
		}

		{
			String styleNameLeft = isImageChoice ? BUTTON_SENMUSUBI_IMAGE : BUTTON_SENMUSUBI;
			String styleNameRight = isImageAnswer ? BUTTON_SENMUSUBI_IMAGE : BUTTON_SENMUSUBI;
			for (int i = 0; i < numberOfChoice; ++i) {
				buttonLeft[i].setStyleName(styleNameLeft);
				buttonRight[i].setStyleName(styleNameRight);
			}
		}

		int row = ((Integer) mapRow.get(sender)).intValue();
		int col = ((Integer) mapCol.get(sender)).intValue();

		if (lastRow == EMPTY || lastCol == col) {
			// 同じ列がクリックされた or 初めてのクリック
			lastRow = row;
			lastCol = col;
			if (col == 0) {
				String styleNameLeft = isImageChoice ? BUTTON_SENMUSUBI_IMAGE_SELECTED
						: BUTTON_SENMUSUBI_SELECTED;
				buttonLeft[row].setStyleName(styleNameLeft);
			} else {
				String styleNameRight = isImageChoice ? BUTTON_SENMUSUBI_IMAGE_SELECTED
						: BUTTON_SENMUSUBI_SELECTED;
				buttonRight[row].setStyleName(styleNameRight);
			}

		} else {
			// 違う列がクリックされた

			int left, right;
			if (col == 0) {
				// 最後は左がクリックされた
				left = row;
				right = lastRow;
			} else {
				// 最後は右がクリックされた
				left = lastRow;
				right = row;
			}

			for (int i = 0; i < numberOfChoice; ++i) {
				// 左→右リンク修正
				if (select[i] == right) {
					select[i] = EMPTY;
				}
			}
			select[left] = right;

			lastRow = EMPTY;
			lastCol = EMPTY;
		}

		updateLine();
		updateLine();

		for (int i = 0; i < numberOfChoice; ++i) {
			if (select[i] == EMPTY) {
				SoundPlayer.getInstance().play(Constant.SOUND_URL_BUTTON_PUSH);
				return;
			}
		}

		// 回答送信
		StringBuilder answerSend = new StringBuilder();
		StringBuilder answerDisplay = new StringBuilder();
		for (int i = 0; i < numberOfChoice; ++i) {
			if (i != 0) {
				answerSend.append(Constant.DELIMITER_GENERAL);
			}
			answerSend.append(problem.shuffledChoices[i]).append(Constant.DELIMITER_KUMIAWASE_PAIR)
					.append(problem.shuffledAnswers[select[i]]);

			int index = select[i];
			answerDisplay.append(getLetter(DIGIT, index));
		}
		answerView.set(answerDisplay.toString(), false);
		sendAnswer(answerSend.toString());
	}

	@Override
	protected void hideAnswer() {
		super.hideAnswer();
		canvas.clear();
	}

	@Override
	protected void onUnload() {
		if (canvas != null) {
			canvas.hide();
		}
		super.onUnload();
	}
}
