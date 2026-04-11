package tv.dyndns.kishibe.qmaclone.server;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = { "/*" })
public class SecurityHeadersFilter implements Filter {
  static final String X_FRAME_OPTIONS_VALUE = "DENY";
  static final String CONTENT_SECURITY_POLICY_VALUE = "frame-ancestors 'none'";

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    httpResponse.setHeader("X-Frame-Options", X_FRAME_OPTIONS_VALUE);
    httpResponse.setHeader("Content-Security-Policy", CONTENT_SECURITY_POLICY_VALUE);
    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }
}
