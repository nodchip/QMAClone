package tv.dyndns.kishibe.qmaclone.client.setting;

import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.lobby.LobbyUi;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData.WebSocketUsage;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundSettings;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundSettingsStore;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class OtherUi extends Composite {

	private static OtherUiUiBinder uiBinder = GWT.create(OtherUiUiBinder.class);

	interface OtherUiUiBinder extends UiBinder<Widget, OtherUi> {
	}

	@UiField
	RadioButton radioButtonSeOn;
	@UiField
	RadioButton radioButtonSeOff;
	@UiField
	RadioButton radioButtonRankingMoveOn;
	@UiField
	RadioButton radioButtonRankingMoveOff;
	@UiField
	RadioButton radioButtonHideAnswerOn;
	@UiField
	RadioButton radioButtonHideAnswerOff;
	@UiField
	RadioButton radioButtonShowInfoOn;
	@UiField
	RadioButton radioButtonShowInfoOff;
	@UiField
	RadioButton radioButtonReflectEventResultOn;
	@UiField
	RadioButton radioButtonReflectEventResultOff;
	@UiField
	RadioButton radioButtonWebSocketDefault;
	@UiField
	RadioButton radioButtonWebSocketOn;
	@UiField
	RadioButton radioButtonWebSocketOff;
	@UiField
	RadioButton radioButtonRegisterCreatedProblemOn;
	@UiField
	RadioButton radioButtonRegisterCreatedProblemOff;
	@UiField
	RadioButton radioButtonRegisterIndicatedProblemOn;
	@UiField
	RadioButton radioButtonRegisterIndicatedProblemOff;
	@UiField
	ListBox listBoxSoundMaster;
	@UiField
	ListBox listBoxSoundUi;
	@UiField
	ListBox listBoxSoundGameplay;
	@UiField
	ListBox listBoxSoundResult;
	@UiField
	CheckBox checkBoxSoundMuted;

	public OtherUi() {
		initWidget(uiBinder.createAndBindUi(this));

		applyBooleanSetting(UserData.get().isPlaySound(), radioButtonSeOn, radioButtonSeOff);
		applyBooleanSetting(UserData.get().isRankingMove(), radioButtonRankingMoveOn,
				radioButtonRankingMoveOff);
		applyBooleanSetting(UserData.get().isHideAnswer(), radioButtonHideAnswerOn,
				radioButtonHideAnswerOff);
		applyBooleanSetting(UserData.get().isShowInfo(), radioButtonShowInfoOn,
				radioButtonShowInfoOff);
		applyBooleanSetting(UserData.get().isReflectEventResult(), radioButtonReflectEventResultOn,
				radioButtonReflectEventResultOff);
		applyBooleanSetting(UserData.get().isRegisterCreatedProblem(),
				radioButtonRegisterCreatedProblemOn, radioButtonRegisterCreatedProblemOff);
		applyBooleanSetting(UserData.get().isRegisterIndicatedProblem(),
				radioButtonRegisterIndicatedProblemOn, radioButtonRegisterIndicatedProblemOff);
		initSoundSettings();

		switch (UserData.get().getWebSocketUsage()) {
		case Default:
			radioButtonWebSocketDefault.setValue(true);
			break;
		case Off:
			radioButtonWebSocketOff.setValue(true);
			break;
		case On:
			radioButtonWebSocketOn.setValue(true);
			break;
		}
	}

	private void applyBooleanSetting(boolean value, HasValue<Boolean> whenTrue,
			HasValue<Boolean> whenFalse) {
		whenTrue.setValue(value);
		whenFalse.setValue(!value);
	}

	private void initSoundSettings() {
		initVolumeList(listBoxSoundMaster);
		initVolumeList(listBoxSoundUi);
		initVolumeList(listBoxSoundGameplay);
		initVolumeList(listBoxSoundResult);

		SoundSettings settings = SoundSettingsStore.loadFromLocalStorage();
		selectVolume(listBoxSoundMaster, settings.getMasterVolume());
		selectVolume(listBoxSoundUi, settings.getUiVolume());
		selectVolume(listBoxSoundGameplay, settings.getGameplayVolume());
		selectVolume(listBoxSoundResult, settings.getResultVolume());
		checkBoxSoundMuted.setValue(settings.isMuted());
	}

	private void initVolumeList(ListBox listBox) {
		listBox.clear();
		for (int i = 0; i <= 10; i++) {
			int percent = i * 10;
			listBox.addItem(percent + "%", Integer.toString(percent));
		}
	}

	private void selectVolume(ListBox listBox, double volume) {
		int percent = normalizeVolumePercent(volume);
		for (int i = 0; i < listBox.getItemCount(); i++) {
			if (Integer.toString(percent).equals(listBox.getValue(i))) {
				listBox.setSelectedIndex(i);
				return;
			}
		}
		listBox.setSelectedIndex(10);
	}

	private int normalizeVolumePercent(double volume) {
		int percent = (int) Math.round(volume * 100.0);
		percent = (percent + 5) / 10 * 10;
		if (percent < 0) {
			return 0;
		}
		if (100 < percent) {
			return 100;
		}
		return percent;
	}

	private double getSelectedVolume(ListBox listBox) {
		int selectedIndex = listBox.getSelectedIndex();
		if (selectedIndex < 0) {
			return 1.0;
		}
		return Integer.parseInt(listBox.getValue(selectedIndex)) / 100.0;
	}

	private SoundSettings collectSoundSettings() {
		return new SoundSettings(
				getSelectedVolume(listBoxSoundMaster),
				getSelectedVolume(listBoxSoundUi),
				getSelectedVolume(listBoxSoundGameplay),
				getSelectedVolume(listBoxSoundResult),
				checkBoxSoundMuted.getValue() != null && checkBoxSoundMuted.getValue(),
				SoundSettings.CURRENT_SCHEMA_VERSION);
	}

	private void saveSoundSettings(String itemName) {
		SoundSettingsStore.saveToLocalStorage(collectSoundSettings());
		saveSetting(itemName);
	}

	private void saveSetting(String itemName) {
		UserData.get().save();
		SettingSaveToast.showSaved(itemName);
	}

	@UiHandler("listBoxSoundMaster")
	void onListBoxSoundMaster(ChangeEvent e) {
		saveSoundSettings("効果音音量");
	}

	@UiHandler("listBoxSoundUi")
	void onListBoxSoundUi(ChangeEvent e) {
		saveSoundSettings("効果音音量");
	}

	@UiHandler("listBoxSoundGameplay")
	void onListBoxSoundGameplay(ChangeEvent e) {
		saveSoundSettings("効果音音量");
	}

	@UiHandler("listBoxSoundResult")
	void onListBoxSoundResult(ChangeEvent e) {
		saveSoundSettings("効果音音量");
	}

	@UiHandler("checkBoxSoundMuted")
	void onCheckBoxSoundMuted(ClickEvent e) {
		saveSoundSettings("効果音ミュート");
	}

	@UiHandler("radioButtonSeOn")
	void onRadioButtonSeOn(ClickEvent e) {
		UserData.get().setPlaySound(true);
		saveSetting("効果音");
	}

	@UiHandler("radioButtonSeOff")
	void onRadioButtonSeOff(ClickEvent e) {
		UserData.get().setPlaySound(false);
		saveSetting("効果音");
	}

	@UiHandler("radioButtonRankingMoveOn")
	void onRadioButtonRankingMoveOn(ClickEvent e) {
		UserData.get().setRankingMove(true);
		saveSetting("ランキング上下変動表示");
	}

	@UiHandler("radioButtonRankingMoveOff")
	void onRadioButtonRankingMoveOff(ClickEvent e) {
		UserData.get().setRankingMove(false);
		saveSetting("ランキング上下変動表示");
	}

	@UiHandler("radioButtonHideAnswerOn")
	void onRadioButtonHideAnswerOn(ClickEvent e) {
		UserData.get().setHideAnswer(true);
		saveSetting("解答の表示");
	}

	@UiHandler("radioButtonHideAnswerOff")
	void onRadioButtonHideAnswerOff(ClickEvent e) {
		UserData.get().setHideAnswer(false);
		saveSetting("解答の表示");
	}

	@UiHandler("radioButtonShowInfoOn")
	void onRadioButtonShowInfoOn(ClickEvent e) {
		UserData.get().setShowInfo(true);
		LobbyUi.getInstance().updateInfomationPanel();
		saveSetting("ログイン画面の情報表示");
	}

	@UiHandler("radioButtonShowInfoOff")
	void onRadioButtonShowInfoOff(ClickEvent e) {
		UserData.get().setShowInfo(false);
		LobbyUi.getInstance().updateInfomationPanel();
		saveSetting("ログイン画面の情報表示");
	}

	@UiHandler("radioButtonReflectEventResultOn")
	void onRadioButtonReflectEventResultOn(ClickEvent e) {
		UserData.get().setReflectEventResult(true);
		saveSetting("イベント戦成績の反映");
	}

	@UiHandler("radioButtonReflectEventResultOff")
	void onRadioButtonReflectEventResultOff(ClickEvent e) {
		UserData.get().setReflectEventResult(false);
		saveSetting("イベント戦成績の反映");
	}

	@UiHandler("radioButtonWebSocketDefault")
	void onRadioButtonWebSocketDefault(ClickEvent e) {
		UserData.get().setWebSocketUsage(WebSocketUsage.Default);
		saveSetting("WebSocket利用設定");
	}

	@UiHandler("radioButtonWebSocketOn")
	void onRadioButtonWebSocketOn(ClickEvent e) {
		UserData.get().setWebSocketUsage(WebSocketUsage.On);
		saveSetting("WebSocket利用設定");
	}

	@UiHandler("radioButtonWebSocketOff")
	void onRadioButtonWebSocketOff(ClickEvent e) {
		UserData.get().setWebSocketUsage(WebSocketUsage.Off);
		saveSetting("WebSocket利用設定");
	}

	@UiHandler("radioButtonRegisterCreatedProblemOn")
	void onRadioButtonRegisterCreatedProblemOn(ClickEvent e) {
		UserData.get().setRegisterCreatedProblem(true);
		saveSetting("問題作成時の自動登録");
	}

	@UiHandler("radioButtonRegisterCreatedProblemOff")
	void onRadioButtonRegisterCreatedProblemOff(ClickEvent e) {
		UserData.get().setRegisterCreatedProblem(false);
		saveSetting("問題作成時の自動登録");
	}

	@UiHandler("radioButtonRegisterIndicatedProblemOn")
	void onRadioButtonRegisterIndicatedProblemOn(ClickEvent e) {
		UserData.get().setRegisterIndicatedProblem(true);
		saveSetting("問題指摘時の自動登録");
	}

	@UiHandler("radioButtonRegisterIndicatedProblemOff")
	void onRadioButtonRegisterIndicatedProblemOff(ClickEvent e) {
		UserData.get().setRegisterIndicatedProblem(false);
		saveSetting("問題指摘時の自動登録");
	}
}
