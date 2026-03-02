package tv.dyndns.kishibe.qmaclone.server.sns;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tv.dyndns.kishibe.qmaclone.server.AdminSessionUtil;
import tv.dyndns.kishibe.qmaclone.server.Injectors;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

/**
 * Facebook認可コールバックエンドポイント。
 */
@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/admin/facebook/auth/callback" })
public class FacebookAuthCallbackServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(FacebookAuthCallbackServlet.class.getName());

  private final FacebookAuthService authService;
  private final AdminSessionUtil adminSessionUtil;

  public FacebookAuthCallbackServlet() {
    this(Injectors.get().getInstance(FacebookAuthService.class), Injectors.get().getInstance(AdminSessionUtil.class));
  }

  FacebookAuthCallbackServlet(FacebookAuthService authService, AdminSessionUtil adminSessionUtil) {
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

    HttpSession session = request.getSession(false);
    String expectedState = session == null ? null
        : (String) session.getAttribute(FacebookAuthStartServlet.SESSION_KEY_FACEBOOK_AUTH_STATE);
    String state = request.getParameter("state");
    if (Strings.isNullOrEmpty(expectedState) || !expectedState.equals(state)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid state");
      return;
    }
    if (session != null) {
      session.removeAttribute(FacebookAuthStartServlet.SESSION_KEY_FACEBOOK_AUTH_STATE);
    }

    String code = request.getParameter("code");
    if (Strings.isNullOrEmpty(code)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing code");
      return;
    }

    String redirectUri = buildRedirectUri(request);
    boolean success = authService.refreshTokensByAuthorizationCode(code, redirectUri);
    if (success) {
      response.sendRedirect(request.getContextPath() + "/QMAClone.html?facebookAuth=success");
      return;
    }
    response.sendRedirect(request.getContextPath() + "/QMAClone.html?facebookAuth=failed");
  }

  String buildRedirectUri(HttpServletRequest request) {
    return FacebookAuthRedirectUriBuilder.build(request);
  }
}
