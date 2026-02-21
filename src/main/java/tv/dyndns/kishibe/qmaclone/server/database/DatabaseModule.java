package tv.dyndns.kishibe.qmaclone.server.database;

import static com.google.inject.Scopes.SINGLETON;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DatabaseModule extends AbstractModule {
  private static final Logger logger = Logger.getLogger(DatabaseModule.class.getName());

  static final String PROPERTY_DB_URL = "qmaclone.db.url";
  static final String PROPERTY_DB_USERNAME = "qmaclone.db.username";
  static final String PROPERTY_DB_PASSWORD = "qmaclone.db.password";
  static final String PROPERTY_ADMIN_CONFIG_PATH = "qmaclone.admin.config";
  static final String DEFAULT_ADMIN_CONFIG_PATH = "ops/config/live/tomcat10/qmaclone-admin.properties";
  private static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
  private static final String VALIDATION_QUERY = "SELECT 1";

  @Override
  protected void configure() {
    bind(CachedDatabase.class).in(SINGLETON);
    bind(Database.class).to(CachedDatabase.class).in(SINGLETON);
  }

  @Provides
  @Singleton
  DataSource provideDataSource() {
    Properties configProperties = loadConfigProperties();
    String url = getRequiredProperty(PROPERTY_DB_URL, configProperties);
    String username = getRequiredProperty(PROPERTY_DB_USERNAME, configProperties);
    String password = getRequiredProperty(PROPERTY_DB_PASSWORD, configProperties);

    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(DRIVER_CLASS_NAME);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setUrl(url);
    dataSource.setValidationQuery(VALIDATION_QUERY);
    return dataSource;
  }

  /**
   * 必須システムプロパティを取得する。
   */
  static String getRequiredProperty(String key, Properties configProperties) {
    String value = System.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      value = configProperties.getProperty(key);
    }
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalStateException("必須設定が未設定です: " + key);
    }
    return value;
  }

  /**
   * 管理者設定ファイルを読み込む。
   */
  static Properties loadConfigProperties() {
    Properties properties = new Properties();
    String configPath = System.getProperty(PROPERTY_ADMIN_CONFIG_PATH, DEFAULT_ADMIN_CONFIG_PATH);
    File file = new File(configPath);
    if (!file.exists() || !file.isFile()) {
      logger.log(Level.WARNING, "設定ファイルが見つかりません: {0}", configPath);
      return properties;
    }
    try (FileInputStream inputStream = new FileInputStream(file)) {
      properties.load(inputStream);
      return properties;
    } catch (IOException e) {
      logger.log(Level.WARNING, "設定ファイルの読み込みに失敗しました: " + configPath, e);
      return properties;
    }
  }

  @Provides
  @Singleton
  private QueryRunner provideQueryRunner(DataSource dataSource) {
    return new QueryRunner(dataSource);
  }

}
