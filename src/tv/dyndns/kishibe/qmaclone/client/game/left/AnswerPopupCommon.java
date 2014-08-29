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

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;

public class AnswerPopupCommon implements AnswerPopup {
	private static final Logger logger = Logger.getLogger(AnswerPopupCommon.class.getName());
	private static final String COMMON_STYLE_NAME = "answerPopup";
	private static final String[][] LETTERS = { { "1", "2", "3", "4" }, { "A", "B", "C", "D" } };
	private static final int OFFSET_X = 4;
	private static final int OFFSET_Y = 16;
	private final Label label = new Label();
	private final AbsolutePanel absolutePanel;
	private int x;
	private int y;
	private Style currentStyle = Style.Normal;

	public AnswerPopupCommon(AbsolutePanel absolutePanel) {
		this.absolutePanel = absolutePanel;
		label.addStyleDependentName(COMMON_STYLE_NAME);
		label.addStyleName(Style.Normal.getStyleName());
	}

	@Override
	public void setStyle(Style style) {
		if (currentStyle == style) {
			return;
		}

		label.removeStyleDependentName(currentStyle.getStyleName());
		label.addStyleDependentName(style.getStyleName());
		currentStyle = style;
	}

	@Override
	public void setPosition(int x, int y) {
		try {
			this.x = x + OFFSET_X;
			this.y = y + OFFSET_Y;
			if (label.isAttached()) {
				absolutePanel.setWidgetPosition(label, this.x, this.y);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "解答表示中にエラーが発生しました", e);
		}
	}

	@Override
	public void show(String s) {
		label.setText(s);
		// まれに以下の部分でエラーが発生するため例外補足
		try {
			absolutePanel.add(label, x, y);
		} catch (Exception e) {
			logger.log(Level.WARNING, "解答表示中にエラーが発生しました", e);
		}
	}

	protected boolean isSystemMessage(String s) {
		return s.equals(LABEL_ANSWERED) || s.equals(LABEL_TIME_UP) || s.equals(LABEL_NO_ANSWER);
	}

	@Override
	public void hide() {
		// まれに以下の部分でエラーが発生するため例外補足
		try {
			absolutePanel.remove(label);
		} catch (Exception e) {
			logger.log(Level.WARNING, "解答表示の更新中にエラーが発生しました", e);
		}
	}

	protected String getLetter(ChoiceMarkType letterType, int letterIndex) {
		if (letterIndex < 0 || LETTERS[letterType.getIndex()].length <= letterIndex) {
			return "";
		}

		return LETTERS[letterType.getIndex()][letterIndex];
	}
}
