package tv.dyndns.kishibe.qmaclone.server.service;

import javax.servlet.annotation.WebServlet;

import tv.dyndns.kishibe.qmaclone.server.DelegatingWebServlet;
import tv.dyndns.kishibe.qmaclone.server.Injectors;

@WebServlet(urlPatterns = { "/tv.dyndns.kishibe.qmaclone.QMAClone/link" })
@SuppressWarnings("serial")
public class LinkServlet extends DelegatingWebServlet {

  public LinkServlet() {
    super(Injectors.get().getInstance(LinkServletStub.class));
  }

}
