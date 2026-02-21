package tv.dyndns.kishibe.qmaclone.server;

import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/rss.xml" })
public class RssServlet extends DelegatingWebServlet {

  public RssServlet() {
    super(Injectors.get().getInstance(RssServletStub.class));
  }

}
