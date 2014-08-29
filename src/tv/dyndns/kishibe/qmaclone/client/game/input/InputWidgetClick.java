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
import tv.dyndns.kishibe.qmaclone.client.game.click.MarkedCanvas;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.geom.Point;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

public class InputWidgetClick extends InputWidget implements MouseDownHandler, ClickHandler {
	private static InputWidgetClick INSTANCE = null;
	private boolean throughClick = false;
	private MarkedCanvas canvas;
	private final Button buttonOk = new Button("OK", this);
	private Point clickPosition;
	private int circleMarkId = MarkedCanvas.REGISTER;
	private final Image image;

	/**
	 * 画像の左上の座標を返す。
	 * 
	 * @return　画像。画像が表示されていない場合はnull。
	 */
	public static Point getCanvasOffset() {
		if (INSTANCE == null || INSTANCE.image == null) {
			return null;
		}
		return new Point(INSTANCE.image.getAbsoluteLeft(), INSTANCE.image.getAbsoluteTop());
	}

	public InputWidgetClick(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);

		HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlignment(ALIGN_MIDDLE);

		image = new Image(problem.getClickImageUrl());
		image.setPixelSize(Constant.CLICK_IMAGE_WIDTH, Constant.CLICK_IMAGE_HEIGHT);
		panel.add(image);

		buttonOk.setStyleName("gwt-Button-clickControl");
		panel.add(buttonOk);

		canvas = new MarkedCanvas(image, Constant.CLICK_IMAGE_WIDTH, Constant.CLICK_IMAGE_HEIGHT);
		canvas.addMouseDownHandler(this);

		add(panel);
	}

	public void enable(boolean b) {
		throughClick = !b;
		buttonOk.setEnabled(b);
	}

	public Point getImageOffset() {
		int x = image.getAbsoluteLeft();
		int y = image.getAbsoluteTop();
		return new Point(x, y);
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		if (throughClick) {
			return;
		}

		int x = event.getX();
		int y = event.getY();
		clickPosition = new Point(x, y);
		if (canvas != null) {
			circleMarkId = canvas.addPointerMark(x, y, circleMarkId);
			canvas.update();
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		if (sender == buttonOk) {
			sendAnswer(clickPosition != null ? (clickPosition.x + " " + clickPosition.y) : "");
		}
	}

	@Override
	protected void hideAnswer() {
		super.hideAnswer();
		// 回答送信後はWidgetPlayerList側が表示を行うためMarkedCanvasのインストアンスを消す
		if (canvas != null) {
			canvas.hide();
			canvas = null;
		}
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		// キャンバス表示位置を確定させるためにshow(）を呼ぶ。
		if (canvas != null) {
			canvas.show();
		}
		INSTANCE = this;
	}

	@Override
	protected void onUnload() {
		INSTANCE = null;

		// BugTrack-QMAClone/575 - QMAClone wiki
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F575
		if (canvas != null) {
			canvas.hide();
			canvas = null;
		}

		super.onUnload();
	}
}
