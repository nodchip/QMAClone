package tv.dyndns.kishibe.qmaclone.server.websocket;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketReadyForGame;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;
import tv.dyndns.kishibe.qmaclone.server.ChatManager;
import tv.dyndns.kishibe.qmaclone.server.GameManager;
import tv.dyndns.kishibe.qmaclone.server.ServerStatusManager;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class PathHandler extends WebSocketHandler {

  private final GameManager gameManager;
  private final ChatManager chatManager;
  private final ServerStatusManager serverStatusManager;

  @Inject
  public PathHandler(GameManager gameManager, ChatManager chatManager,
      ServerStatusManager serverStatusManager) {
    this.gameManager = Preconditions.checkNotNull(gameManager);
    this.chatManager = Preconditions.checkNotNull(chatManager);
    this.serverStatusManager = Preconditions.checkNotNull(serverStatusManager);
  }

  @Override
  public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
    String pathInfo = request.getPathInfo();

    if (pathInfo.contains(PacketChatMessages.class.getName())) {
      return chatManager.getChatMessagesWebSocket();

    } else if (pathInfo.contains(PacketMatchingData.class.getName())) {
      int sessionId = Integer.parseInt(splitLastPart(pathInfo));
      return gameManager.getSession(sessionId).getMatchingDataWebSocket();

    } else if (pathInfo.contains(PacketReadyForGame.class.getName())) {
      int sessionId = Integer.parseInt(splitLastPart(pathInfo));
      return gameManager.getSession(sessionId).getReadyForGameWebSocket();

    } else if (pathInfo.contains(PacketGameStatus.class.getName())) {
      int sessionId = Integer.parseInt(splitLastPart(pathInfo));
      return gameManager.getSession(sessionId).getGameStatusWebSocket();

    } else if (pathInfo.contains(PacketServerStatus.class.getName())) {
      return serverStatusManager.getServerStatusWebSocket();

    }

    return null;
  }

  private String splitLastPart(String s) {
    String[] parts = s.split("/");
    return parts[parts.length - 1];
  }
}
