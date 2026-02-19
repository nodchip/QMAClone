package tv.dyndns.kishibe.qmaclone.client.game;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * 解答表示欄を「固定個数の枠（スロット）」として表示する AnswerView 実装。
 * <p>
 * 入力文字列は従来どおり保持しつつ、表示は slotCount 分だけ左から埋める。
 */
public class AnswerViewSlots extends Composite implements AnswerView {
	private static final String SYSTEM_ANSWERED = "(解答済)";
	private static final String SYSTEM_TIME_UP = "(時間切れ)";
	private static final String SYSTEM_NO_ANSWER = "・・・・・・・・";

	private final int maxLength;
	private final int temporaryMaxLength;
	private final int slotCount;

	private final List<Label> slots = new ArrayList<Label>();
	private final Label badge = new Label();
	private String letters = "";

	public AnswerViewSlots(int maxLength, int temporaryMaxLength, int slotCount) {
		this.maxLength = maxLength;
		this.temporaryMaxLength = temporaryMaxLength;
		this.slotCount = slotCount;

		FlowPanel root = new FlowPanel();
		// 既存のレイアウト/余白調整を流用するため、従来の解答欄クラスを維持する。
		root.setStyleName("gwt-Label-answer");
		root.addStyleName("answerSlotsRoot");

		badge.setStyleName("answerSlotsBadge");
		badge.setVisible(false);
		root.add(badge);

		FlowPanel slotsPanel = new FlowPanel();
		slotsPanel.setStyleName("answerSlots");
		root.add(slotsPanel);

		for (int i = 0; i < slotCount; i++) {
			Label slot = new Label();
			slot.setStyleName("answerSlot");
			slots.add(slot);
			slotsPanel.add(slot);
		}

		initWidget(root);
		updateDisplay();
	}

	public AnswerViewSlots(int maxLength, int temporaryMaxLength) {
		this(maxLength, temporaryMaxLength, 8);
	}

	public AnswerViewSlots(int maxLength) {
		this(maxLength, maxLength, 8);
	}

	private void updateDisplay() {
		// システム表示中も枠自体は見せ続ける（枠の中に文言は出さない）。
		for (int i = 0; i < slots.size(); i++) {
			Label slot = slots.get(i);
			slot.removeStyleDependentName("filled");
			slot.removeStyleDependentName("empty");

			if (i < letters.length()) {
				slot.setText(String.valueOf(letters.charAt(i)));
				slot.addStyleDependentName("filled");
			} else {
				// 空の枠でも高さが崩れないようにする（通常スペースはHTMLで畳まれる可能性がある）
				slot.setText("\u00A0");
				slot.addStyleDependentName("empty");
			}
		}
	}

	private boolean isSystemMessage(String s) {
		return SYSTEM_ANSWERED.equals(s) || SYSTEM_TIME_UP.equals(s) || SYSTEM_NO_ANSWER.equals(s);
	}

	@Override
	public void set(String s, boolean fillWithFrame) {
		// fillWithFrame は「枠を表示するか」の意図だが、このビューは常に枠を表示する。
		if (isSystemMessage(s)) {
			// 文字列を枠内に出さず、オーバーレイ表示に切り替える。
			badge.setText(s);
			badge.setVisible(true);
			return;
		}

		badge.setVisible(false);
		if (s.length() > temporaryMaxLength) {
			s = s.substring(0, temporaryMaxLength);
		}
		// 表示は枠数分に切る（内部値は従来どおり temporaryMaxLength まで保持する）
		letters = s;
		if (letters.length() > slotCount) {
			letters = letters.substring(0, slotCount);
		}
		updateDisplay();
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
