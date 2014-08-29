package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;

import com.google.api.gwt.client.OAuth2Login;
import com.google.api.gwt.services.plus.shared.Plus;
import com.google.api.gwt.services.plus.shared.Plus.PlusAuthScope;
import com.google.api.gwt.services.plus.shared.model.Person;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class PanelSettingUserCodePresenter {

	interface View extends IsWidget {
		interface Factory {
			View create();
		}

		void setUserCodeTextBoxEnable(boolean enabled);

		void setSwitchToUserCodeButtonEnable(boolean enabled);

		void setInvalidUserCodeMessageVisible(boolean visible);

		String getUserCode();

		void showConnectButton();

		void setConnectButtonEnable(boolean enabled);

		void showAlreadyConnectedMessage();

		void showRequiredReloadMessage();

		void setUserDataList(List<PacketUserData> userDataList);

		void showSwitchToConnectedUserCodeButton();

		void setSwitchToConnectedUserCodeButtonVisible(boolean visible);

		void setShowUserCodeListButtonEnable(boolean enabled);

		void showShowUserCodeListButton();

		int getSelectedUserCode();

		void setDisconnectUserCodeButtonEnabled(boolean enabled);

		void setDisconnectUserCodeButtonVisible(boolean visible);

		void showConnectedMessage();
	}

	private enum AuthorizeMode {
		CONNECT, SHOW_USER_CODE_LIST,
	}

	private static final Logger logger = Logger.getLogger(PanelSettingUserCodePresenter.class
			.getName());
	@VisibleForTesting
	static final String CLIENT_ID = "684576226407.apps.googleusercontent.com";
	@VisibleForTesting
	static final String USER_ID_ME = "me";
	private final ServiceAsync service;
	private final OAuth2Login oAuth2Login;
	private final Plus plus;
	private final UserData userData;
	private View view;
	private AuthorizeMode authorizeMode;

	@Inject
	public PanelSettingUserCodePresenter(ServiceAsync service, OAuth2Login oAuth2Login, Plus plus,
			UserData userData) {
		this.service = Preconditions.checkNotNull(service);
		this.oAuth2Login = Preconditions.checkNotNull(oAuth2Login);
		this.plus = Preconditions.checkNotNull(plus);
		this.userData = Preconditions.checkNotNull(userData);
	}

	public void setView(View view) {
		Preconditions.checkState(this.view == null);
		this.view = Preconditions.checkNotNull(view);
	}

	public void onLoad() {
		String googlePlusId = userData.getGooglePlusId();
		if (Strings.isNullOrEmpty(googlePlusId)) {
			// 連携されていない場合
			view.showConnectButton();
			view.showShowUserCodeListButton();
		} else {
			// 連携されている場合
			view.showAlreadyConnectedMessage();
			updateUserCodeList(googlePlusId);
		}
	}

	private void updateUserCodeList(String googlePlusId) {
		service.lookupUserDataByGooglePlusId(googlePlusId, callbackLookupUserDataByGooglePlusId);
	}

	@VisibleForTesting
	AsyncCallback<List<PacketUserData>> callbackLookupUserDataByGooglePlusId = new AsyncCallback<List<PacketUserData>>() {
		@Override
		public void onSuccess(List<PacketUserData> result) {
			view.setUserDataList(result);

			if (result.isEmpty()) {
				// Do nothing.
			} else if (result.size() == 1) {
				view.setDisconnectUserCodeButtonVisible(true);
			} else {
				view.setDisconnectUserCodeButtonVisible(true);
				view.setSwitchToConnectedUserCodeButtonVisible(true);
			}
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "ユーザーデータの検索に失敗しました", caught);
		}
	};

	/**
	 * 入力されたユーザーコードに切り替える
	 */
	public void switchToUserCode() {
		view.setInvalidUserCodeMessageVisible(false);
		view.setSwitchToUserCodeButtonEnable(false);

		int userCode;
		try {
			userCode = Integer.parseInt(view.getUserCode());
		} catch (Exception e) {
			view.setInvalidUserCodeMessageVisible(true);
			return;
		}

		service.loadUserData(userCode, callbackLoadUserData);
	}

	@VisibleForTesting
	AsyncCallback<PacketUserData> callbackLoadUserData = new AsyncCallback<PacketUserData>() {
		@Override
		public void onSuccess(PacketUserData result) {
			view.setSwitchToUserCodeButtonEnable(true);

			if (result == null || result.userCode == 0) {
				// ユーザーコードが見つからなかった
				view.setInvalidUserCodeMessageVisible(true);
				return;
			}

			userData.setUserCode(result.userCode);
			view.showRequiredReloadMessage();
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "ユーザーコードの読み込みに失敗しました", caught);
		}
	};

	/**
	 * 現在のユーザーコードをGoogle+アカウントに連携する
	 */
	public void connect() {
		view.setConnectButtonEnable(false);
		authorize(AuthorizeMode.CONNECT);
	}

	private void authorize(AuthorizeMode authorizeMode) {
		this.authorizeMode = authorizeMode;

		System.out.println("oAuth2Login.authorize()");
		oAuth2Login.authorize(CLIENT_ID, PlusAuthScope.PLUS_ME, callbackAuthorize);
	}

	@VisibleForTesting
	final Callback<Void, Exception> callbackAuthorize = new Callback<Void, Exception>() {
		@Override
		public void onSuccess(Void result) {
			System.out.println("plus.people().get()");
			plus.people().get(USER_ID_ME).to(receiverGet).fire();
		}

		@Override
		public void onFailure(Exception reason) {
			logger.log(Level.WARNING, "Google+認証に失敗しました", reason);
		}
	};
	@VisibleForTesting
	final Receiver<Person> receiverGet = new Receiver<Person>() {
		@Override
		public void onSuccess(Person response) {
			System.out.println("plus.people().get()");
			String googlePlusId = response.getId();

			switch (authorizeMode) {
			case CONNECT:
				userData.setGooglePlusId(googlePlusId);
				userData.save();
				updateUserCodeList(googlePlusId);
				break;

			case SHOW_USER_CODE_LIST:
				updateUserCodeList(googlePlusId);
				break;
			}
		}
	};

	/**
	 * Google+に連携しているユーザーコード一覧を表示する
	 */
	public void showUserCodeList() {
		view.setShowUserCodeListButtonEnable(false);
		authorize(AuthorizeMode.SHOW_USER_CODE_LIST);
	}

	/**
	 * 連携済みのユーザーコードに切り替える
	 */
	public void switchToConnectedUserCode() {
		view.setSwitchToUserCodeButtonEnable(false);

		int userCode = view.getSelectedUserCode();
		userData.setUserCode(userCode);
		view.showRequiredReloadMessage();
	}

	/**
	 * 連携済みのユーザーコードを解除する
	 */
	public void disconnectUserCode() {
		view.setDisconnectUserCodeButtonEnabled(false);

		int userCode = view.getSelectedUserCode();
		service.disconnectUserCode(userCode, callbackDisconnectUserCode);

		if (userCode == userData.getUserCode()) {
			view.showRequiredReloadMessage();
		}
	}

	@VisibleForTesting
	AsyncCallback<Void> callbackDisconnectUserCode = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			updateUserCodeList(userData.getGooglePlusId());
			view.setDisconnectUserCodeButtonEnabled(true);
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "ユーザーコード連携の解除に失敗しました", caught);
		}
	};

}
