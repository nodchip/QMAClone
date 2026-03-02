package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

/**
 * {@link FacebookAuthRedirectUriBuilder} のテスト。
 */
public class FacebookAuthRedirectUriBuilderTest {
  @Test
  public void buildShouldUseForwardedHeaders() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
    when(request.getHeader("X-Forwarded-Host")).thenReturn("kishibe.dyndns.tv");
    when(request.getHeader("X-Forwarded-Port")).thenReturn("443");
    when(request.getContextPath()).thenReturn("/QMAClone");

    String redirectUri = FacebookAuthRedirectUriBuilder.build(request);

    assertEquals("https://kishibe.dyndns.tv/QMAClone/admin/facebook/auth/callback", redirectUri);
  }

  @Test
  public void buildShouldFallbackToRequestValuesWhenForwardedHeadersAreMissing() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);
    when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
    when(request.getHeader("X-Forwarded-Port")).thenReturn(null);
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("127.0.0.1");
    when(request.getServerPort()).thenReturn(8080);
    when(request.getContextPath()).thenReturn("/QMAClone");

    String redirectUri = FacebookAuthRedirectUriBuilder.build(request);

    assertEquals("http://127.0.0.1:8080/QMAClone/admin/facebook/auth/callback", redirectUri);
  }
}
