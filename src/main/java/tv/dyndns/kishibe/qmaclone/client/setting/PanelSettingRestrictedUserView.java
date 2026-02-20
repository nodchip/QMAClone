package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.ListDataProvider;

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
	SimplePanel panelUserCodes;
	@UiField
	TextBox textBoxUserCode;
	@UiField
	Button buttonAddUserCode;
	@UiField
	Button buttonRemoveUserCode;
	@UiField
	Button buttonClearUserCode;
	@UiField
	SimplePanel panelRemoteAddresses;
	@UiField
	TextBox textBoxRemoteAddress;
	@UiField
	Button buttonAddRemoteAddress;
	@UiField
	Button buttonRemoveRemoteAddress;
	@UiField
	Button buttonClearRemoteAddress;
	private final CellTable<Integer> userCodeTable = new CellTable<Integer>(100,
			GWT.<CellTable.BasicResources> create(CellTable.BasicResources.class));
	private final ListDataProvider<Integer> userCodeDataProvider = new ListDataProvider<Integer>();
	private final CellTable<String> remoteAddressTable = new CellTable<String>(100,
			GWT.<CellTable.BasicResources> create(CellTable.BasicResources.class));
	private final ListDataProvider<String> remoteAddressDataProvider = new ListDataProvider<String>();

	public PanelSettingRestrictedUserView(PanelSettingRestrictedUser presenter) {
		this.presenter = Preconditions.checkNotNull(presenter);
		initWidget(uiBinder.createAndBindUi(this));
		setStyleName("settingAdminRoot settingAdminRestrictedRoot");

		for (RestrictionType restrictionType : RestrictionType.values()) {
			listBoxType.addItem(restrictionType.name());
		}
		buttonAddUserCode.addStyleName("creationButtonPrimary");
		buttonRemoveUserCode.addStyleName("creationButtonSecondary");
		buttonClearUserCode.addStyleName("linkDangerButton");
		buttonAddRemoteAddress.addStyleName("creationButtonPrimary");
		buttonRemoveRemoteAddress.addStyleName("creationButtonSecondary");
		buttonClearRemoteAddress.addStyleName("linkDangerButton");

		setupUserCodeTable();
		setupRemoteAddressTable();
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
		List<Integer> sortedCodes = Lists.newArrayList(userCodes);
		Collections.sort(sortedCodes);
		userCodeDataProvider.setList(sortedCodes);
	}

	@Override
	public void setRemoteAddresses(Set<String> remoteAddresses) {
		List<String> sortedAddresses = Lists.newArrayList(remoteAddresses);
		Collections.sort(sortedAddresses);
		remoteAddressDataProvider.setList(sortedAddresses);
	}

	private void setupUserCodeTable() {
		userCodeTable.setWidth("100%");
		userCodeTable.addStyleName("settingAdminRestrictedTable");
		userCodeTable.setEmptyTableWidget(new HTML("<div class='settingAdminEmpty'>登録済みユーザーコードはありません。</div>"));
		TextColumn<Integer> column = new TextColumn<Integer>() {
			@Override
			public String getValue(Integer object) {
				return object == null ? "" : Integer.toString(object);
			}
		};
		column.setFieldUpdater(new FieldUpdater<Integer, String>() {
			@Override
			public void update(int index, Integer object, String value) {
				if (object != null) {
					textBoxUserCode.setValue(Integer.toString(object));
				}
			}
		});
		userCodeTable.addColumn(column, "ユーザーコード");
		userCodeDataProvider.addDataDisplay(userCodeTable);
		panelUserCodes.setWidget(userCodeTable);
	}

	private void setupRemoteAddressTable() {
		remoteAddressTable.setWidth("100%");
		remoteAddressTable.addStyleName("settingAdminRestrictedTable");
		remoteAddressTable.setEmptyTableWidget(new HTML("<div class='settingAdminEmpty'>登録済みリモートアドレスはありません。</div>"));
		TextColumn<String> column = new TextColumn<String>() {
			@Override
			public String getValue(String object) {
				return object == null ? "" : object;
			}
		};
		column.setFieldUpdater(new FieldUpdater<String, String>() {
			@Override
			public void update(int index, String object, String value) {
				if (object != null) {
					textBoxRemoteAddress.setValue(object);
				}
			}
		});
		remoteAddressTable.addColumn(column, "リモートアドレス");
		remoteAddressDataProvider.addDataDisplay(remoteAddressTable);
		panelRemoteAddresses.setWidget(remoteAddressTable);
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
