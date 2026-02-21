package tv.dyndns.kishibe.qmaclone.server;

import java.io.IOException;
import java.util.Calendar;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.impl.cookie.DateUtils;

@WebFilter(urlPatterns = { "/*" })
public class PageExpiryFilter implements Filter {

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) arg0;
    HttpServletResponse resp = (HttpServletResponse) arg1;
    String path = req.getServletPath();
    if (path.endsWith(".nocache.js")) {
      resp.setHeader("Pragma", "no-cache");
      resp.setHeader("Cache-Control", "no-cache");
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.YEAR, -1);
      resp.setHeader("Expires", DateUtils.formatDate(calendar.getTime()));

    } else if (path.endsWith(".gif") || path.endsWith(".png") || path.endsWith(".css")
        || path.endsWith(".js")) {
      resp.setHeader("Cache-Control", "public, max-age=" + 30 * 24 * 60 * 60);
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, 30);
      resp.setHeader("Expires", DateUtils.formatDate(calendar.getTime()));
    }

    arg2.doFilter(arg0, arg1);
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
  }

}
