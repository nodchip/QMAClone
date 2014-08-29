package tv.dyndns.kishibe.qmaclone.server;

import javax.servlet.annotation.WebServlet;

import com.google.gwt.logging.server.RemoteLoggingServiceImpl;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/tv.dyndns.kishibe.qmaclone.QMAClone/remote_logging" })
public class RemoteLoggingServlet extends RemoteLoggingServiceImpl {
}
