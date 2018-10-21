package tv.dyndns.kishibe.qmaclone.server;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = { "/icon" })
@SuppressWarnings("serial")
public class IconUploadServlet extends DelegatingWebServlet {

  public IconUploadServlet() {
    super(Injectors.get().getInstance(IconUploadServletStub.class));
  }

}
