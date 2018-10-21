package tv.dyndns.kishibe.qmaclone.client.game;

import com.google.gwt.user.client.ui.Label;

public class AnswerViewImpl extends Label implements AnswerView {
	private final int maxLength;
	private final int temporaryMaxLength;
	private String letters = "";

	/**
	 * 回答表示ウィジットのコンストラクタ
	 * 
	 * @param maxLength
	 *            解答として送信される文字列の最大長
	 * @param temporaryMaxLength
	 *            画面に一時的に表示される文字列の最大長。ローマ字入力にて使用する
	 * @param fillWithFrame
	 *            空文字の枠を表示する場合は {@code true}
	 */
	public AnswerViewImpl(int maxLength, int temporaryMaxLength, boolean fillWithFrame) {
		this.maxLength = maxLength;
		this.temporaryMaxLength = temporaryMaxLength;
		addStyleDependentName("answer");
		updateDisplay(fillWithFrame);
	}

	public AnswerViewImpl(int maxLength, int temporaryMaxLength) {
		this(maxLength, temporaryMaxLength, false);
	}

	public AnswerViewImpl(int maxLength) {
		this(maxLength, maxLength, false);
	}

	private void updateDisplay(boolean fillWithFrame) {
		StringBuilder sb = new StringBuilder(letters);
		if (fillWithFrame) {
			// 回答の最大文字列に達するまで□で埋める
			while (sb.length() < maxLength) {
				sb.append('□');
			}
		}
		setText(sb.toString());
	}

	@Override
	public void set(String s, boolean fillWithFrame) {
		if (s.length() > temporaryMaxLength) {
			s = s.substring(0, temporaryMaxLength);
		}
		letters = s;
		updateDisplay(fillWithFrame);
	}

	@Override
	public String get() {
		return letters.length() <= maxLength ? letters : letters.substring(0, maxLength);
	}

	@Override
	public String getRaw() {
		return letters;
	}
}
