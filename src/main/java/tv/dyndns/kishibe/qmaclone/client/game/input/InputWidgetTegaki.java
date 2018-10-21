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
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.handwriting.StrokeCanvas;
import tv.dyndns.kishibe.qmaclone.client.game.handwriting.StrokeCanvasListener;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class InputWidgetTegaki extends InputWidget implements ClickHandler, StrokeCanvasListener {
	private static final Logger logger = Logger.getLogger(InputWidgetTegaki.class.getName());
	private static final int GRID_COLUMS = 3;
	private static final int GRID_ROWS = 4;
	public static final int NUMBER_OF_CANDIDATE = GRID_COLUMS * GRID_ROWS;
	private final StrokeCanvas strokeCanvas = new StrokeCanvas(this);
	private final Button[] buttons = new Button[NUMBER_OF_CANDIDATE];
	private final Button buttonOk = new Button("OK", this);
	private final Button buttonDelete = new Button("削除", this);
	private final Button buttonClear = new Button("クリア", this);

	public InputWidgetTegaki(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);

		for (int i = 0; i < NUMBER_OF_CANDIDATE; ++i) {
			buttons[i] = new Button("", this);
			buttons[i].setStyleName("gwt-Button-tegaki");
		}

		final HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setVerticalAlignment(ALIGN_BOTTOM);

		{
			// 文字候補
			final VerticalPanel panel = new VerticalPanel();
			panel.add(new Label("文字候補"));

			{
				final Grid grid = new Grid(GRID_ROWS, GRID_COLUMS);
				int index = 0;
				for (int row = 0; row < GRID_ROWS; ++row) {
					for (int column = 0; column < GRID_COLUMS; ++column) {
						grid.setWidget(row, column, buttons[index++]);
					}
				}
				panel.add(grid);
			}
			horizontalPanel.add(panel);
		}

		horizontalPanel.add(strokeCanvas);

		{
			final VerticalPanel verticalPanel = new VerticalPanel();
			buttonClear.setStyleName("gwt-Button-tegakiControl");
			buttonDelete.setStyleName("gwt-Button-tegakiControl");
			buttonOk.setStyleName("gwt-Button-tegakiControl");
			verticalPanel.add(buttonClear);
			verticalPanel.add(buttonDelete);
			verticalPanel.add(buttonOk);
			horizontalPanel.add(verticalPanel);
		}

		add(horizontalPanel);
	}

	public void enable(boolean b) {
		for (Button button : buttons) {
			button.setEnabled(b);
		}
		buttonOk.setEnabled(b);
		buttonDelete.setEnabled(b);
		buttonClear.setEnabled(b);
	}

	private void clearButtons() {
		for (Button button : buttons) {
			button.setText("");
		}
	}

	public void onStrokeFinished() {
		Service.Util.getInstance().recognizeHandwriting(strokeCanvas.getStrokes(),
				callbackRecognizeHandwriting);
	}

	private final AsyncCallback<String[]> callbackRecognizeHandwriting = new AsyncCallback<String[]>() {
		public void onSuccess(String[] result) {
			clearButtons();

			for (int i = 0; i < NUMBER_OF_CANDIDATE; ++i) {
				if (result.length == i) {
					break;
				}
				buttons[i].setText(result[i]);
			}
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "文字認識結果の取得に失敗しました", caught);
		}
	};

	@Override
	public void onClick(ClickEvent event) {
		final Widget sender = (Widget) event.getSource();
		String lastAnswer = answerView.get();
		if (Arrays.asList(buttons).contains(sender)) {
			// 1文字選択
			final Button clickedButton = (Button) sender;
			final String letter = clickedButton.getText();
			if (letter.equals("")) {
				return;
			}
			answerView.set(lastAnswer + letter, false);
			strokeCanvas.clear();

			clearButtons();

		} else if (sender == buttonOk) {
			enable(false);
			sendAnswer(lastAnswer);

		} else if (sender == buttonDelete) {
			answerView
					.set(lastAnswer.isEmpty() ? "" : lastAnswer.substring(0, lastAnswer.length()),
							false);

		} else if (sender == buttonClear) {
			strokeCanvas.clear();
			clearButtons();
		}
	}
}
