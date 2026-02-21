package tv.dyndns.kishibe.qmaclone.server.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.jupiter.api.Test;

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

  @Test
  public void migratePlayerSoundColumnsShouldAddColumnsWhenMissing() throws Exception {
    RecordingQueryRunner runner = new RecordingQueryRunner();
    runner.setColumnCount("SOUND_MASTER_VOLUME", 0L);
    runner.setColumnCount("SOUND_UI_VOLUME", 0L);
    runner.setColumnCount("SOUND_GAMEPLAY_VOLUME", 0L);
    runner.setColumnCount("SOUND_RESULT_VOLUME", 0L);
    runner.setColumnCount("SOUND_MUTED", 0L);
    runner.setColumnCount("SOUND_SETTINGS_VERSION", 0L);

    DatabaseSchemaMigrator migrator = new DatabaseSchemaMigrator(runner, "qmaclone");
    migrator.migratePlayerSoundColumns();

    assertEquals(6, runner.getUpdates().size());
    assertTrue(
        runner.getUpdates().contains("ALTER TABLE player ADD COLUMN SOUND_MASTER_VOLUME DOUBLE NOT NULL DEFAULT 1.0"));
    assertTrue(
        runner.getUpdates().contains("ALTER TABLE player ADD COLUMN SOUND_UI_VOLUME DOUBLE NOT NULL DEFAULT 1.0"));
    assertTrue(
        runner.getUpdates().contains("ALTER TABLE player ADD COLUMN SOUND_GAMEPLAY_VOLUME DOUBLE NOT NULL DEFAULT 1.0"));
    assertTrue(
        runner.getUpdates().contains("ALTER TABLE player ADD COLUMN SOUND_RESULT_VOLUME DOUBLE NOT NULL DEFAULT 1.0"));
    assertTrue(
        runner.getUpdates().contains("ALTER TABLE player ADD COLUMN SOUND_MUTED BOOLEAN NOT NULL DEFAULT FALSE"));
    assertTrue(
        runner.getUpdates().contains("ALTER TABLE player ADD COLUMN SOUND_SETTINGS_VERSION INT NOT NULL DEFAULT 1"));
  }

  @Test
  public void migratePlayerSoundColumnsShouldSkipWhenColumnsAlreadyExist() throws Exception {
    RecordingQueryRunner runner = new RecordingQueryRunner();
    runner.setColumnCount("SOUND_MASTER_VOLUME", 1L);
    runner.setColumnCount("SOUND_UI_VOLUME", 1L);
    runner.setColumnCount("SOUND_GAMEPLAY_VOLUME", 1L);
    runner.setColumnCount("SOUND_RESULT_VOLUME", 1L);
    runner.setColumnCount("SOUND_MUTED", 1L);
    runner.setColumnCount("SOUND_SETTINGS_VERSION", 1L);

    DatabaseSchemaMigrator migrator = new DatabaseSchemaMigrator(runner, "qmaclone");
    migrator.migratePlayerSoundColumns();

    assertTrue(runner.getUpdates().isEmpty());
  }

  /**
   * QueryRunner の呼び出しを記録するテストダブル。
   */
  private static class RecordingQueryRunner extends QueryRunner {
    private final Map<String, Long> columnCounts = new HashMap<String, Long>();
    private long indexCount;
    private final List<String> updates = new ArrayList<>();

    void setColumnCount(String columnName, long count) {
      columnCounts.put(columnName, count);
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
        return (T) Long.valueOf(columnCounts.containsKey(columnName) ? columnCounts.get(columnName) : 0L);
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
