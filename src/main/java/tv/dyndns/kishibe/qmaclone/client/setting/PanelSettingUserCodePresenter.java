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
      updateUserCodeList(authProvider, authSubject);
    }
  }

  private void updateUserCodeList(String provider, String subject) {
    service.lookupUserDataByExternalAccount(provider, subject, callbackLookupUserDataByGooglePlusId);
  }

  private void disableAllActionButtonsUntilReload() {
    view.setSwitchToUserCodeButtonEnable(false);
    view.setConnectButtonEnable(false);
    view.setShowUserCodeListButtonEnable(false);
    view.setDisconnectUserCodeButtonEnabled(false);
  }

  @VisibleForTesting
  AsyncCallback<List<PacketUserData>> callbackLookupUserDataByGooglePlusId =
      new AsyncCallback<List<PacketUserData>>() {
        @Override
        public void onSuccess(List<PacketUserData> result) {
          view.setShowUserCodeListButtonEnable(true);
          view.setSwitchToConnectedUserCodeButtonVisible(false);
          view.setDisconnectUserCodeButtonVisible(false);
          view.setUserDataList(result);

          if (result.isEmpty()) {
            view.setConnectButtonEnable(true);
            view.showConnectedMessage();
            return;
          }
          view.setDisconnectUserCodeButtonVisible(true);
          if (result.size() != 1) {
            view.setSwitchToConnectedUserCodeButtonVisible(true);
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          view.setShowUserCodeListButtonEnable(true);
          view.setConnectButtonEnable(true);
          logger.log(Level.WARNING, "繝ｦ繝ｼ繧ｶ繝ｼ繝・・繧ｿ縺ｮ讀懃ｴ｢縺ｫ螟ｱ謨励＠縺ｾ縺励◆", caught);
        }
      };

  /**
   * 蜈･蜉帙＆繧後◆繝ｦ繝ｼ繧ｶ繝ｼ繧ｳ繝ｼ繝峨↓蛻・ｊ譖ｿ縺医ｋ縲・   */
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
        view.setInvalidUserCodeMessageVisible(true);
        return;
      }

      userData.setUserCode(result.userCode);
      view.showRequiredReloadMessage();
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "繝ｦ繝ｼ繧ｶ繝ｼ繧ｳ繝ｼ繝峨・隱ｭ縺ｿ霎ｼ縺ｿ縺ｫ螟ｱ謨励＠縺ｾ縺励◆", caught);
    }
  };

  /**
   * 迴ｾ蝨ｨ縺ｮ繝ｦ繝ｼ繧ｶ繝ｼ繧ｳ繝ｼ繝峨ｒ螟夜Κ繧｢繧ｫ繧ｦ繝ｳ繝医↓謗･邯壹☆繧九・   */
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
          updateUserCodeList(provider, subject);
          break;
      }
    }

    @Override
    public void onFailure(Exception reason) {
      view.setConnectButtonEnable(true);
      view.setShowUserCodeListButtonEnable(true);
      logger.log(Level.WARNING, "螟夜Κ繧｢繧ｫ繧ｦ繝ｳ繝郁ｪ榊庄縺ｫ螟ｱ謨励＠縺ｾ縺励◆", reason);
    }
  };

  @VisibleForTesting
  AsyncCallback<Void> callbackSaveExternalAccount = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
      view.hideConnectButton();
      view.setShowUserCodeListButtonVisible(false);
      view.setSwitchToConnectedUserCodeButtonVisible(false);
      view.setDisconnectUserCodeButtonVisible(false);
      view.showAlreadyConnectedMessage();
      disableAllActionButtonsUntilReload();
      view.showRequiredReloadMessage();
    }

    @Override
    public void onFailure(Throwable caught) {
      view.setConnectButtonEnable(true);
      view.setShowUserCodeListButtonEnable(true);
      logger.log(Level.WARNING, "外部アカウント設定の保存に失敗しました", caught);
    }
  };

  /**
   * 螟夜Κ繧｢繧ｫ繧ｦ繝ｳ繝医↓謗･邯壹＠縺ｦ縺・ｋ繝ｦ繝ｼ繧ｶ繝ｼ繧ｳ繝ｼ繝我ｸ隕ｧ繧定｡ｨ遉ｺ縺吶ｋ縲・   */
  public void showUserCodeList() {
    view.setShowUserCodeListButtonEnable(false);
    view.setConnectButtonEnable(false);
    String provider = userData.getAuthProvider();
    String subject = userData.getAuthSubject();
    if (!Strings.isNullOrEmpty(provider) && !Strings.isNullOrEmpty(subject)) {
      updateUserCodeList(provider, subject);
      return;
    }
    authorize(AuthorizeMode.SHOW_USER_CODE_LIST);
  }

  /**
   * 謗･邯壽ｸ医∩縺ｮ繝ｦ繝ｼ繧ｶ繝ｼ繧ｳ繝ｼ繝峨↓蛻・ｊ譖ｿ縺医ｋ縲・   */
  public void switchToConnectedUserCode() {
    view.setSwitchToUserCodeButtonEnable(false);

    int userCode = view.getSelectedUserCode();
    userData.setUserCode(userCode);
    view.showRequiredReloadMessage();
  }

  /**
   * 謗･邯壽ｸ医∩縺ｮ繝ｦ繝ｼ繧ｶ繝ｼ繧ｳ繝ｼ繝峨ｒ蛻・妙縺吶ｋ縲・   */
  public void disconnectUserCode() {
    view.setDisconnectUserCodeButtonEnabled(false);

    int userCode = view.getSelectedUserCode();
    service.disconnectExternalAccount(userCode, callbackDisconnectUserCode);
  }

  @VisibleForTesting
  AsyncCallback<Void> callbackDisconnectUserCode = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
      view.showRequiredReloadMessage();
    }

    @Override
    public void onFailure(Throwable caught) {
      view.setDisconnectUserCodeButtonEnabled(true);
      logger.log(Level.WARNING, "繝ｦ繝ｼ繧ｶ繝ｼ繧ｳ繝ｼ繝牙・譁ｭ縺ｫ螟ｱ謨励＠縺ｾ縺励◆", caught);
    }
  };
}

