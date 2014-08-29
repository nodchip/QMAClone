package tv.dyndns.kishibe.qmaclone.server.database;

import static com.google.inject.Scopes.SINGLETON;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DatabaseModule extends AbstractModule {

  private static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
  private static final String USERNAME = "qmaclone";
  private static final String PASSWORD = "qmaclone";
  private static final String URL = "jdbc:mysql://gust/qmaclone?user=qmaclone&password=qmaclone&autoReconnects=true&userUnicode=true&characterEncoding=UTF-8";
  private static final String VALIDATION_QUERY = "SELECT 1";

  @Override
  protected void configure() {
    bind(CachedDatabase.class).in(SINGLETON);
    bind(Database.class).to(CachedDatabase.class).in(SINGLETON);
  }

  @Provides
  @Singleton
  private DataSource provideDataSource() {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(DRIVER_CLASS_NAME);
    dataSource.setUsername(USERNAME);
    dataSource.setPassword(PASSWORD);
    dataSource.setUrl(URL);
    dataSource.setValidationQuery(VALIDATION_QUERY);
    return dataSource;
  }

  @Provides
  @Singleton
  private QueryRunner provideQueryRunner(DataSource dataSource) {
    return new QueryRunner(dataSource);
  }

}
