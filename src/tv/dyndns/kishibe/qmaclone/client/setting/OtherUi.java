package tv.dyndns.kishibe.qmaclone.client.setting;

import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.lobby.LobbyUi;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData.WebSocketUsage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
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

	@UiHandler("radioButtonSeOn")
	void onRadioButtonSeOn(ClickEvent e) {
		UserData.get().setPlaySound(true);
		UserData.get().save();
	}

	@UiHandler("radioButtonSeOff")
	void onRadioButtonSeOff(ClickEvent e) {
		UserData.get().setPlaySound(false);
		UserData.get().save();
	}

	@UiHandler("radioButtonRankingMoveOn")
	void onRadioButtonRankingMoveOn(ClickEvent e) {
		UserData.get().setRankingMove(true);
		UserData.get().save();
	}

	@UiHandler("radioButtonRankingMoveOff")
	void onRadioButtonRankingMoveOff(ClickEvent e) {
		UserData.get().setRankingMove(false);
		UserData.get().save();
	}

	@UiHandler("radioButtonHideAnswerOn")
	void onRadioButtonHideAnswerOn(ClickEvent e) {
		UserData.get().setHideAnswer(true);
		UserData.get().save();
	}

	@UiHandler("radioButtonHideAnswerOff")
	void onRadioButtonHideAnswerOff(ClickEvent e) {
		UserData.get().setHideAnswer(false);
		UserData.get().save();
	}

	@UiHandler("radioButtonShowInfoOn")
	void onRadioButtonShowInfoOn(ClickEvent e) {
		UserData.get().setShowInfo(true);
		LobbyUi.getInstance().updateInfomationPanel();
		UserData.get().save();
	}

	@UiHandler("radioButtonShowInfoOff")
	void onRadioButtonShowInfoOff(ClickEvent e) {
		UserData.get().setShowInfo(false);
		LobbyUi.getInstance().updateInfomationPanel();
		UserData.get().save();
	}

	@UiHandler("radioButtonReflectEventResultOn")
	void onRadioButtonReflectEventResultOn(ClickEvent e) {
		UserData.get().setReflectEventResult(true);
		UserData.get().save();
	}

	@UiHandler("radioButtonReflectEventResultOff")
	void onRadioButtonReflectEventResultOff(ClickEvent e) {
		UserData.get().setReflectEventResult(false);
		UserData.get().save();
	}

	@UiHandler("radioButtonWebSocketDefault")
	void onRadioButtonWebSocketDefault(ClickEvent e) {
		UserData.get().setWebSocketUsage(WebSocketUsage.Default);
		UserData.get().save();
	}

	@UiHandler("radioButtonWebSocketOn")
	void onRadioButtonWebSocketOn(ClickEvent e) {
		UserData.get().setWebSocketUsage(WebSocketUsage.On);
		UserData.get().save();
	}

	@UiHandler("radioButtonWebSocketOff")
	void onRadioButtonWebSocketOff(ClickEvent e) {
		UserData.get().setWebSocketUsage(WebSocketUsage.Off);
		UserData.get().save();
	}

	@UiHandler("radioButtonRegisterCreatedProblemOn")
	void onRadioButtonRegisterCreatedProblemOn(ClickEvent e) {
		UserData.get().setRegisterCreatedProblem(true);
		UserData.get().save();
	}

	@UiHandler("radioButtonRegisterCreatedProblemOff")
	void onRadioButtonRegisterCreatedProblemOff(ClickEvent e) {
		UserData.get().setRegisterCreatedProblem(false);
		UserData.get().save();
	}

	@UiHandler("radioButtonRegisterIndicatedProblemOn")
	void onRadioButtonRegisterIndicatedProblemOn(ClickEvent e) {
		UserData.get().setRegisterIndicatedProblem(true);
		UserData.get().save();
	}

	@UiHandler("radioButtonRegisterIndicatedProblemOff")
	void onRadioButtonRegisterIndicatedProblemOff(ClickEvent e) {
		UserData.get().setRegisterIndicatedProblem(false);
		UserData.get().save();
	}
}
