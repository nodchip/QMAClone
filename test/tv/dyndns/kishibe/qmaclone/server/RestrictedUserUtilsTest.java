package tv.dyndns.kishibe.qmaclone.server;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.database.Database;

import com.google.common.collect.ImmutableSet;
import com.google.gwt.thirdparty.guava.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class RestrictedUserUtilsTest {

  private static final int FAKE_USER_CODE = 12345678;
  private static final String FAKE_REMOTE_ADDRESS = "1.2.3.4";
  private static final RestrictionType FAKE_RESTRICTION_TYPE = RestrictionType.BBS;
  @Mock
  private Database mockDatabase;
  private RestrictedUserUtils restrictedUserUtils;

  @Before
  public void setUp() throws Exception {
    restrictedUserUtils = new RestrictedUserUtils(mockDatabase);
  }

  @Test
  public void checkAndUpdateRestrictedShouldUserReturnTrueIfUserCodeMatch() throws Exception {
    when(mockDatabase.getRestrictedUserCodes(FAKE_RESTRICTION_TYPE)).thenReturn(
        ImmutableSet.of(FAKE_USER_CODE));
    when(mockDatabase.getRestrictedRemoteAddresses(FAKE_RESTRICTION_TYPE)).thenReturn(
        Sets.<String> newHashSet());

    restrictedUserUtils.checkAndUpdateRestrictedUser(FAKE_USER_CODE, FAKE_REMOTE_ADDRESS,
        FAKE_RESTRICTION_TYPE);

    verify(mockDatabase, never()).addRestrictedUserCode(FAKE_USER_CODE, FAKE_RESTRICTION_TYPE);
    verify(mockDatabase).addRestrictedRemoteAddress(FAKE_REMOTE_ADDRESS, FAKE_RESTRICTION_TYPE);
  }

  @Test
  public void checkAndUpdateRestrictedUserShouldReturnTrueIfRemoteAddressMatch() throws Exception {
    when(mockDatabase.getRestrictedUserCodes(FAKE_RESTRICTION_TYPE)).thenReturn(
        ImmutableSet.<Integer> of());
    when(mockDatabase.getRestrictedRemoteAddresses(FAKE_RESTRICTION_TYPE)).thenReturn(
        ImmutableSet.of(FAKE_REMOTE_ADDRESS));

    restrictedUserUtils.checkAndUpdateRestrictedUser(FAKE_USER_CODE, FAKE_REMOTE_ADDRESS,
        FAKE_RESTRICTION_TYPE);

    verify(mockDatabase).addRestrictedUserCode(FAKE_USER_CODE, FAKE_RESTRICTION_TYPE);
    verify(mockDatabase, never()).addRestrictedRemoteAddress(FAKE_REMOTE_ADDRESS,
        FAKE_RESTRICTION_TYPE);
  }

  @Test
  public void checkAndUpdateRestrictedUserShouldNotSaveLocalHost() throws Exception {
    when(mockDatabase.getRestrictedUserCodes(FAKE_RESTRICTION_TYPE)).thenReturn(
        ImmutableSet.of(FAKE_USER_CODE));
    when(mockDatabase.getRestrictedRemoteAddresses(FAKE_RESTRICTION_TYPE)).thenReturn(
        ImmutableSet.of(FAKE_REMOTE_ADDRESS));

    restrictedUserUtils.checkAndUpdateRestrictedUser(FAKE_USER_CODE, "127.0.0.1",
        FAKE_RESTRICTION_TYPE);

    verify(mockDatabase, never()).addRestrictedUserCode(FAKE_USER_CODE, FAKE_RESTRICTION_TYPE);
    verify(mockDatabase, never()).addRestrictedRemoteAddress(anyString(),
        any(RestrictionType.class));
  }

}
