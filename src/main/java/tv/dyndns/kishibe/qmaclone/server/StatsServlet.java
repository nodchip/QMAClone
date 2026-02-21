package tv.dyndns.kishibe.qmaclone.server;

import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/stats" })
public class StatsServlet extends DelegatingWebServlet {
  public StatsServlet() {
    super(Injectors.get().getInstance(StatsServletStub.class));
  }
}
