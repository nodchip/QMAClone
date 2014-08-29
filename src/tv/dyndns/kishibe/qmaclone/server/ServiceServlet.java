package tv.dyndns.kishibe.qmaclone.server;

import javax.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/tv.dyndns.kishibe.qmaclone.QMAClone/service" })
public class ServiceServlet extends DelegatingWebServlet {

  public ServiceServlet() {
    super(Injectors.get().getInstance(ServiceServletStub.class));
  }

}
