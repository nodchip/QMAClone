package tv.dyndns.kishibe.qmaclone.server;

import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/image/*" })
public class ImageProxyServlet extends DelegatingWebServlet {

  public ImageProxyServlet() {
    super(Injectors.get().getInstance(ImageProxyServletStub.class));
  }

}
