package tv.dyndns.kishibe.qmaclone.server.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.Test;

/**
 * DatabaseSchemaMigrator の単体テスト。
 */
public class DatabaseSchemaMigratorTest {

  @Test
  public void migratePlayerAuthColumnsShouldAddColumnsAndIndexWhenMissing() throws Exception {
    RecordingQueryRunner runner = new RecordingQueryRunner();
    runner.setColumnCount("AUTH_PROVIDER", 0L);
    runner.setColumnCount("AUTH_SUB", 0L);
    runner.setIndexCount(0L);

    DatabaseSchemaMigrator migrator = new DatabaseSchemaMigrator(runner, "qmaclone");
    migrator.migratePlayerAuthColumns();

    assertEquals(3, runner.getUpdates().size());
    assertTrue(runner.getUpdates().contains("ALTER TABLE player ADD COLUMN AUTH_PROVIDER VARCHAR(64) NULL"));
    assertTrue(runner.getUpdates().contains("ALTER TABLE player ADD COLUMN AUTH_SUB VARCHAR(255) NULL"));
    assertTrue(
        runner
            .getUpdates()
            .contains(
                "CREATE UNIQUE INDEX UQ_PLAYER_AUTH_PROVIDER_SUB ON player (AUTH_PROVIDER, AUTH_SUB)"));
  }

  @Test
  public void migratePlayerAuthColumnsShouldSkipWhenColumnsAndIndexAlreadyExist() throws Exception {
    RecordingQueryRunner runner = new RecordingQueryRunner();
    runner.setColumnCount("AUTH_PROVIDER", 1L);
    runner.setColumnCount("AUTH_SUB", 1L);
    runner.setIndexCount(1L);

    DatabaseSchemaMigrator migrator = new DatabaseSchemaMigrator(runner, "qmaclone");
    migrator.migratePlayerAuthColumns();

    assertTrue(runner.getUpdates().isEmpty());
  }

  /**
   * QueryRunner の呼び出しを記録するテストダブル。
   */
  private static class RecordingQueryRunner extends QueryRunner {
    private long authProviderColumnCount;
    private long authSubColumnCount;
    private long indexCount;
    private final List<String> updates = new ArrayList<>();

    void setColumnCount(String columnName, long count) {
      if ("AUTH_PROVIDER".equals(columnName)) {
        authProviderColumnCount = count;
      } else if ("AUTH_SUB".equals(columnName)) {
        authSubColumnCount = count;
      }
    }

    void setIndexCount(long count) {
      indexCount = count;
    }

    List<String> getUpdates() {
      return updates;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
      if (sql.contains("INFORMATION_SCHEMA.COLUMNS")) {
        String columnName = (String) params[2];
        if ("AUTH_PROVIDER".equals(columnName)) {
          return (T) Long.valueOf(authProviderColumnCount);
        }
        if ("AUTH_SUB".equals(columnName)) {
          return (T) Long.valueOf(authSubColumnCount);
        }
      }
      if (sql.contains("INFORMATION_SCHEMA.STATISTICS")) {
        return (T) Long.valueOf(indexCount);
      }
      if ("SELECT DATABASE()".equals(sql) && rsh instanceof ScalarHandler) {
        return (T) "qmaclone";
      }
      throw new SQLException("Unexpected query: " + sql);
    }

    @Override
    public int update(String sql, Object... params) throws SQLException {
      updates.add(sql);
      return 1;
    }

    @Override
    public int update(String sql) throws SQLException {
      updates.add(sql);
      return 1;
    }

    @Override
    public int update(Connection conn, String sql, Object... params) throws SQLException {
      updates.add(sql);
      return 1;
    }
  }
}
