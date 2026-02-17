package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

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

    void hideConnectButton();

    void setConnectButtonEnable(boolean enabled);

    void showAlreadyConnectedMessage();

    void showRequiredReloadMessage();

    void setUserDataList(List<PacketUserData> userDataList);

    void showSwitchToConnectedUserCodeButton();

    void setSwitchToConnectedUserCodeButtonVisible(boolean visible);

    void setShowUserCodeListButtonEnable(boolean enabled);

    void setShowUserCodeListButtonVisible(boolean visible);

    void showShowUserCodeListButton();

    int getSelectedUserCode();

    void setDisconnectUserCodeButtonEnabled(boolean enabled);

    void setDisconnectUserCodeButtonVisible(boolean visible);

    void showConnectedMessage();
  }

  private enum AuthorizeMode {
    CONNECT, SHOW_USER_CODE_LIST,
  }

  private static final Logger logger =
      Logger.getLogger(PanelSettingUserCodePresenter.class.getName());
  private final ServiceAsync service;
  private final ExternalAccountConnector externalAccountConnector;
  private final UserData userData;
  private View view;
  private AuthorizeMode authorizeMode;

  @Inject
  public PanelSettingUserCodePresenter(
      ServiceAsync service, ExternalAccountConnector externalAccountConnector, UserData userData) {
    this.service = Preconditions.checkNotNull(service);
    this.externalAccountConnector = Preconditions.checkNotNull(externalAccountConnector);
    this.userData = Preconditions.checkNotNull(userData);
  }

  public void setView(View view) {
    Preconditions.checkState(this.view == null);
    this.view = Preconditions.checkNotNull(view);
  }

  public void onLoad() {
    String authProvider = userData.getAuthProvider();
    String authSubject = userData.getAuthSubject();
    if (Strings.isNullOrEmpty(authProvider) || Strings.isNullOrEmpty(authSubject)) {
      view.showConnectButton();
      view.showShowUserCodeListButton();
    } else {
      view.showAlreadyConnectedMessage();
      updateUserDataListForInitialLoad(authProvider, authSubject);
    }
  }

  private void updateUserDataListForInitialLoad(String provider, String subject) {
    service.lookupUserDataByExternalAccount(provider, subject, callbackLookupUserDataByGooglePlusId);
  }

  private void updateUserDataListForUserAction(String provider, String subject) {
    service.lookupUserDataByExternalAccount(
        provider, subject, callbackLookupUserDataByGooglePlusIdByUserAction);
  }

  private void disableAllActionButtonsUntilReload() {
    view.setSwitchToUserCodeButtonEnable(false);
    view.setConnectButtonEnable(false);
    view.setShowUserCodeListButtonEnable(false);
    view.setDisconnectUserCodeButtonEnabled(false);
  }

  @VisibleForTesting
  AsyncCallback<List<PacketUserData>> callbackLookupUserDataByGooglePlusId =
      new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<List<PacketUserData>>() {
        @Override
        public void onSuccess(List<PacketUserData> result) {
          handleLookupUserDataSuccess(result, false);
        }

        @Override
        public void onFailureRpc(Throwable caught) {
          handleLookupUserDataFailure(caught);
        }
      };

  @VisibleForTesting
  AsyncCallback<List<PacketUserData>> callbackLookupUserDataByGooglePlusIdByUserAction =
      new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<List<PacketUserData>>() {
        @Override
        public void onSuccess(List<PacketUserData> result) {
          handleLookupUserDataSuccess(result, true);
        }

        @Override
        public void onFailureRpc(Throwable caught) {
          handleLookupUserDataFailure(caught);
        }
      };

  private void handleLookupUserDataSuccess(List<PacketUserData> result, boolean userAction) {
    view.setShowUserCodeListButtonEnable(true);
    view.setSwitchToConnectedUserCodeButtonVisible(false);
    view.setDisconnectUserCodeButtonVisible(false);
    view.setUserDataList(result);

    if (result.isEmpty()) {
      view.setConnectButtonEnable(true);
      view.showConnectedMessage();
      return;
    }

    view.setShowUserCodeListButtonVisible(false);
    view.setDisconnectUserCodeButtonVisible(true);
    if (userAction || result.size() >= 2) {
      view.setSwitchToConnectedUserCodeButtonVisible(true);
    }
  }

  private void handleLookupUserDataFailure(Throwable caught) {
    view.setShowUserCodeListButtonEnable(true);
    view.setConnectButtonEnable(true);
    logger.log(Level.WARNING, "ユーザーデータの検索に失敗しました", caught);
  }

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
  AsyncCallback<PacketUserData> callbackLoadUserData = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<PacketUserData>() {
    @Override
    public void onSuccess(PacketUserData result) {
      view.setSwitchToUserCodeButtonEnable(true);

      if (result == null || result.userCode == 0) {
        view.setInvalidUserCodeMessageVisible(true);
        return;
      }

      userData.setUserCode(result.userCode);
      SettingSaveToast.showSaved("ユーザーコード切り替え");
      view.showRequiredReloadMessage();
    }

    @Override
    public void onFailureRpc(Throwable caught) {
      logger.log(Level.WARNING, "ユーザーコードの読み込みに失敗しました", caught);
    }
  };

  public void connect() {
    view.setConnectButtonEnable(false);
    view.setShowUserCodeListButtonEnable(false);
    authorize(AuthorizeMode.CONNECT);
  }

  private void authorize(AuthorizeMode authorizeMode) {
    this.authorizeMode = authorizeMode;
    externalAccountConnector.authorize(callbackAuthorize);
  }

  @VisibleForTesting
  final ExternalAccountConnector.Callback callbackAuthorize = new ExternalAccountConnector.Callback() {
    @Override
    public void onSuccess(String provider, String subject) {
      switch (authorizeMode) {
        case CONNECT:
          userData.setAuthProvider(provider);
          userData.setAuthSubject(subject);
          userData.save(callbackSaveExternalAccount);
          break;

        case SHOW_USER_CODE_LIST:
          updateUserDataListForUserAction(provider, subject);
          break;
      }
    }

    @Override
    public void onFailure(Exception reason) {
      view.setConnectButtonEnable(true);
      view.setShowUserCodeListButtonEnable(true);
      logger.log(Level.WARNING, "外部アカウント認可に失敗しました", reason);
    }
  };

  @VisibleForTesting
  AsyncCallback<Void> callbackSaveExternalAccount = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
      view.hideConnectButton();
      view.setShowUserCodeListButtonVisible(false);
      view.setSwitchToConnectedUserCodeButtonVisible(false);
      view.setDisconnectUserCodeButtonVisible(false);
      view.showAlreadyConnectedMessage();
      SettingSaveToast.showSaved("Googleアカウント連携");
      disableAllActionButtonsUntilReload();
      view.showRequiredReloadMessage();
    }

    @Override
    public void onFailureRpc(Throwable caught) {
      view.setConnectButtonEnable(true);
      view.setShowUserCodeListButtonEnable(true);
      logger.log(Level.WARNING, "外部アカウント設定の保存に失敗しました", caught);
    }
  };

  public void showUserCodeList() {
    view.setShowUserCodeListButtonEnable(false);
    view.setConnectButtonEnable(false);
    String provider = userData.getAuthProvider();
    String subject = userData.getAuthSubject();
    if (!Strings.isNullOrEmpty(provider) && !Strings.isNullOrEmpty(subject)) {
      updateUserDataListForUserAction(provider, subject);
      return;
    }
    authorize(AuthorizeMode.SHOW_USER_CODE_LIST);
  }

  public void switchToConnectedUserCode() {
    view.setSwitchToUserCodeButtonEnable(false);

    int userCode = view.getSelectedUserCode();
    userData.setUserCode(userCode);
    SettingSaveToast.showSaved("連携済みユーザーコード切り替え");
    view.showRequiredReloadMessage();
  }

  public void disconnectUserCode() {
    view.setDisconnectUserCodeButtonEnabled(false);

    int userCode = view.getSelectedUserCode();
    service.disconnectExternalAccount(userCode, callbackDisconnectUserCode);
  }

  @VisibleForTesting
  AsyncCallback<Void> callbackDisconnectUserCode = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
      SettingSaveToast.showSaved("Googleアカウント連携解除");
      view.showRequiredReloadMessage();
    }

    @Override
    public void onFailureRpc(Throwable caught) {
      view.setDisconnectUserCodeButtonEnabled(true);
      logger.log(Level.WARNING, "ユーザーコード切断に失敗しました", caught);
    }
  };
}
