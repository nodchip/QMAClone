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

import com.google.api.gwt.client.OAuth2Login;
import com.google.api.gwt.services.plus.shared.Plus;
import com.google.api.gwt.services.plus.shared.Plus.PeopleContext;
import com.google.api.gwt.services.plus.shared.Plus.PeopleContext.GetRequest;
import com.google.api.gwt.services.plus.shared.Plus.PlusAuthScope;
import com.google.api.gwt.services.plus.shared.model.Person;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.web.bindery.requestfactory.shared.RequestContext;

@RunWith(MockitoJUnitRunner.class)
public class PanelSettingUserCodePresenterTest {

  private static final String FAKE_GOOGLE_PLUS_ID = "fake google plus id";
  private static final int FAKE_USER_CODE_1 = 11111111;
  private static final int FAKE_USER_CODE_2 = 22222222;
  @Mock
  private ServiceAsync mockService;
  @Mock
  private OAuth2Login mockOAuth2Login;
  @Mock
  private Plus mockPlus;
  @Mock
  private PanelSettingUserCodePresenter.View mockView;
  @Mock
  private PeopleContext mockPeopleContext;
  @Mock
  private GetRequest mockGetRequest;
  @Mock
  private RequestContext mockRequestContext;
  @Mock
  private Person mockPerson;
  @Mock
  private UserData mockUserData;
  private PanelSettingUserCodePresenter presenter;

  @Before
  public void setUp() throws Exception {
    presenter = new PanelSettingUserCodePresenter(mockService, mockOAuth2Login, mockPlus,
        mockUserData);
    presenter.setView(mockView);
  }

  @Test
  public void onLoadShouldProcessWithoutGooglePlusId() {
    when(mockUserData.getGooglePlusId()).thenReturn(null);

    presenter.onLoad();

    verify(mockView).showConnectButton();
    verify(mockView).showShowUserCodeListButton();
  }

  @Test
  public void onLoadShouldProcessWithGooglePlusId() {
    when(mockUserData.getGooglePlusId()).thenReturn(FAKE_GOOGLE_PLUS_ID);

    presenter.onLoad();

    verify(mockView).showAlreadyConnectedMessage();
    verify(mockService).lookupUserDataByGooglePlusId(FAKE_GOOGLE_PLUS_ID,
        presenter.callbackLookupUserDataByGooglePlusId);
  }

  @Test
  public void callbackLookupUserDataByGooglePlusIdShouldShowBothButtonIfMultipleUserData() {
    ImmutableList<PacketUserData> fakeUserDataList = ImmutableList.of(
        TestDataProvider.getUserData(), TestDataProvider.getUserData());
    presenter.callbackLookupUserDataByGooglePlusId.onSuccess(fakeUserDataList);

    verify(mockView).setUserDataList(fakeUserDataList);
    verify(mockView).setSwitchToConnectedUserCodeButtonVisible(true);
    verify(mockView).setDisconnectUserCodeButtonVisible(true);
  }

  @Test
  public void callbackLookupUserDataByGooglePlusIdShouldShowDisconnectIfOneUserData() {
    ImmutableList<PacketUserData> fakeUserDataList = ImmutableList.of(TestDataProvider
        .getUserData());
    presenter.callbackLookupUserDataByGooglePlusId.onSuccess(fakeUserDataList);

    verify(mockView).setUserDataList(fakeUserDataList);
    verify(mockView, never()).setSwitchToConnectedUserCodeButtonVisible(true);
    verify(mockView).setDisconnectUserCodeButtonVisible(true);
  }

  @Test
  public void callbackLookupUserDataByGooglePlusIdShouldNotShowButtonIfEmpty() {
    List<PacketUserData> fakeUserDataList = Lists.newArrayList();
    presenter.callbackLookupUserDataByGooglePlusId.onSuccess(fakeUserDataList);

    verify(mockView).setUserDataList(fakeUserDataList);
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
  public void connectShouldAuthorizeGooglePlusId() {
    presenter.connect();

    verify(mockView).setConnectButtonEnable(false);
    verify(mockOAuth2Login).authorize(PanelSettingUserCodePresenter.CLIENT_ID,
        PlusAuthScope.PLUS_ME, presenter.callbackAuthorize);
  }

  @Test
  public void callbackAuthorizeShouldGetMyProfile() {
    when(mockPlus.people()).thenReturn(mockPeopleContext);
    when(mockPeopleContext.get(PanelSettingUserCodePresenter.USER_ID_ME))
        .thenReturn(mockGetRequest);
    when(mockGetRequest.to(presenter.receiverGet)).thenReturn(mockRequestContext);

    presenter.callbackAuthorize.onSuccess(null);

    verify(mockRequestContext).fire();
  }

  @Test
  public void receiverGetShouldSetGooglePlusIdAndShowMessage() {
    when(mockPerson.getId()).thenReturn(FAKE_GOOGLE_PLUS_ID);

    presenter.connect();
    presenter.receiverGet.onSuccess(mockPerson);

    verify(mockService).lookupUserDataByGooglePlusId(FAKE_GOOGLE_PLUS_ID,
        presenter.callbackLookupUserDataByGooglePlusId);
    verify(mockUserData).setGooglePlusId(FAKE_GOOGLE_PLUS_ID);
    verify(mockUserData).save();
  }

  @Test
  public void receiverGetShouldUpdateUserCodeList() {
    when(mockPerson.getId()).thenReturn(FAKE_GOOGLE_PLUS_ID);

    presenter.showUserCodeList();
    presenter.receiverGet.onSuccess(mockPerson);

    verify(mockService).lookupUserDataByGooglePlusId(FAKE_GOOGLE_PLUS_ID,
        presenter.callbackLookupUserDataByGooglePlusId);
  }

  @Test
  public void showUserCodeListShouldAuthorize() {
    presenter.showUserCodeList();

    verify(mockView).setShowUserCodeListButtonEnable(false);
    verify(mockOAuth2Login).authorize(PanelSettingUserCodePresenter.CLIENT_ID,
        PlusAuthScope.PLUS_ME, presenter.callbackAuthorize);
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
    when(mockUserData.getUserCode()).thenReturn(FAKE_USER_CODE_2);

    presenter.disconnectUserCode();

    verify(mockView).setDisconnectUserCodeButtonEnabled(false);
    verify(mockService).disconnectUserCode(FAKE_USER_CODE_1, presenter.callbackDisconnectUserCode);
  }

  @Test
  public void disconnectUserCodeShouldRequireReloadIfMine() {
    when(mockView.getSelectedUserCode()).thenReturn(FAKE_USER_CODE_1);
    when(mockUserData.getUserCode()).thenReturn(FAKE_USER_CODE_1);

    presenter.disconnectUserCode();

    verify(mockView).setDisconnectUserCodeButtonEnabled(false);
    verify(mockService).disconnectUserCode(FAKE_USER_CODE_1, presenter.callbackDisconnectUserCode);
    verify(mockView).showRequiredReloadMessage();
  }

  @Test
  public void callbackDisconnectUserCodeShouldUpdateUserCodeList() {
    when(mockUserData.getGooglePlusId()).thenReturn(FAKE_GOOGLE_PLUS_ID);

    presenter.callbackDisconnectUserCode.onSuccess(null);

    verify(mockService).lookupUserDataByGooglePlusId(FAKE_GOOGLE_PLUS_ID,
        presenter.callbackLookupUserDataByGooglePlusId);
    verify(mockView).setDisconnectUserCodeButtonEnabled(true);
  }

}
