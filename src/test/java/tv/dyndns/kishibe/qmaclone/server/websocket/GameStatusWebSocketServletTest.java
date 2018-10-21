package tv.dyndns.kishibe.qmaclone.server.websocket;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.server.Game;
import tv.dyndns.kishibe.qmaclone.server.GameManager;
import tv.dyndns.kishibe.qmaclone.server.exception.GameNotFoundException;
import tv.dyndns.kishibe.qmaclone.server.websocket.GameStatusWebSocketServlet.GameStatusWebSocket;

@RunWith(MockitoJUnitRunner.class)
public class GameStatusWebSocketServletTest {
  @Mock
  private GameManager mockGameManager;
  @Mock
  private Session mockSession;
  @Mock
  private MessageSender<PacketGameStatus> mockMessageSender;
  @Mock
  private WebSocketServletFactory mockWebSocketServletFactory;
  @Mock
  private Game mockGame;
  private FakeUpgradeRequest fakeUpgradeRequest;
  private GameStatusWebSocketServlet servlet;
  private GameStatusWebSocket webSocket;

  @Before
  public void setUp() throws Exception {
    servlet = new GameStatusWebSocketServlet(mockGameManager);
    webSocket = new GameStatusWebSocket();
    fakeUpgradeRequest = new FakeUpgradeRequest();
  }

  @Test
  public void onConnectJoinsSessionToGameStatusMessageSender() throws Exception {
    fakeUpgradeRequest
        .setParameterMap(ImmutableMap.of(Constant.KEY_GAME_SESSION_ID, ImmutableList.of("121")));

    when(mockSession.getUpgradeRequest()).thenReturn(fakeUpgradeRequest);
    when(mockGameManager.getSession(121)).thenReturn(mockGame);
    when(mockGame.getGameStatusMessageSender()).thenReturn(mockMessageSender);

    webSocket.onConnect(mockSession);

    verify(mockMessageSender).join(mockSession);
  }

  @Test
  public void onConnectClosesSessionIfFailedToParseGameSessionId() throws Exception {
    fakeUpgradeRequest.setParameterMap(ImmutableMap.of());

    when(mockSession.getUpgradeRequest()).thenReturn(fakeUpgradeRequest);
    when(mockGameManager.getSession(121)).thenReturn(mockGame);
    when(mockGame.getGameStatusMessageSender()).thenReturn(mockMessageSender);

    webSocket.onConnect(mockSession);

    verify(mockSession).close();
  }

  @Test
  public void onConnectClosesSessionIfGameSessionNotFound() throws Exception {
    fakeUpgradeRequest.setParameterMap(ImmutableMap.of());

    when(mockSession.getUpgradeRequest()).thenReturn(fakeUpgradeRequest);
    when(mockGameManager.getSession(121)).thenThrow(new GameNotFoundException());

    webSocket.onConnect(mockSession);

    verify(mockSession).close();
  }

  @Test
  public void onCloseByesSessionFromGameStatusMessageSender() throws Exception {
    fakeUpgradeRequest
        .setParameterMap(ImmutableMap.of(Constant.KEY_GAME_SESSION_ID, ImmutableList.of("121")));

    when(mockSession.getUpgradeRequest()).thenReturn(fakeUpgradeRequest);
    when(mockGameManager.getSession(121)).thenReturn(mockGame);
    when(mockGame.getGameStatusMessageSender()).thenReturn(mockMessageSender);

    webSocket.onConnect(mockSession);
    webSocket.onClose(0, "");

    verify(mockMessageSender).bye(mockSession);
  }

  @Test
  public void onCloseClosesSessionIfGameSessionNotFound() throws Exception {
    fakeUpgradeRequest
        .setParameterMap(ImmutableMap.of(Constant.KEY_GAME_SESSION_ID, ImmutableList.of("121")));

    when(mockSession.getUpgradeRequest()).thenReturn(fakeUpgradeRequest);
    when(mockGameManager.getSession(121)).thenThrow(new GameNotFoundException());

    webSocket.onConnect(mockSession);
    webSocket.onClose(0, "");

    verify(mockSession, times(2)).close();
  }

  @Test
  public void onErrorByesSessionFromGameStatusMessageSender() throws Exception {
    fakeUpgradeRequest
        .setParameterMap(ImmutableMap.of(Constant.KEY_GAME_SESSION_ID, ImmutableList.of("121")));

    when(mockSession.getUpgradeRequest()).thenReturn(fakeUpgradeRequest);
    when(mockSession.getRemoteAddress()).thenReturn(new InetSocketAddress(0));
    when(mockGameManager.getSession(121)).thenReturn(mockGame);
    when(mockGame.getGameStatusMessageSender()).thenReturn(mockMessageSender);

    webSocket.onConnect(mockSession);
    webSocket.onError(new Exception());

    verify(mockMessageSender).bye(mockSession);
  }

  @Test
  public void onErrorClosesSessionIfGameSessionNotFound() throws Exception {
    fakeUpgradeRequest
        .setParameterMap(ImmutableMap.of(Constant.KEY_GAME_SESSION_ID, ImmutableList.of("121")));

    when(mockSession.getUpgradeRequest()).thenReturn(fakeUpgradeRequest);
    when(mockGameManager.getSession(121)).thenThrow(new GameNotFoundException());
    when(mockSession.getRemoteAddress()).thenReturn(new InetSocketAddress(0));

    webSocket.onConnect(mockSession);
    webSocket.onError(new Exception());

    verify(mockSession, times(2)).close();
  }

  @Test
  public void configureRegistersGameStatusWebSocket() {
    servlet.configure(mockWebSocketServletFactory);

    verify(mockWebSocketServletFactory).register(GameStatusWebSocket.class);
  }
}
