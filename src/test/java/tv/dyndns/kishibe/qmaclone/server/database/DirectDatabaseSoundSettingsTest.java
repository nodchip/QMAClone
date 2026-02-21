package tv.dyndns.kishibe.qmaclone.server.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;
import tv.dyndns.kishibe.qmaclone.server.testing.GuiceInjectionExtension;

/**
 * 音量設定の永続化テスト。
 */
@ExtendWith(GuiceInjectionExtension.class)
public class DirectDatabaseSoundSettingsTest {

  private static final int USER_CODE = 82123456;

  @Inject
  private CachedDatabase database;

  @BeforeEach
  public void setUp() {
    database.clearCache();
  }

  /**
   * load/save/loadで音量設定が保持される。
   */
  @Test
  public void shouldPersistSoundSettingsInUserData() throws Exception {
    PacketUserData expected = TestDataProvider.getUserData();
    expected.userCode = USER_CODE;
    expected.soundMasterVolume = 0.8;
    expected.soundUiVolume = 0.7;
    expected.soundGameplayVolume = 0.6;
    expected.soundResultVolume = 0.9;
    expected.soundMuted = false;
    expected.soundSettingsVersion = 1;

    database.setUserData(expected);
    PacketUserData actual = database.getUserData(USER_CODE);

    assertNotNull(actual);
    assertEquals(0.8, actual.soundMasterVolume, 0.0001);
    assertEquals(0.7, actual.soundUiVolume, 0.0001);
    assertEquals(0.6, actual.soundGameplayVolume, 0.0001);
    assertEquals(0.9, actual.soundResultVolume, 0.0001);
    assertFalse(actual.soundMuted);
    assertEquals(1, actual.soundSettingsVersion);
  }
}
