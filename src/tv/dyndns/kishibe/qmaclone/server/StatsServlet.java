package tv.dyndns.kishibe.qmaclone.server;

import javax.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/stats" })
public class StatsServlet extends DelegatingWebServlet {
  public StatsServlet() {
    super(Injectors.get().getInstance(StatsServletStub.class));
  }
}
