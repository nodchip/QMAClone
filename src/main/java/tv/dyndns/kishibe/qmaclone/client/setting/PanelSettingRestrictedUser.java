package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class PanelSettingRestrictedUser implements IsWidget {

	interface View extends IsWidget {
		RestrictionType getType();

		int getUserCode();

		String getRemoteAddress();

		void setUserCodes(Set<Integer> userCodes);

		void setRemoteAddresses(Set<String> remoteAddresses);
	}

	private static final Logger logger = Logger.getLogger(PanelSettingRestrictedUser.class
			.getName());
	@VisibleForTesting
	View view;
	private final ServiceAsync service;

	public PanelSettingRestrictedUser(ServiceAsync service) {
		this.service = Preconditions.checkNotNull(service);
	}

	@VisibleForTesting
	PanelSettingRestrictedUser(ServiceAsync service, View view) {
		this.service = Preconditions.checkNotNull(service);
		this.view = Preconditions.checkNotNull(view);
		update();
	}

	public void setView(View view) {
		this.view = Preconditions.checkNotNull(view);
		update();
	}

	public void onTypeChanged() {
		update();
	}

	@VisibleForTesting
	void update() {
		RestrictionType restrictionType = view.getType();
		service.getRestrictedUserCodes(restrictionType, callbackGetRestrictedUserCodes);
	}

	@VisibleForTesting
	final AsyncCallback<Set<Integer>> callbackGetRestrictedUserCodes = new AsyncCallback<Set<Integer>>() {
		@Override
		public void onSuccess(Set<Integer> result) {
			view.setUserCodes(result);
			RestrictionType restrictionType = view.getType();
			service.getRestrictedRemoteAddresses(restrictionType,
					callbackGetRestrictedRemoteAddresses);
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "制限ユーザーコードの取得に失敗しました");
		}
	};

	@VisibleForTesting
	final AsyncCallback<Set<String>> callbackGetRestrictedRemoteAddresses = new AsyncCallback<Set<String>>() {
		@Override
		public void onSuccess(Set<String> result) {
			view.setRemoteAddresses(result);
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "制限リモートアドレスの取得に失敗しました");
		}
	};

	public void onAddUserCodeButton() {
		int userCode = view.getUserCode();
		RestrictionType restrictionType = view.getType();
		service.addRestrictedUserCode(userCode, restrictionType, callbackRestrictedUser);
	}

	public void onRemoveUserCodeButton() {
		int userCode = view.getUserCode();
		RestrictionType restrictionType = view.getType();
		service.removeRestrictedUserCode(userCode, restrictionType, callbackRestrictedUser);
	}

	public void onClearUserCodesButton() {
		RestrictionType restrictionType = view.getType();
		service.clearRestrictedUserCodes(restrictionType, callbackRestrictedUser);
	}

	public void onAddRemoteAddressButton() {
		String remoteAddress = view.getRemoteAddress();
		RestrictionType restrictionType = view.getType();
		service.addRestrictedRemoteAddress(remoteAddress, restrictionType, callbackRestrictedUser);
	}

	public void onRemoveRemoteAddressButton() {
		String remoteAddress = view.getRemoteAddress();
		RestrictionType restrictionType = view.getType();
		service.removeRestrictedRemoteAddress(remoteAddress, restrictionType,
				callbackRestrictedUser);
	}

	public void onClearRemoteAddressesButton() {
		RestrictionType restrictionType = view.getType();
		service.clearRestrictedRemoteAddresses(restrictionType, callbackRestrictedUser);
	}

	@VisibleForTesting
	final AsyncCallback<Void> callbackRestrictedUser = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			update();
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "制限ユーザーの追加/削除/クリアに失敗しました");
		}
	};

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
