package tv.dyndns.kishibe.qmaclone.client.setting;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * SettingModuleのバインド内容を検証するテスト。
 */
public class SettingModuleTest {

  /**
   * 外部アカウント接続はGoogle向け実装を返す。
   */
  @Test
  public void provideExternalAccountConnectorShouldReturnGoogleConnector() {
    SettingModule module = new SettingModule();

    ExternalAccountConnector connector = module.provideExternalAccountConnector();

    assertTrue(connector instanceof GoogleExternalAccountConnector);
  }
}
