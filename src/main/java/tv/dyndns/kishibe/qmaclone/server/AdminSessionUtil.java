package tv.dyndns.kishibe.qmaclone.server;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

/**
 * 管理者セッション判定ユーティリティ。
 */
public class AdminSessionUtil {
  private static final String SESSION_KEY_LOGIN_USER_CODE = "loginUserCode";

  private final AdminAccessManager adminAccessManager;
  private final Database database;

  @Inject
  public AdminSessionUtil(AdminAccessManager adminAccessManager, Database database) {
    this.adminAccessManager = Preconditions.checkNotNull(adminAccessManager);
    this.database = Preconditions.checkNotNull(database);
  }

  public boolean isAdministrator(HttpServletRequest request) throws DatabaseException {
    if (request == null) {
      return false;
    }
    HttpSession session = request.getSession(false);
    if (session == null) {
      return false;
    }
    Object userCodeObject = session.getAttribute(SESSION_KEY_LOGIN_USER_CODE);
    if (!(userCodeObject instanceof Integer)) {
      return false;
    }
    return adminAccessManager.isAdministrator((Integer) userCodeObject, database);
  }
}

