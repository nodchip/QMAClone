package tv.dyndns.kishibe.qmaclone.server.sns;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tv.dyndns.kishibe.qmaclone.server.AdminSessionUtil;
import tv.dyndns.kishibe.qmaclone.server.Injectors;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

/**
 * Facebook認可開始エンドポイント。
 */
@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/admin/facebook/auth/start" })
public class FacebookAuthStartServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(FacebookAuthStartServlet.class.getName());
  static final String SESSION_KEY_FACEBOOK_AUTH_STATE = "facebookAuthState";

  private final FacebookAuthService authService;
  private final AdminSessionUtil adminSessionUtil;

  public FacebookAuthStartServlet() {
    this(Injectors.get().getInstance(FacebookAuthService.class), Injectors.get().getInstance(AdminSessionUtil.class));
  }

  FacebookAuthStartServlet(FacebookAuthService authService, AdminSessionUtil adminSessionUtil) {
    this.authService = Preconditions.checkNotNull(authService);
    this.adminSessionUtil = Preconditions.checkNotNull(adminSessionUtil);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      if (!adminSessionUtil.isAdministrator(request)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "管理者判定に失敗しました", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    String state = UUID.randomUUID().toString();
    request.getSession(true).setAttribute(SESSION_KEY_FACEBOOK_AUTH_STATE, state);
    String redirectUri = buildRedirectUri(request);
    String authorizeUrl = authService.buildAuthorizationUrl(redirectUri, state);
    if (authorizeUrl == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    response.sendRedirect(authorizeUrl);
  }

  String buildRedirectUri(HttpServletRequest request) {
    return FacebookAuthRedirectUriBuilder.build(request);
  }
}
