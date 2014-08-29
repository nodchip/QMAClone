package tv.dyndns.kishibe.qmaclone.server;

import javax.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/rss.xml" })
public class RssServlet extends DelegatingWebServlet {

  public RssServlet() {
    super(Injectors.get().getInstance(RssServletStub.class));
  }

}
