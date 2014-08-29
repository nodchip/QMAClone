package tv.dyndns.kishibe.qmaclone.server;

import static org.easymock.EasyMock.isA;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.eclipse.jetty.websocket.WebSocket;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.websocket.WebSockets;

import com.google.common.collect.ImmutableList;

public class ServerStatusManagerTest extends EasyMockSupport {
  private Database mockDatabase;
  private GameManager mockGameManager;
  private NormalModeProblemManager mockNormalModeProblemManager;
  private PlayerHistoryManager mockPlayerHistoryManager;
  private WebSockets<PacketServerStatus> mockServerStatusWebSockets;
  private ThreadPool mockThreadPool;
  private WebSocket mockWebSocket;
  private ServerStatusManager manager;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    mockDatabase = createMock(Database.class);
    mockGameManager = createMock(GameManager.class);
    mockNormalModeProblemManager = createMock(NormalModeProblemManager.class);
    mockPlayerHistoryManager = createMock(PlayerHistoryManager.class);
    mockServerStatusWebSockets = createMock(WebSockets.class);
    mockThreadPool = createNiceMock(ThreadPool.class);
    mockWebSocket = createMock(WebSocket.class);

    EasyMock.expect(mockDatabase.loadPageView()).andStubReturn(new PageView());
    EasyMock.expect(mockDatabase.getNumberOfActiveUsers()).andStubReturn(12345);
    EasyMock.expect(mockGameManager.getNumberOfPlayersInWhole()).andStubReturn(1);
    EasyMock.expect(mockNormalModeProblemManager.getNumberOfProblem()).andStubReturn(210000);
    EasyMock.expect(mockPlayerHistoryManager.get()).andStubReturn(
        ImmutableList.of(new PacketPlayerSummary()));
  }

  @Test
  public void testLogin() throws Exception {
    mockServerStatusWebSockets.send(isA(PacketServerStatus.class));
    mockServerStatusWebSockets.send(isA(PacketServerStatus.class));

    replayAll();

    manager = new ServerStatusManager(mockDatabase, mockGameManager, mockNormalModeProblemManager,
        mockPlayerHistoryManager, mockServerStatusWebSockets, mockThreadPool);
    int numberOfPageView = manager.getServerStatus().numberOfPageView;
    manager.login();
    manager.updateServerStatus();
    assertEquals(numberOfPageView + 1, manager.getServerStatus().numberOfPageView);

    verifyAll();
  }

  @Test
  public void testGetServerStatus() throws Exception {
    mockServerStatusWebSockets.send(isA(PacketServerStatus.class));

    replayAll();

    manager = new ServerStatusManager(mockDatabase, mockGameManager, mockNormalModeProblemManager,
        mockPlayerHistoryManager, mockServerStatusWebSockets, mockThreadPool);
    assertNotNull(manager.getServerStatus());

    verifyAll();
  }

  @Test
  @Ignore
  public void testSaveServerStatus() {
    fail("Not yet implemented");
  }

  @Test
  public void testKeepAlive() {
    mockServerStatusWebSockets.send(isA(PacketServerStatus.class));

    replayAll();

    manager = new ServerStatusManager(mockDatabase, mockGameManager, mockNormalModeProblemManager,
        mockPlayerHistoryManager, mockServerStatusWebSockets, mockThreadPool);
    manager.keepAlive(123456789);

    verifyAll();

    assertThat(manager.loginUserCodes, hasItem(123456789));
  }

  @Test
  @Ignore
  public void testGetLoginUsers() {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testChangeStatics() {
    fail("Not yet implemented");
  }

  @Test
  public void updateServerStatusShouldNotUpdateFieldIfNoChange() throws Exception {
    mockServerStatusWebSockets.send(isA(PacketServerStatus.class));

    replayAll();

    manager = new ServerStatusManager(mockDatabase, mockGameManager, mockNormalModeProblemManager,
        mockPlayerHistoryManager, mockServerStatusWebSockets, mockThreadPool);
    PacketServerStatus status = manager.getServerStatus();
    manager.updateServerStatus();
    assertSame(status, manager.getServerStatus());

    verifyAll();
  }

  @Test
  public void updateServerStatusShouldUpdateFieldIfChangeExists() throws Exception {
    mockServerStatusWebSockets.send(isA(PacketServerStatus.class));
    mockServerStatusWebSockets.send(isA(PacketServerStatus.class));

    replayAll();

    manager = new ServerStatusManager(mockDatabase, mockGameManager, mockNormalModeProblemManager,
        mockPlayerHistoryManager, mockServerStatusWebSockets, mockThreadPool);
    PacketServerStatus status = manager.getServerStatus();
    manager.login();
    manager.updateServerStatus();
    assertThat(status, not(equalTo(manager.getServerStatus())));

    verifyAll();
  }

  @Test
  public void getServerStatusWebSocketShouldReturnSocket() {
    mockServerStatusWebSockets.send(isA(PacketServerStatus.class));
    EasyMock.expect(mockServerStatusWebSockets.newWebSocket()).andReturn(mockWebSocket);

    replayAll();

    manager = new ServerStatusManager(mockDatabase, mockGameManager, mockNormalModeProblemManager,
        mockPlayerHistoryManager, mockServerStatusWebSockets, mockThreadPool);
    assertNotNull(manager.getServerStatusWebSocket());

    verifyAll();
  }
}
