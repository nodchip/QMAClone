package tv.dyndns.kishibe.qmaclone.client.game.input;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.ButtonLetter;
import tv.dyndns.kishibe.qmaclone.client.game.LetterType;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.util.StringUtils;
import tv.dyndns.kishibe.qmaclone.client.util.StringUtils.VoicedSoundMark;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

public class InputWidgetTyping50 extends InputWidget implements ClickHandler {
	private static final String BUTTON_LETTER[] = {
			"あかさたなはまやらわぁゃいきしちにひみ り ぃゅうくすつぬふむゆるをぅょえけせてねへめ れ ぇっおこそとのほもよろんぉー",
			"アカサタナハマヤラワァャイキシチニヒミ リ ィュウクスツヌフムユルヲゥョエケセテネヘメ レ ェッオコソトノホモヨロンォー",
			"１２３４５６７８９０ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ" };
	private static final int[] NUMBER_OF_LETTERS_PER_ROW = { 12, 12, 10 };

	private final Grid gridLetter;
	private ButtonLetter[] buttons;
	// 消す・濁点・半濁点・OK
	private final Button buttonDakuten = new Button("゛", this);
	private final Button buttonHandakuten = new Button("゜", this);
	private final Button buttonDelete = new Button("消す", this);
	private final Button buttonOK = new Button("ＯＫ", this);

	public InputWidgetTyping50(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, LetterType letterType, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);

		if (letterType == null) {
			gridLetter = null;
			return;
		}
		String letters = BUTTON_LETTER[letterType.ordinal()];

		int numberOfLettersPerRow = NUMBER_OF_LETTERS_PER_ROW[letterType.ordinal()];
		gridLetter = new Grid(5, numberOfLettersPerRow + 1);
		gridLetter.setBorderWidth(0);
		gridLetter.setCellPadding(0);
		gridLetter.setCellSpacing(0);

		buttons = new ButtonLetter[numberOfLettersPerRow * 5];

		for (int row = 0; row < 5; ++row) {
			for (int column = 0; column < numberOfLettersPerRow; ++column) {
				int index = row * numberOfLettersPerRow + column;

				if (letters.length() <= index) {
					break;
				}

				String letter = letters.substring(index, index + 1);

				if (letter.equals(" ")) {
					continue;
				}

				ButtonLetter buttonLetter = new ButtonLetter(letter, answerView);
				buttons[index] = buttonLetter;
				gridLetter.setWidget(row, column, buttonLetter);
			}
		}

		buttonDakuten.setStyleName("gwt-Button-typing");
		buttonHandakuten.setStyleName("gwt-Button-typing");
		buttonDelete.setStyleName("gwt-Button-typingControl");
		buttonOK.setStyleName("gwt-Button-typingControl");

		buttonDelete.setWidth("90px");
		buttonOK.setWidth("90px");

		int mostLeftColumIndex = gridLetter.getColumnCount() - 1;
		if (letterType != LetterType.Alphabet) {
			gridLetter.setWidget(0, mostLeftColumIndex, buttonDakuten);
			gridLetter.setWidget(1, mostLeftColumIndex, buttonHandakuten);
		}
		gridLetter.setWidget(3, mostLeftColumIndex, buttonDelete);
		gridLetter.setWidget(4, mostLeftColumIndex, buttonOK);

		add(gridLetter);
	}

	public void enable(boolean enabled) {
		for (int i = 0; i < buttons.length; ++i) {
			Button button = buttons[i];
			if (button != null) {
				button.setEnabled(enabled);
			}
		}

		buttonDakuten.setEnabled(enabled);
		buttonHandakuten.setEnabled(enabled);
		buttonDelete.setEnabled(enabled);
		buttonOK.setEnabled(enabled);
	}

	@Override
	public void onClick(ClickEvent event) {
		Widget sender = (Widget) event.getSource();
		if (sender == buttonDakuten) {
			// 濁点ボタン
			playSound(Constant.SOUND_URL_BUTTON_PUSH);
			answerView.set(StringUtils.switchVoicedSoundMarkOfLastLetter(answerView.get(),
					VoicedSoundMark.Full), true);

		} else if (sender == buttonHandakuten) {
			// 半濁点ボタン
			playSound(Constant.SOUND_URL_BUTTON_PUSH);
			answerView.set(StringUtils.switchVoicedSoundMarkOfLastLetter(answerView.get(),
					VoicedSoundMark.Half), true);

		} else if (sender == buttonDelete) {
			// 消すボタン
			answerView.set(StringUtils.removeLast(answerView.get()), true);

		} else if (sender == buttonOK) {
			// OKボタン
			enable(false);
			sendAnswer(answerView.get());
		}
	}
}
