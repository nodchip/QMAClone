package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PanelSettingRestrictedUserView extends Composite implements
		PanelSettingRestrictedUser.View {

	private static PanelSettingRestrictedUserViewUiBinder uiBinder = GWT
			.create(PanelSettingRestrictedUserViewUiBinder.class);

	interface PanelSettingRestrictedUserViewUiBinder extends
			UiBinder<Widget, PanelSettingRestrictedUserView> {
	}

	private final PanelSettingRestrictedUser presenter;
	@UiField
	ListBox listBoxType;
	@UiField
	FlowPanel panelUserCodes;
	@UiField
	TextBox textBoxUserCode;
	@UiField
	Button buttonAddUserCode;
	@UiField
	Button buttonRemoveUserCode;
	@UiField
	Button buttonClearUserCode;
	@UiField
	FlowPanel panelRemoteAddresses;
	@UiField
	TextBox textBoxRemoteAddress;
	@UiField
	Button buttonAddRemoteAddress;
	@UiField
	Button buttonRemoveRemoteAddress;
	@UiField
	Button buttonClearRemoteAddress;

	public PanelSettingRestrictedUserView(PanelSettingRestrictedUser presenter) {
		this.presenter = Preconditions.checkNotNull(presenter);
		initWidget(uiBinder.createAndBindUi(this));

		for (RestrictionType restrictionType : RestrictionType.values()) {
			listBoxType.addItem(restrictionType.name());
		}
	}

	@Override
	public RestrictionType getType() {
		return RestrictionType.valueOf(listBoxType.getItemText(listBoxType.getSelectedIndex()));
	}

	@Override
	public int getUserCode() {
		return Integer.valueOf(textBoxUserCode.getValue());
	}

	@Override
	public String getRemoteAddress() {
		return textBoxRemoteAddress.getValue();
	}

	@Override
	public void setUserCodes(Set<Integer> userCodes) {
		panelUserCodes.clear();
		for (final int userCode : userCodes) {
			panelUserCodes.add(new Button(String.valueOf(userCode), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					textBoxUserCode.setValue(String.valueOf(userCode));
				}
			}));
		}
	}

	@Override
	public void setRemoteAddresses(Set<String> remoteAddresses) {
		panelRemoteAddresses.clear();
		for (final String remoteAddress : remoteAddresses) {
			panelRemoteAddresses.add(new Button(remoteAddress, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					textBoxRemoteAddress.setValue(remoteAddress);
				}
			}));
		}
	}

	@UiHandler("buttonAddUserCode")
	void onButtonAddUserCode(ClickEvent e) {
		presenter.onAddUserCodeButton();
	}

	@UiHandler("buttonRemoveUserCode")
	void onButtonRemoveUserCode(ClickEvent e) {
		presenter.onRemoveUserCodeButton();
	}

	@UiHandler("buttonClearUserCode")
	void onButtonClearUserCode(ClickEvent e) {
		presenter.onClearUserCodesButton();
	}

	@UiHandler("buttonAddRemoteAddress")
	void onButtonAddRemoteAddress(ClickEvent e) {
		presenter.onAddRemoteAddressButton();
	}

	@UiHandler("buttonRemoveRemoteAddress")
	void onButtonRemoveRemoteAddress(ClickEvent e) {
		presenter.onRemoveRemoteAddressButton();
	}

	@UiHandler("buttonClearRemoteAddress")
	void onButtonClearRemoteAddress(ClickEvent e) {
		presenter.onClearRemoteAddressesButton();
	}

	@UiHandler("listBoxType")
	void onListBoxType(ChangeEvent e) {
		presenter.onTypeChanged();
	}

}
