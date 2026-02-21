package tv.dyndns.kishibe.qmaclone.server;

import jakarta.servlet.annotation.WebServlet;

import com.google.gwt.logging.server.RemoteLoggingServiceImpl;

/**
 * GWTクライアントのリモートログ受け取り用サーブレット。
 */
@WebServlet(urlPatterns = { "/tv.dyndns.kishibe.qmaclone.QMAClone/remote_logging", "/remote_logging" })
@SuppressWarnings("serial")
public class RemoteLoggingServlet extends RemoteLoggingServiceImpl {
}
