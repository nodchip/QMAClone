package tv.dyndns.kishibe.qmaclone.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SecurityHeadersFilterTest {
  @Test
  public void doFilterShouldSetClickjackingDefenseHeaders() throws IOException, ServletException {
    SecurityHeadersFilter filter = new SecurityHeadersFilter();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(request.getServletPath()).thenReturn("/QMAClone.html");

    filter.doFilter(request, response, chain);

    verify(response).setHeader("X-Frame-Options", "DENY");
    verify(response).setHeader("Content-Security-Policy", "frame-ancestors 'none'");
    verify(chain).doFilter(request, response);
  }
}
