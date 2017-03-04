package tv.dyndns.kishibe.qmaclone.server.websocket;

import org.eclipse.jetty.server.Server;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class WebSocketServer extends Server {
  @Inject
  public WebSocketServer(@Named("webSocketServerPort") int port, PathHandler pathHandler) {
    super(port);
    setHandler(pathHandler);
  }
}
