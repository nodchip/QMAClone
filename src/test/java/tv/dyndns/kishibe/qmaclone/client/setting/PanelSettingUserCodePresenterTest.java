package tv.dyndns.kishibe.qmaclone.client.setting;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class PanelSettingUserCodePresenterTest {

  private static final String FAKE_GOOGLE_PLUS_ID = "fake google plus id";
  private static final int FAKE_USER_CODE_1 = 11111111;
  private static final int FAKE_USER_CODE_2 = 22222222;
  @Mock
  private ServiceAsync mockService;
  @Mock
  private ExternalAccountConnector mockExternalAccountConnector;
  @Mock
  private PanelSettingUserCodePresenter.View mockView;
  @Mock
  private UserData mockUserData;
  private PanelSettingUserCodePresenter presenter;

  @Before
  public void setUp() throws Exception {
    presenter =
        new PanelSettingUserCodePresenter(mockService, mockExternalAccountConnector, mockUserData);
    presenter.setView(mockView);
  }

  @Test
  public void onLoadShouldProcessWithoutGooglePlusId() {
    when(mockUserData.getAuthProvider()).thenReturn(null);
    when(mockUserData.getAuthSubject()).thenReturn(null);

    presenter.onLoad();

    verify(mockView).showConnectButton();
    verify(mockView).showShowUserCodeListButton();
  }

  @Test
  public void onLoadShouldProcessWithGooglePlusId() {
    when(mockUserData.getAuthProvider()).thenReturn("google");
    when(mockUserData.getAuthSubject()).thenReturn(FAKE_GOOGLE_PLUS_ID);

    presenter.onLoad();

    verify(mockView).showAlreadyConnectedMessage();
    verify(mockService)
        .lookupUserDataByExternalAccount(
            "google", FAKE_GOOGLE_PLUS_ID, presenter.callbackLookupUserDataByGooglePlusId);
  }

  @Test
  public void callbackLookupUserDataByGooglePlusIdShouldShowBothButtonIfMultipleUserData() {
    ImmutableList<PacketUserData> fakeUserDataList =
        ImmutableList.of(TestDataProvider.getUserData(), TestDataProvider.getUserData());
    presenter.callbackLookupUserDataByGooglePlusId.onSuccess(fakeUserDataList);

    verify(mockView).setUserDataList(fakeUserDataList);
    verify(mockView).setShowUserCodeListButtonVisible(false);
    verify(mockView).setSwitchToConnectedUserCodeButtonVisible(false);
    verify(mockView).setDisconnectUserCodeButtonVisible(false);
    verify(mockView).setSwitchToConnectedUserCodeButtonVisible(true);
    verify(mockView).setDisconnectUserCodeButtonVisible(true);
  }

  @Test
  public void callbackLookupUserDataByGooglePlusIdShouldShowDisconnectOnlyIfOneUserDataOnInitialLoad() {
    ImmutableList<PacketUserData> fakeUserDataList = ImmutableList.of(TestDataProvider.getUserData());
    presenter.callbackLookupUserDataByGooglePlusId.onSuccess(fakeUserDataList);

    verify(mockView).setUserDataList(fakeUserDataList);
    verify(mockView).setSwitchToConnectedUserCodeButtonVisible(false);
    verify(mockView).setDisconnectUserCodeButtonVisible(false);
    verify(mockView, never()).setSwitchToConnectedUserCodeButtonVisible(true);
    verify(mockView).setDisconnectUserCodeButtonVisible(true);
  }

  @Test
  public void callbackLookupUserDataByGooglePlusIdShouldShowSwitchAndDisconnectIfOneUserDataAfterShowButton() {
    when(mockUserData.getAuthProvider()).thenReturn("google");
    when(mockUserData.getAuthSubject()).thenReturn(FAKE_GOOGLE_PLUS_ID);
    presenter.showUserCodeList();

    ImmutableList<PacketUserData> fakeUserDataList = ImmutableList.of(TestDataProvider.getUserData());
    presenter.callbackLookupUserDataByGooglePlusIdByUserAction.onSuccess(fakeUserDataList);

    verify(mockView).setUserDataList(fakeUserDataList);
    verify(mockView).setShowUserCodeListButtonVisible(false);
    verify(mockView).setSwitchToConnectedUserCodeButtonVisible(false);
    verify(mockView).setDisconnectUserCodeButtonVisible(false);
    verify(mockView).setSwitchToConnectedUserCodeButtonVisible(true);
    verify(mockView).setDisconnectUserCodeButtonVisible(true);
  }

  @Test
  public void callbackLookupUserDataByGooglePlusIdShouldNotShowButtonIfEmpty() {
    List<PacketUserData> fakeUserDataList = Lists.newArrayList();
    presenter.callbackLookupUserDataByGooglePlusId.onSuccess(fakeUserDataList);

    verify(mockView).setUserDataList(fakeUserDataList);
    verify(mockView).setConnectButtonEnable(true);
    verify(mockView).setSwitchToConnectedUserCodeButtonVisible(false);
    verify(mockView).setDisconnectUserCodeButtonVisible(false);
    verify(mockView).showConnectedMessage();
    verify(mockView, never()).setSwitchToConnectedUserCodeButtonVisible(true);
    verify(mockView, never()).setDisconnectUserCodeButtonVisible(true);
  }

  @Test
  public void switchToUserCodeShouldLoadUserData() {
    when(mockView.getUserCode()).thenReturn(String.valueOf(FAKE_USER_CODE_1));

    presenter.switchToUserCode();

    verify(mockView).setInvalidUserCodeMessageVisible(false);
    verify(mockView).setSwitchToUserCodeButtonEnable(false);
    verify(mockService).loadUserData(FAKE_USER_CODE_1, presenter.callbackLoadUserData);
  }

  @Test
  public void switchToUserCodeShouldSetInvalidMessage() {
    when(mockView.getUserCode()).thenReturn("invalid user code");

    presenter.switchToUserCode();

    verify(mockView).setInvalidUserCodeMessageVisible(false);
    verify(mockView).setSwitchToUserCodeButtonEnable(false);
    verify(mockView).setInvalidUserCodeMessageVisible(true);
  }

  @Test
  public void callbackLoadUserDataShouldSetUserCodeAndShowMessage() {
    PacketUserData fakeUserData = TestDataProvider.getUserData();
    fakeUserData.userCode = FAKE_USER_CODE_1;

    presenter.callbackLoadUserData.onSuccess(fakeUserData);

    verify(mockView).setSwitchToUserCodeButtonEnable(true);
    verify(mockUserData).setUserCode(FAKE_USER_CODE_1);
    verify(mockView).showRequiredReloadMessage();
  }

  @Test
  public void callbackLoadUserDataShouldShowInvalidUserCodeMessage() {
    presenter.callbackLoadUserData.onSuccess(null);

    verify(mockView).setSwitchToUserCodeButtonEnable(true);
    verify(mockView).setInvalidUserCodeMessageVisible(true);
  }

  @Test
  public void connectShouldAuthorizeExternalAccount() {
    presenter.connect();

    verify(mockView).setConnectButtonEnable(false);
    verify(mockView).setShowUserCodeListButtonEnable(false);
    verify(mockExternalAccountConnector).authorize(presenter.callbackAuthorize);
  }

  @Test
  public void callbackAuthorizeShouldSetGooglePlusIdAndShowMessage() {
    presenter.connect();
    presenter.callbackAuthorize.onSuccess("google", FAKE_GOOGLE_PLUS_ID);

    verify(mockService, never())
        .lookupUserDataByExternalAccount(
            "google", FAKE_GOOGLE_PLUS_ID, presenter.callbackLookupUserDataByGooglePlusId);
    verify(mockUserData).setAuthProvider("google");
    verify(mockUserData).setAuthSubject(FAKE_GOOGLE_PLUS_ID);
    verify(mockUserData).save(presenter.callbackSaveExternalAccount);
  }

  @Test
  public void callbackSaveExternalAccountShouldHideConnectButtonAndRequireReload() {
    when(mockUserData.getAuthProvider()).thenReturn("google");
    when(mockUserData.getAuthSubject()).thenReturn(FAKE_GOOGLE_PLUS_ID);

    presenter.callbackSaveExternalAccount.onSuccess(null);

    verify(mockView).hideConnectButton();
    verify(mockView).setShowUserCodeListButtonVisible(false);
    verify(mockView).setSwitchToConnectedUserCodeButtonVisible(false);
    verify(mockView).setDisconnectUserCodeButtonVisible(false);
    verify(mockView).showAlreadyConnectedMessage();
    verify(mockView).setSwitchToUserCodeButtonEnable(false);
    verify(mockView).setConnectButtonEnable(false);
    verify(mockView).setShowUserCodeListButtonEnable(false);
    verify(mockView).setDisconnectUserCodeButtonEnabled(false);
    verify(mockView).showRequiredReloadMessage();
  }

  @Test
  public void callbackAuthorizeShouldUpdateUserCodeList() {
    presenter.showUserCodeList();
    presenter.callbackAuthorize.onSuccess("google", FAKE_GOOGLE_PLUS_ID);

    verify(mockService)
        .lookupUserDataByExternalAccount(
            "google", FAKE_GOOGLE_PLUS_ID, presenter.callbackLookupUserDataByGooglePlusIdByUserAction);
  }

  @Test
  public void showUserCodeListShouldAuthorize() {
    when(mockUserData.getAuthProvider()).thenReturn(null);
    when(mockUserData.getAuthSubject()).thenReturn(null);

    presenter.showUserCodeList();

    verify(mockView).setShowUserCodeListButtonEnable(false);
    verify(mockView).setConnectButtonEnable(false);
    verify(mockExternalAccountConnector).authorize(presenter.callbackAuthorize);
  }

  @Test
  public void showUserCodeListShouldLookupDirectlyWhenAuthInfoExists() {
    when(mockUserData.getAuthProvider()).thenReturn("google");
    when(mockUserData.getAuthSubject()).thenReturn(FAKE_GOOGLE_PLUS_ID);

    presenter.showUserCodeList();

    verify(mockView).setShowUserCodeListButtonEnable(false);
    verify(mockView).setConnectButtonEnable(false);
    verify(mockService)
        .lookupUserDataByExternalAccount(
            "google", FAKE_GOOGLE_PLUS_ID, presenter.callbackLookupUserDataByGooglePlusIdByUserAction);
    verify(mockExternalAccountConnector, never()).authorize(presenter.callbackAuthorize);
  }

  @Test
  public void switchToConnectedUserCodeShouldSetUserCode() {
    when(mockView.getSelectedUserCode()).thenReturn(FAKE_USER_CODE_1);

    presenter.switchToConnectedUserCode();

    verify(mockView).setSwitchToUserCodeButtonEnable(false);
    verify(mockUserData).setUserCode(FAKE_USER_CODE_1);
    verify(mockView).showRequiredReloadMessage();
  }

  @Test
  public void disconnectUserCodeShouldCallRpc() {
    when(mockView.getSelectedUserCode()).thenReturn(FAKE_USER_CODE_1);

    presenter.disconnectUserCode();

    verify(mockView).setDisconnectUserCodeButtonEnabled(false);
    verify(mockService).disconnectExternalAccount(FAKE_USER_CODE_1, presenter.callbackDisconnectUserCode);
  }

  @Test
  public void disconnectUserCodeShouldRequireReloadIfMine() {
    when(mockView.getSelectedUserCode()).thenReturn(FAKE_USER_CODE_1);

    presenter.disconnectUserCode();

    verify(mockView).setDisconnectUserCodeButtonEnabled(false);
    verify(mockService).disconnectExternalAccount(FAKE_USER_CODE_1, presenter.callbackDisconnectUserCode);
  }

  @Test
  public void callbackDisconnectUserCodeShouldShowReloadMessage() {
    presenter.callbackDisconnectUserCode.onSuccess(null);

    verify(mockView).showRequiredReloadMessage();
  }

  @Test
  public void callbackDisconnectUserCodeShouldEnableButtonOnFailure() {
    presenter.callbackDisconnectUserCode.onFailure(new RuntimeException("failure"));

    verify(mockView).setDisconnectUserCodeButtonEnabled(true);
  }
}
