package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PanelSettingUserCodeView extends Composite implements
		PanelSettingUserCodePresenter.View {

	public interface MyTemplate extends SafeHtmlTemplates {
		@Template("<img src='{0}' width='32px' height='32px'/>{1} {2}")
		SafeHtml image(SafeUri imageUrl, int userCode, String name);
	}

	interface PanelSettingUserCodeViewUiBinder extends UiBinder<Widget, PanelSettingUserCodeView> {
	}

	private static final MyTemplate TEMPLATE = GWT.create(MyTemplate.class);
	private static final PanelSettingUserCodeViewUiBinder uiBinder = GWT
			.create(PanelSettingUserCodeViewUiBinder.class);
	private static final String GROUP_USER_CODE = "group user code";

	private final PanelSettingUserCodePresenter presenter;
	private Map<CheckBox, PacketUserData> radioButtonToUserData;
	private final UserData userData;
	@UiField
	TextBox textBoxUserCode;
	@UiField
	Button buttonSwitchToUserCode;
	@UiField
	HTML htmlInvalidUserCode;
	@UiField
	Button buttonConnect;
	@UiField
	HTML htmlAlreadyConnected;
	@UiField
	HTML htmlConnected;
	@UiField
	VerticalPanel panelUserCodeList;
	@UiField
	Button buttonShowUserCodeList;
	@UiField
	Button buttonSwitchToConnectedUserCode;
	@UiField
	Button buttonDisconnectUserCode;

	@Inject
	public PanelSettingUserCodeView(PanelSettingUserCodePresenter presenter, UserData userData) {
		initWidget(uiBinder.createAndBindUi(this));
		this.presenter = Preconditions.checkNotNull(presenter);
		this.presenter.setView(this);
		this.userData = Preconditions.checkNotNull(userData);
	}

	@UiHandler("buttonSwitchToUserCode")
	void onSwitchToUserCode(ClickEvent e) {
		presenter.switchToUserCode();
	}

	@UiHandler("buttonConnect")
	void onConnect(ClickEvent e) {
		presenter.connect();
	}

	@UiHandler("buttonShowUserCodeList")
	void onShowUserCodeList(ClickEvent e) {
		presenter.showUserCodeList();
	}

	@UiHandler("buttonSwitchToConnectedUserCode")
	void onSwitchToConnectedUserCode(ClickEvent e) {
		presenter.switchToConnectedUserCode();
	}

	@UiHandler("buttonDisconnectUserCode")
	void onDisconnectUserCode(ClickEvent e) {
		presenter.disconnectUserCode();
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		presenter.onLoad();
	}

	@Override
	public void setUserCodeTextBoxEnable(boolean enabled) {
		textBoxUserCode.setEnabled(enabled);
	}

	@Override
	public void setSwitchToUserCodeButtonEnable(boolean enabled) {
		buttonSwitchToUserCode.setEnabled(enabled);
	}

	@Override
	public void setInvalidUserCodeMessageVisible(boolean visible) {
		htmlInvalidUserCode.setVisible(visible);
	}

	@Override
	public String getUserCode() {
		return textBoxUserCode.getValue();
	}

	@Override
	public void showConnectButton() {
		buttonConnect.setVisible(true);
	}

	@Override
	public void setConnectButtonEnable(boolean enabled) {
		buttonConnect.setEnabled(enabled);
	}

	@Override
	public void showAlreadyConnectedMessage() {
		htmlAlreadyConnected.setVisible(true);
	}

	@Override
	public void showRequiredReloadMessage() {
		Window.alert("OKボタンを押した後にF5ボタンを押してページをリロードしてください。");
	}

	@Override
	public void setUserDataList(List<PacketUserData> userDataList) {
		panelUserCodeList.clear();
		radioButtonToUserData = Maps.newHashMap();
		for (PacketUserData userData : userDataList) {
			SafeUri imageUrl = UriUtils.fromString(Constant.ICON_URL_PREFIX
					+ userData.imageFileName);
			SafeHtml label = TEMPLATE.image(imageUrl, userData.userCode, userData.playerName);
			RadioButton radioButton = new RadioButton(GROUP_USER_CODE, label);
			radioButtonToUserData.put(radioButton, userData);
			panelUserCodeList.add(radioButton);

			if (this.userData.getUserCode() == userData.userCode) {
				radioButton.setValue(true);
			}
		}
	}

	@Override
	public void showSwitchToConnectedUserCodeButton() {
		buttonSwitchToConnectedUserCode.setVisible(true);
	}

	@Override
	public void setSwitchToConnectedUserCodeButtonVisible(boolean visible) {
		buttonSwitchToConnectedUserCode.setVisible(visible);
	}

	@Override
	public void setShowUserCodeListButtonEnable(boolean enabled) {
		buttonShowUserCodeList.setEnabled(enabled);
	}

	@Override
	public void showShowUserCodeListButton() {
		buttonShowUserCodeList.setVisible(true);
	}

	@Override
	public int getSelectedUserCode() {
		for (Entry<CheckBox, PacketUserData> entry : radioButtonToUserData.entrySet()) {
			if (entry.getKey().getValue()) {
				return entry.getValue().userCode;
			}
		}

		Preconditions.checkState(false, "Could not find a selected user code.");
		return 0;
	}

	@Override
	public void setDisconnectUserCodeButtonEnabled(boolean enabled) {
		buttonDisconnectUserCode.setEnabled(enabled);
	}

	@Override
	public void setDisconnectUserCodeButtonVisible(boolean visible) {
		buttonDisconnectUserCode.setVisible(visible);
	}

	@Override
	public void showConnectedMessage() {
		htmlConnected.setVisible(true);
	}

}
