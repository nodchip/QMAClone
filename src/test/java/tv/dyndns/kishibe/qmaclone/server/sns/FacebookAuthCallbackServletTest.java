package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tv.dyndns.kishibe.qmaclone.server.AdminSessionUtil;

/**
 * {@link FacebookAuthCallbackServlet} のテスト。
 */
public class FacebookAuthCallbackServletTest {
  @Test
  public void doGetShouldRejectWhenStateDoesNotMatch() throws Exception {
    FacebookAuthService authService = mock(FacebookAuthService.class);
    AdminSessionUtil adminSessionUtil = mock(AdminSessionUtil.class);
    FacebookAuthCallbackServlet servlet = new FacebookAuthCallbackServlet(authService, adminSessionUtil);

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpSession session = mock(HttpSession.class);
    when(adminSessionUtil.isAdministrator(request)).thenReturn(true);
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute(FacebookAuthStartServlet.SESSION_KEY_FACEBOOK_AUTH_STATE)).thenReturn("expected");
    when(request.getParameter("state")).thenReturn("actual");

    servlet.doGet(request, response);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid state");
    verify(authService, never()).refreshTokensByAuthorizationCode(org.mockito.ArgumentMatchers.anyString(),
        org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  public void doGetShouldRefreshTokenWhenStateAndCodeAreValid() throws Exception {
    FacebookAuthService authService = mock(FacebookAuthService.class);
    AdminSessionUtil adminSessionUtil = mock(AdminSessionUtil.class);
    FacebookAuthCallbackServlet servlet = new FacebookAuthCallbackServlet(authService, adminSessionUtil);

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpSession session = mock(HttpSession.class);
    when(adminSessionUtil.isAdministrator(request)).thenReturn(true);
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute(FacebookAuthStartServlet.SESSION_KEY_FACEBOOK_AUTH_STATE)).thenReturn("state");
    when(request.getParameter("state")).thenReturn("state");
    when(request.getParameter("code")).thenReturn("code");
    when(request.getScheme()).thenReturn("https");
    when(request.getServerName()).thenReturn("kishibe.dyndns.tv");
    when(request.getServerPort()).thenReturn(443);
    when(request.getContextPath()).thenReturn("/QMAClone");
    when(authService.refreshTokensByAuthorizationCode("code",
        "https://kishibe.dyndns.tv/QMAClone/admin/facebook/auth/callback")).thenReturn(true);

    servlet.doGet(request, response);

    verify(response).sendRedirect("/QMAClone/QMAClone.html?facebookAuth=success");
    verify(session, times(1)).removeAttribute(FacebookAuthStartServlet.SESSION_KEY_FACEBOOK_AUTH_STATE);
  }
}
