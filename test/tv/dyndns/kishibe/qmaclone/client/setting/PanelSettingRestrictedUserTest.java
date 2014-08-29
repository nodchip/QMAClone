package tv.dyndns.kishibe.qmaclone.client.setting;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;

import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class PanelSettingRestrictedUserTest {

	private static final int FAKE_USER_CODE = 12345678;
	private static final RestrictionType FAKE_RESTRICTION_TYPE = RestrictionType.PROBLEM_SUBMITTION;
	private static final String FAKE_REMOTE_ADDRESS = "1.2.3.4";
	@Mock
	private ServiceAsync mockService;
	@Mock
	private PanelSettingRestrictedUser.View mockView;
	private PanelSettingRestrictedUser presenter;

	@Before
	public void setUp() throws Exception {
		presenter = spy(new PanelSettingRestrictedUser(mockService, mockView));
		when(mockView.getUserCode()).thenReturn(FAKE_USER_CODE);
		when(mockView.getType()).thenReturn(FAKE_RESTRICTION_TYPE);
		when(mockView.getRemoteAddress()).thenReturn(FAKE_REMOTE_ADDRESS);
	}

	@Test
	public void setViewShouldHoldViewAndUpdate() {
		presenter.setView(mockView);

		assertSame(mockView, presenter.view);
		verify(presenter).update();
	}

	@Test
	public void onTypeChangedShouldUpdate() {
		presenter.onTypeChanged();

		verify(presenter).update();
	}

	@Test
	public void updateShouldGetRestricteduserCodes() {
		presenter.update();

		verify(mockService).getRestrictedUserCodes(FAKE_RESTRICTION_TYPE,
				presenter.callbackGetRestrictedUserCodes);
	}

	@Test
	public void callbackGetRestrictedUserCodesShouldGetRestrictedRemoteAddresses() {
		presenter.callbackGetRestrictedUserCodes.onSuccess(ImmutableSet.of(FAKE_USER_CODE));

		verify(mockView).setUserCodes(ImmutableSet.of(FAKE_USER_CODE));
		verify(mockService).getRestrictedRemoteAddresses(FAKE_RESTRICTION_TYPE,
				presenter.callbackGetRestrictedRemoteAddresses);
	}

	@Test
	public void callbackGetRestrictedRemoteAddressesShouldSetRemoteAddresses() {
		presenter.callbackGetRestrictedRemoteAddresses.onSuccess(ImmutableSet
				.of(FAKE_REMOTE_ADDRESS));

		verify(mockView).setRemoteAddresses(ImmutableSet.of(FAKE_REMOTE_ADDRESS));
	}

	@Test
	public void onAddUserCodeButtonShouldCallRpc() {
		presenter.onAddUserCodeButton();

		verify(mockService).addRestrictedUserCode(FAKE_USER_CODE, FAKE_RESTRICTION_TYPE,
				presenter.callbackRestrictedUser);
	}

	@Test
	public void onRemoveUserCodeButtonShouldCallRpc() {
		presenter.onRemoveUserCodeButton();

		verify(mockService).removeRestrictedUserCode(FAKE_USER_CODE, FAKE_RESTRICTION_TYPE,
				presenter.callbackRestrictedUser);
	}

	@Test
	public void onClearUserCodesButtonShouldCallRpc() {
		presenter.onClearUserCodesButton();

		verify(mockService).clearRestrictedUserCodes(FAKE_RESTRICTION_TYPE,
				presenter.callbackRestrictedUser);
	}

	@Test
	public void onAddRemoteAddressButtonShouldCallRpc() {
		presenter.onAddRemoteAddressButton();

		verify(mockService).addRestrictedRemoteAddress(FAKE_REMOTE_ADDRESS, FAKE_RESTRICTION_TYPE,
				presenter.callbackRestrictedUser);
	}

	@Test
	public void onRemoveRemoteAddressButtonShouldCallRpc() {
		presenter.onRemoveRemoteAddressButton();

		verify(mockService).removeRestrictedRemoteAddress(FAKE_REMOTE_ADDRESS,
				FAKE_RESTRICTION_TYPE, presenter.callbackRestrictedUser);
	}

	@Test
	public void onClearRemoteAddressesButtonShouldCallRpc() {
		presenter.onClearRemoteAddressesButton();

		verify(mockService).clearRestrictedRemoteAddresses(FAKE_RESTRICTION_TYPE,
				presenter.callbackRestrictedUser);
	}

	@Test
	public void callbackRestrictedUserShouldUpdate() {
		presenter.callbackRestrictedUser.onSuccess(null);

		verify(mockService).getRestrictedUserCodes(FAKE_RESTRICTION_TYPE,
				presenter.callbackGetRestrictedUserCodes);
	}

}
