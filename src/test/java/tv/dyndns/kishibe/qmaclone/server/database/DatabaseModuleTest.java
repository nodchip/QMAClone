package tv.dyndns.kishibe.qmaclone.server.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * DatabaseModuleの設定読み込みテスト。
 */
public class DatabaseModuleTest {
  private final Map<String, String> originalProperties = new HashMap<>();

  @BeforeEach
  public void setUp() {
    backupProperty(DatabaseModule.PROPERTY_DB_URL);
    backupProperty(DatabaseModule.PROPERTY_DB_USERNAME);
    backupProperty(DatabaseModule.PROPERTY_DB_PASSWORD);
    backupProperty(DatabaseModule.PROPERTY_ADMIN_CONFIG_PATH);
  }

  @AfterEach
  public void tearDown() {
    restoreProperty(DatabaseModule.PROPERTY_DB_URL);
    restoreProperty(DatabaseModule.PROPERTY_DB_USERNAME);
    restoreProperty(DatabaseModule.PROPERTY_DB_PASSWORD);
    restoreProperty(DatabaseModule.PROPERTY_ADMIN_CONFIG_PATH);
  }

  /**
   * システムプロパティでDataSourceが構成される。
   */
  @Test
  public void provideDataSourceShouldUseSystemProperties() throws Exception {
    System.setProperty(DatabaseModule.PROPERTY_DB_URL, "jdbc:mysql://localhost/test_db");
    System.setProperty(DatabaseModule.PROPERTY_DB_USERNAME, "test_user");
    System.setProperty(DatabaseModule.PROPERTY_DB_PASSWORD, "test_password");

    DatabaseModule module = new DatabaseModule();
    BasicDataSource dataSource = (BasicDataSource) module.provideDataSource();

    assertEquals("jdbc:mysql://localhost/test_db", dataSource.getUrl());
    assertEquals("test_user", dataSource.getUsername());
    assertEquals("test_password", dataSource.getPassword());
    dataSource.close();
  }

  /**
   * 必須プロパティ未設定時は例外となる。
   */
  @Test
  public void provideDataSourceShouldFailWhenRequiredPropertyMissing() {
    System.clearProperty(DatabaseModule.PROPERTY_DB_URL);
    System.clearProperty(DatabaseModule.PROPERTY_DB_USERNAME);
    System.clearProperty(DatabaseModule.PROPERTY_DB_PASSWORD);
    System.setProperty(DatabaseModule.PROPERTY_ADMIN_CONFIG_PATH, "invalid-path.properties");

    DatabaseModule module = new DatabaseModule();
    IllegalStateException exception = assertThrows(IllegalStateException.class, module::provideDataSource);

    assertTrue(exception.getMessage().contains(DatabaseModule.PROPERTY_DB_URL));
  }

  /**
   * 設定ファイルからDataSourceが構成される。
   */
  @Test
  public void provideDataSourceShouldUseConfigFile() throws Exception {
    System.clearProperty(DatabaseModule.PROPERTY_DB_URL);
    System.clearProperty(DatabaseModule.PROPERTY_DB_USERNAME);
    System.clearProperty(DatabaseModule.PROPERTY_DB_PASSWORD);
    File tempFile = File.createTempFile("qmaclone-admin", ".properties");
    tempFile.deleteOnExit();
    Properties properties = new Properties();
    properties.setProperty(DatabaseModule.PROPERTY_DB_URL, "jdbc:mysql://localhost/file_db");
    properties.setProperty(DatabaseModule.PROPERTY_DB_USERNAME, "file_user");
    properties.setProperty(DatabaseModule.PROPERTY_DB_PASSWORD, "file_password");
    try (java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
      properties.store(outputStream, "test");
    }
    System.setProperty(DatabaseModule.PROPERTY_ADMIN_CONFIG_PATH, tempFile.getAbsolutePath());

    DatabaseModule module = new DatabaseModule();
    BasicDataSource dataSource = (BasicDataSource) module.provideDataSource();

    assertEquals("jdbc:mysql://localhost/file_db", dataSource.getUrl());
    assertEquals("file_user", dataSource.getUsername());
    assertEquals("file_password", dataSource.getPassword());
    dataSource.close();
  }

  /**
   * 退避対象のシステムプロパティを保存する。
   */
  private void backupProperty(String key) {
    originalProperties.put(key, System.getProperty(key));
  }

  /**
   * システムプロパティを元の状態に戻す。
   */
  private void restoreProperty(String key) {
    String value = originalProperties.get(key);
    if (value == null) {
      System.clearProperty(key);
    } else {
      System.setProperty(key, value);
    }
  }
}
