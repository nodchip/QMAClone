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

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.LetterType;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;

public class InputWidgetTyping extends InputWidget implements ClickHandler {
	private static final Logger logger = Logger.getLogger(InputWidgetTyping.class.getName());
	@VisibleForTesting
	final SimplePanel panel = new SimplePanel();
	private final LetterType letterType;
	private final Button buttonChange = new Button("50音/QWERTY切り替え", this);
	private final InputWidgetTyping50 typing50;
	private final InputWidgetTypingQwerty typingQwerty;

	public InputWidgetTyping(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);
		letterType = judgeLetterType(problem.shuffledAnswers[0]);
		typing50 = new InputWidgetTyping50(problem, answerView, questionPanel, letterType,
				sessionData);
		typingQwerty = new InputWidgetTypingQwerty(problem, answerView, questionPanel, letterType,
				sessionData);

		if (letterType == LetterType.Hiragana && UserData.get().isQwertyHiragana()
				|| letterType == LetterType.Katakana && UserData.get().isQwertyKatakana()
				|| letterType == LetterType.Alphabet && UserData.get().isQwertyAlphabet()) {
			panel.setWidget(typingQwerty);
		} else {
			panel.setWidget(typing50);
		}

		add(panel);
		add(new HTML(new SafeHtmlBuilder().appendEscapedLines(
				"ボタンを押して" + letterType.toString() + "を入力し\n最後にOKを押してください").toSafeHtml()));
		add(buttonChange);
	}

	private final static String VALID_LETTER[] = {
			"あかさたなはまやらわいきしちにひみりをうくすつぬふむゆるんえけせてねへめれおこそとのほもよろーがぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽぁぃぅぇぉゃゅょっ",
			"アカサタナハマヤラワイキシチニヒミリヲウクスツヌフムユルンエケセテネヘメレオコソトノホモヨローガギグゲゴザジズゼゾダヂヅデドバビブベボパピプペポァィゥェォャュョッヴ",
			"１２３４５６７８９０ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ" };

	private LetterType judgeLetterType(String answer) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(answer));

		int[] counter = new int[LetterType.values().length];
		for (char ch : answer.toCharArray()) {
			for (LetterType type : LetterType.values()) {
				if (VALID_LETTER[type.ordinal()].indexOf(ch) != -1) {
					++counter[type.ordinal()];
				}
			}
		}

		for (LetterType type : LetterType.values()) {
			if (counter[type.ordinal()] == answer.length()) {
				return type;
			}
		}

		logger.log(Level.SEVERE, "解答の文字種別判定に失敗しました");
		return null;
	}

	@Override
	public void onClick(ClickEvent event) {
		if (event.getSource() == buttonChange) {
			onChangeButton();
		}
	}

	@VisibleForTesting
	void onChangeButton() {
		if (panel.getWidget() == typing50) {
			panel.setWidget(typingQwerty);
		} else {
			panel.setWidget(typing50);
		}

		InputWidget inputWidget = (InputWidget) panel.getWidget();

		switch (letterType) {
		case Hiragana:
			UserData.get().setQwertyHiragana(inputWidget == typingQwerty);
			break;
		case Katakana:
			UserData.get().setQwertyKatakana(inputWidget == typingQwerty);
			break;
		case Alphabet:
			UserData.get().setQwertyAlphabet(inputWidget == typingQwerty);
			break;
		}
		UserData.get().save();
	}

	@Override
	public void enable(boolean b) {
		InputWidget inputWidget = (InputWidget) panel.getWidget();
		inputWidget.hideAnswer();
		inputWidget.enable(b);
		buttonChange.setEnabled(b);
	}
}
