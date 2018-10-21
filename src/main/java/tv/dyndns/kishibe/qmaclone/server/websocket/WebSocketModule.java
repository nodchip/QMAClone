package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketReadyForGame;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;
import tv.dyndns.kishibe.qmaclone.server.ThreadPool;

public class WebSocketModule extends AbstractModule {
  private static final Logger logger = Logger.getLogger(WebSocketModule.class.toString());
  private static final String PATH_PREFIX = "/QMAClone/websocket/";

  @Override
  protected void configure() {
    // Do nothing.
  }

  @Provides
  private ServletContextHandler provideServletContextHandler(
      ChatMessagesWebSocketServlet chatMessagesWebSocketServlet,
      GameStatusWebSocketServlet gameStatusWebSocketServlet,
      MatchingStatusWebSocketServlet matchingStatusWebSocketServlet,
      ReadyForGameWebSocketServlet readyForGameWebSocketServlet,
      ServerStatusWebSocketServlet serverStatusWebSocketServlet) {
    ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    handler.addServlet(new ServletHolder(chatMessagesWebSocketServlet),
        PATH_PREFIX + PacketChatMessages.class.getName());
    handler.addServlet(new ServletHolder(gameStatusWebSocketServlet),
        PATH_PREFIX + PacketGameStatus.class.getName());
    handler.addServlet(new ServletHolder(matchingStatusWebSocketServlet),
        PATH_PREFIX + PacketMatchingStatus.class.getName());
    handler.addServlet(new ServletHolder(readyForGameWebSocketServlet),
        PATH_PREFIX + PacketReadyForGame.class.getName());
    handler.addServlet(new ServletHolder(serverStatusWebSocketServlet),
        PATH_PREFIX + PacketServerStatus.class.getName());
    return handler;
  }

  @Provides
  private HandlerList provideHandlerList(ServletContextHandler servletContextHandler) {
    HandlerList list = new HandlerList();
    list.addHandler(servletContextHandler);
    return list;
  }

  @Provides
  @Singleton
  private Server provideServer(HandlerList handlerList) throws Exception {
    Server server = new Server(Constant.WEB_SOCKET_PORT);
    server.setHandler(handlerList);
    try {
      server.start();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "WebSocketサーバーの開始に失敗しました。", e);
      throw e;
    }
    return server;
  }

  @Provides
  private MessageSender<PacketChatMessages> provideChatMessagesWebSockets(ThreadPool threadPool) {
    return new MessageSender<PacketChatMessages>(threadPool) {
      @Override
      protected String encode(PacketChatMessages status) {
        return new Gson().toJson(status);
      }
    };
  }

  @Provides
  private MessageSender<PacketMatchingStatus> provideMatchingStatusMessageSender(
      ThreadPool threadPool) {
    return new MessageSender<PacketMatchingStatus>(threadPool) {
      @Override
      protected String encode(PacketMatchingStatus status) {
        return new Gson().toJson(status);
      }
    };
  }

  @Provides
  private MessageSender<PacketReadyForGame> provideReadyForGameMessageSender(
      ThreadPool threadPool) {
    return new MessageSender<PacketReadyForGame>(threadPool) {
      @Override
      protected String encode(PacketReadyForGame status) {
        return new Gson().toJson(status);
      }
    };
  }

  @Provides
  private MessageSender<PacketGameStatus> provideGameStatusMessageSender(ThreadPool threadPool) {
    return new MessageSender<PacketGameStatus>(threadPool) {
      @Override
      protected String encode(PacketGameStatus status) {
        return new Gson().toJson(status);
      }
    };
  }

  @Provides
  private MessageSender<PacketServerStatus> provideServerStatusMessageSender(
      ThreadPool threadPool) {
    return new MessageSender<PacketServerStatus>(threadPool) {
      @Override
      protected String encode(PacketServerStatus status) {
        return new Gson().toJson(status);
      }
    };
  }
}
