package tv.dyndns.kishibe.qmaclone.server;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public class DelegatingWebServlet extends HttpServlet {

  private final HttpServlet stub;

  protected DelegatingWebServlet(HttpServlet stub) {
    this.stub = Preconditions.checkNotNull(stub);
  }

  @Override
  public void destroy() {
    stub.destroy();
  }

  @Override
  public ServletConfig getServletConfig() {
    return stub.getServletConfig();
  }

  @Override
  public String getServletInfo() {
    return stub.getServletInfo();
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    stub.init(config);
  }

  @Override
  public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    stub.service(req, res);
  }

}
