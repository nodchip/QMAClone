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
package tv.dyndns.kishibe.qmaclone.client.game.left;

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.click.MarkedCanvas;
import tv.dyndns.kishibe.qmaclone.client.game.input.InputWidgetClick;
import tv.dyndns.kishibe.qmaclone.client.game.left.AnswerPopup.Style;
import tv.dyndns.kishibe.qmaclone.client.geom.Point;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Strings;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

public class WidgetPlayer extends HorizontalPanel {
	private static final Logger logger = Logger.getLogger(WidgetPlayer.class.getName());
	private static final int INTERPOLATE_RATIO_NUMERATOR = 1;
	private static final int INTERPOLATE_RATIO_DENOMINATOR = 10;
	private static final int OFFSET_X = 5;
	public static final int HEIGHT = 65;
	private final HTML htmlName = new HTML();
	private String answer;
	protected PacketProblem problem;
	private boolean flagRecieved = false;
	private boolean flagOpen = false;
	private boolean flagTimeUp = false;
	private boolean isCorrect;
	private int currentX;
	private int currentY;
	private int destX;
	private int destY;
	private final AnswerPopupFactory answerPopupFactory;
	private AnswerPopup answerPopup = null;
	private final WidgetPlayerList parentPanel;

	public WidgetPlayer(PacketPlayerSummary playerSummary, String imageFileName,
			WidgetPlayerList parentPanel, int rank) {
		destX = currentX = OFFSET_X;
		destY = currentY = HEIGHT * rank;
		this.parentPanel = parentPanel;
		answerPopupFactory = new AnswerPopupFactory(parentPanel);

		setPixelSize(180, 50);
		setVerticalAlignment(ALIGN_MIDDLE);

		Image image = new Image(Constant.ICON_URL_PREFIX + imageFileName);
		image.setPixelSize(Constant.ICON_SIZE, Constant.ICON_SIZE);
		add(image);
		setCellWidth(image, "50px");

		setPlayerSummary(playerSummary);
		htmlName.addStyleDependentName("playerName");
		add(htmlName);

		update();
	}

	public void setProblem(PacketProblem problem) {
		this.problem = problem;
		answerPopup = answerPopupFactory.get(problem);
		answerPopup.setPosition(currentX, currentY);
	}

	public void setPlayerSummary(PacketPlayerSummary player) {
		htmlName.setHTML(player.asGameSafeHtml());
	}

	public void recieveAnswer(String answer) {
		if (Strings.isNullOrEmpty(answer)) {
			this.answer = AnswerPopup.LABEL_NO_ANSWER;
		} else {
			this.answer = answer;
		}

		flagRecieved = true;

		update();
	}

	public void open() {
		flagOpen = true;

		update();
	}

	public void recieveTimeUp() {
		flagTimeUp = true;

		update();
	}

	public void clearAnswer() {
		flagRecieved = false;
		flagOpen = false;
		flagTimeUp = false;
		this.answer = "";

		if (answerPopup != null) {
			answerPopup.hide();
		}

		markSegmentId = MarkedCanvas.REGISTER;
		markPointerId = MarkedCanvas.REGISTER;
		markYesId = MarkedCanvas.REGISTER;
		markNoId = MarkedCanvas.REGISTER;

		update();
	}

	public void update() {
		if (answerPopup == null) {
			return;
		}

		if (flagTimeUp) {
			isCorrect = flagRecieved && problem.isCorrect(this.answer);
			answerPopup.setStyle(isCorrect ? Style.Correct : Style.Wrong);
			answerPopup.show(flagRecieved ? this.answer : AnswerPopup.LABEL_TIME_UP);

		} else if (flagRecieved) {
			answerPopup.setStyle(Style.Answered);
			answerPopup.show(flagOpen ? this.answer : AnswerPopup.LABEL_ANSWERED);

		} else {
			answerPopup.hide();
		}

		if (problem.type == ProblemType.Click) {
			updateForClickQuiz();
		}
	}

	// 以下画像クリッククイズのための特殊処理
	private int markSegmentId = MarkedCanvas.REGISTER;
	private int markPointerId = MarkedCanvas.REGISTER;
	private int markYesId = MarkedCanvas.REGISTER;
	private int markNoId = MarkedCanvas.REGISTER;

