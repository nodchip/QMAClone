package tv.dyndns.kishibe.qmaclone.server.database;

import java.sql.SQLException;

import javax.annotation.Nullable;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * 起動時に player テーブルの認証関連カラムを補正する。
 */
public class DatabaseSchemaMigrator {
  private static final String PLAYER_TABLE = "player";
  private static final String AUTH_PROVIDER_COLUMN = "AUTH_PROVIDER";
  private static final String AUTH_SUB_COLUMN = "AUTH_SUB";
  private static final String AUTH_INDEX_NAME = "UQ_PLAYER_AUTH_PROVIDER_SUB";
  private static final String SOUND_MASTER_VOLUME_COLUMN = "SOUND_MASTER_VOLUME";
  private static final String SOUND_UI_VOLUME_COLUMN = "SOUND_UI_VOLUME";
  private static final String SOUND_GAMEPLAY_VOLUME_COLUMN = "SOUND_GAMEPLAY_VOLUME";
  private static final String SOUND_RESULT_VOLUME_COLUMN = "SOUND_RESULT_VOLUME";
  private static final String SOUND_MUTED_COLUMN = "SOUND_MUTED";
  private static final String SOUND_SETTINGS_VERSION_COLUMN = "SOUND_SETTINGS_VERSION";
  private final QueryRunner runner;
  private final String schemaName;

  public DatabaseSchemaMigrator(QueryRunner runner) throws DatabaseException {
    this(runner, null);
  }

  /**
   * テスト用コンストラクタ。
   */
  DatabaseSchemaMigrator(QueryRunner runner, @Nullable String schemaName) {
    this.runner = Preconditions.checkNotNull(runner);
    this.schemaName = schemaName;
  }

  /**
   * AUTH_PROVIDER/AUTH_SUB 列と一意インデックスを不足時のみ追加する。
   */
  public void migratePlayerAuthColumns() throws DatabaseException {
    String activeSchema = Strings.isNullOrEmpty(schemaName) ? loadSchemaName() : schemaName;
    if (Strings.isNullOrEmpty(activeSchema)) {
      return;
    }

    ensureColumnExists(activeSchema, AUTH_PROVIDER_COLUMN, "VARCHAR(64) NULL");
    ensureColumnExists(activeSchema, AUTH_SUB_COLUMN, "VARCHAR(255) NULL");
    ensureUniqueIndexExists(activeSchema);
  }

  /**
   * SOUND_* 列を不足時のみ追加する。
   */
  public void migratePlayerSoundColumns() throws DatabaseException {
    String activeSchema = Strings.isNullOrEmpty(schemaName) ? loadSchemaName() : schemaName;
    if (Strings.isNullOrEmpty(activeSchema)) {
      return;
    }

    ensureColumnExists(activeSchema, SOUND_MASTER_VOLUME_COLUMN, "DOUBLE NOT NULL DEFAULT 1.0");
    ensureColumnExists(activeSchema, SOUND_UI_VOLUME_COLUMN, "DOUBLE NOT NULL DEFAULT 1.0");
    ensureColumnExists(activeSchema, SOUND_GAMEPLAY_VOLUME_COLUMN, "DOUBLE NOT NULL DEFAULT 1.0");
    ensureColumnExists(activeSchema, SOUND_RESULT_VOLUME_COLUMN, "DOUBLE NOT NULL DEFAULT 1.0");
    ensureColumnExists(activeSchema, SOUND_MUTED_COLUMN, "BOOLEAN NOT NULL DEFAULT FALSE");
    ensureColumnExists(activeSchema, SOUND_SETTINGS_VERSION_COLUMN, "INT NOT NULL DEFAULT 1");
  }

  private String loadSchemaName() throws DatabaseException {
    try {
      return runner.query("SELECT DATABASE()", new ScalarHandler<String>());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private void ensureColumnExists(String schema, String columnName, String columnDefinition)
      throws DatabaseException {
    try {
      long count =
          runner.query(
              "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?",
              new ScalarHandler<Long>(),
              schema,
              PLAYER_TABLE,
              columnName);
      if (count != 0) {
        return;
      }

      runner.update(
          "ALTER TABLE " + PLAYER_TABLE + " ADD COLUMN " + columnName + " " + columnDefinition);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private void ensureUniqueIndexExists(String schema) throws DatabaseException {
    try {
      long count =
          runner.query(
              "SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND INDEX_NAME = ?",
              new ScalarHandler<Long>(),
              schema,
              PLAYER_TABLE,
              AUTH_INDEX_NAME);
      if (count != 0) {
        return;
      }

      runner.update(
          "CREATE UNIQUE INDEX "
              + AUTH_INDEX_NAME
              + " ON "
              + PLAYER_TABLE
              + " ("
              + AUTH_PROVIDER_COLUMN
              + ", "
              + AUTH_SUB_COLUMN
              + ")");
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
