package tv.dyndns.kishibe.qmaclone.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.database.Database;

import com.google.common.collect.ImmutableSet;
import com.google.gwt.thirdparty.guava.common.collect.Sets;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RestrictedUserUtilsTest {

  private static final int FAKE_USER_CODE = 12345678;
  private static final String FAKE_REMOTE_ADDRESS = "1.2.3.4";
  private static final RestrictionType FAKE_RESTRICTION_TYPE = RestrictionType.BBS;
  @Mock
  private Database mockDatabase;
  private RestrictedUserUtils restrictedUserUtils;

  @BeforeEach
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