	private void updateForClickQuiz() {
		if (!flagRecieved) {
			return;
		}

		Point answerPosition = getAnswerPosition();
		if (answerPosition == null || !answerPosition.isValid()) {
			return;
		}

		Point canvasOffset = InputWidgetClick.getCanvasOffset();
		if (canvasOffset == null) {
			return;
		}

		Point offset = parentPanel.getOffset();
		int offsetX = canvasOffset.x - offset.x;
		int offsetY = canvasOffset.y - offset.y;

		int x = answerPosition.x + offsetX;
		int y = answerPosition.y + offsetY;

		// 先にensureCanvas()してしまうと解答欄に回答表示用のパネルがかぶってしまい、
		// 回答できなくなってしまう
		if (flagTimeUp) {
			MarkedCanvas canvas = parentPanel.ensureCanvas();
			canvas.removeMark(markPointerId);
			if (problem.isCorrect(this.answer)) {
				markYesId = canvas.addYesMark(x, y, markYesId);
			} else {
				markNoId = canvas.addNoMark(x, y, markNoId);
			}

		} else if (flagOpen) {
			MarkedCanvas canvas = parentPanel.ensureCanvas();
			markPointerId = canvas.addPointerMark(x, y, markPointerId);
		}

		updatePositionClilck();
	}

	public int getCurrentX() {
		return currentX;
	}

	public int getCurrentY() {
		return currentY;
	}

	public void setRank(int rank) {
		destY = (rank - 1) * HEIGHT;
	}

	private int interpolate(int src, int dest, int ratioNumerator, int ratioDenominator) {
		return (src * (ratioDenominator - ratioNumerator) + dest * ratioNumerator)
				/ ratioDenominator;
	}

	public void updatePosition() {
		int newX = interpolate(currentX, destX, INTERPOLATE_RATIO_NUMERATOR,
				INTERPOLATE_RATIO_DENOMINATOR);
		int newY = interpolate(currentY, destY, INTERPOLATE_RATIO_NUMERATOR,
				INTERPOLATE_RATIO_DENOMINATOR);

		if (newX != currentX || newY != currentY) {
			currentX = newX;
			currentY = newY;

			// 以下の部分でまれにエラーが起きるためエラー補足
			try {
				parentPanel.setWidgetPosition(this, currentX, currentY);
			} catch (Exception e) {
				logger.log(Level.WARNING, "プレイヤー表示位置の更新中にエラーが発生しました", e);
			}

			if (answerPopup != null) {
				answerPopup.setPosition(currentX, currentY);
			}
		}

		if (problem != null && problem.type == ProblemType.Click) {
			updatePositionClilck();
		}
	}

	private void updatePositionClilck() {
		if (!flagRecieved || !flagOpen) {
			return;
		}

		Point answerPosition = getAnswerPosition();
		if (answerPosition == null || !answerPosition.isValid()) {
			return;
		}

		Point canvasOffset = InputWidgetClick.getCanvasOffset();
		if (canvasOffset == null) {
			return;
		}

		Point parentOffset = parentPanel.getOffset();
		int offsetX = canvasOffset.x - parentOffset.x;
		int offsetY = canvasOffset.y - parentOffset.y;

		Point segmentBeginPosition = getSegmentBeginPosition();
		int startX = segmentBeginPosition.x - parentOffset.x;
		int startY = segmentBeginPosition.y - parentOffset.y;
		int endX = answerPosition.x + offsetX;
		int endY = answerPosition.y + offsetY;

		MarkedCanvas canvas = parentPanel.ensureCanvas();
		markSegmentId = canvas.addSegmentMark(startX, startY, endX, endY, markSegmentId);
	}

	private Point getSegmentBeginPosition() {
		int x = getAbsoluteLeft() + getOffsetWidth() / 2;
		int y = getAbsoluteTop() + getOffsetHeight() / 2;
		return new Point(x, y);
	}

	private Point getAnswerPosition() {
		return Point.fromString(this.answer);
	}
}
