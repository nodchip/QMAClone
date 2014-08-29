package tv.dyndns.kishibe.qmaclone.client.game.input;

import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.GlobalKeyEventHandler;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.LetterType;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class InputWidgetTypingQwerty extends InputWidget implements ClickHandler {
	private static final String STYLE_NAME_LETTER = "gwt-Button-typing";
	private static final String STYLE_NAME_CONTROL = "gwt-Button-typingControl";
	private static final String[] LETTERS = { "１２３４５６７８９０-", "ＱＷＥＲＴＹＵＩＯＰ", "ＡＳＤＦＧＨＪＫＬ", "ＺＸＣＶＢＮＭ" };
	private static final String ACCEPTED_KEYS = "1234567890-QWERTYUIOPASDFGHJKLZXCVBNM";
	private final LetterType letterType;
	private final Set<Button> buttons = Sets.newHashSet();
	private final Button buttonDelete = new Button("消す", this);
	private final Button buttonOk = new Button("OK", this);
	private HandlerRegistration globalKeyHandlerRegistration;

	public InputWidgetTypingQwerty(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, LetterType letterType, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);
		this.letterType = letterType;

		setHorizontalAlignment(ALIGN_LEFT);
		for (int row = 0; row < LETTERS.length; ++row) {
			HorizontalPanel panel = new HorizontalPanel();

			HTML space = new HTML();
			space.setPixelSize(row * 20, 1);
			panel.add(space);

			String letters = LETTERS[row];
			for (int column = 0; column < letters.length(); ++column) {
				Button button = new Button(letters.substring(column, column + 1), this);
				button.setStyleName(STYLE_NAME_LETTER);
				panel.add(button);
				buttons.add(button);
			}

			if (row + 1 == LETTERS.length) {
				panel.add(buttonDelete);
				panel.add(buttonOk);
				buttonDelete.setStyleName(STYLE_NAME_CONTROL);
				buttonOk.setStyleName(STYLE_NAME_CONTROL);
			}

			add(panel);
		}
	}

	@Override
	public void enable(boolean b) {
		for (Button button : buttons) {
			button.setEnabled(b);
		}
		buttonDelete.setEnabled(b);
		buttonOk.setEnabled(b);
	}

	@Override
	public void onClick(ClickEvent event) {
		Button source = (Button) event.getSource();
		if (buttons.contains(source)) {
			// 文字ボタン
			String letter = source.getText();
			addLetterToAnswer(letter);

		} else if (source == buttonDelete) {
			// 消すボタン
			onErase();

		} else if (source == buttonOk) {
			// OKボタン
			onOk();

		} else {
			throw new AssertionError();
		}
	}

	private void onKey(char ch) {
		if (!buttonOk.isEnabled()) {
			return;
		}

		System.out.println("key='" + ch + "' (" + (int) ch + ")");
		System.out.println("enter=(" + (int) '\n' + ")");
		System.out.println("bs=(" + (int) '\b' + ")");

		// Enter == OKボタン
		if (ch == '\n' || ch == '\r') {
			// 1問につき複数回解答が送信できてしまうバグへの対処
			if (buttonOk.isEnabled()) {
				onOk();
			}
			return;
		}

		// Back Space == 消すボタン
		if (ch == '\b') {
			onErase();
			return;
		}

		String letter = "" + ch;
		letter = letter.toUpperCase();
		if (!ACCEPTED_KEYS.contains(letter)) {
			return;
		}
		letter = StringUtils.toFullWidth(letter);

		addLetterToAnswer(letter);
	}

	private void onErase() {
		String answer = answerView.get();
		answer = StringUtils.removeLast(answer);
		answerView.set(answer, true);
	}

	private void onOk() {
		enable(false);
		String answer = answerView.get();
		if (letterType != LetterType.Alphabet) {
			answer = StringUtils.convertLastAlphabetToKanaBeforeSendAnswer(answer,
					letterType == LetterType.Hiragana ? LetterType.Hiragana : LetterType.Katakana);
		}
		answerView.set(answer, true);
		sendAnswer(answer);
	}

	@VisibleForTesting
	void addLetterToAnswer(String letter) {
		String answer = answerView.getRaw();
		answer += letter;
		switch (letterType) {
		case Alphabet:
			break;
		case Hiragana:
			answer = StringUtils.convertLastAlphabetToKana(answer, LetterType.Hiragana);
			break;
		case Katakana:
			answer = StringUtils.convertLastAlphabetToKana(answer, LetterType.Katakana);
			break;
		}
		answerView.set(answer, true);
	}

	@Override
	protected void onLoad() {
		super.onLoad();

		globalKeyHandlerRegistration = Event.addNativePreviewHandler(new GlobalKeyEventHandler() {
			@Override
			protected void onKeyPress(char ch) {
				onKey(ch);
			}
		});
	}

	@Override
	protected void onUnload() {
		if (globalKeyHandlerRegistration != null) {
			globalKeyHandlerRegistration.removeHandler();
			globalKeyHandlerRegistration = null;
		}

		super.onUnload();
	}
}
