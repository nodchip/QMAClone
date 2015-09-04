package tv.dyndns.kishibe.qmaclone.server;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.eclipse.jetty.websocket.WebSocket;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.google.common.collect.ImmutableList;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;
import tv.dyndns.kishibe.qmaclone.server.database.Database;

@RunWith(JUnit4.class)
public class ServerStatusManagerTest {

  @Rule
  public final MockitoRule mocks = MockitoJUnit.rule();

  @Mock
  private Database mockDatabase;
  @Mock
  private GameManager mockGameManager;
  @Mock
  private NormalModeProblemManager mockNormalModeProblemManager;
  @Mock
  private PlayerHistoryManager mockPlayerHistoryManager;
  @Mock
  private ThreadPool mockThreadPool;
  @Mock
  private WebSocket mockWebSocket;

  private ServerStatusManager manager;

  @Before
  public void setUp() throws Exception {
    when(mockDatabase.loadPageView()).thenReturn(new PageView());
    when(mockDatabase.getNumberOfActiveUsers()).thenReturn(12345);
    when(mockGameManager.getNumberOfPlayersInWhole()).thenReturn(1);
    when(mockNormalModeProblemManager.getNumberOfProblem()).thenReturn(210000);
    when(mockPlayerHistoryManager.get()).thenReturn(ImmutableList.of(new PacketPlayerSummary()));

    manager = new ServerStatusManager(mockDatabase, mockGameManager, mockNormalModeProblemManager,
        mockPlayerHistoryManager, mockThreadPool);
  }

  @Test
  public void testLogin() throws Exception {
    int numberOfPageView = manager.getServerStatus().numberOfPageView;
    manager.login();
    manager.updateServerStatus();

    assertThat(manager.getServerStatus().numberOfPageView).isEqualTo(numberOfPageView + 1);
  }

  @Test
  public void testGetServerStatus() throws Exception {
    assertThat(manager.getServerStatus()).isNotNull();
  }

  @Test
  @Ignore
  public void testSaveServerStatus() {
    fail("Not yet implemented");
  }

  @Test
  public void testKeepAlive() {
    manager.keepAlive(123456789);

    assertThat((Iterable<Integer>) manager.loginUserCodes).contains(123456789);
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
    PacketServerStatus status = manager.getServerStatus();
    manager.updateServerStatus();

    assertThat(manager.getServerStatus()).isSameAs(status);
  }

  @Test
  public void updateServerStatusShouldUpdateFieldIfChangeExists() throws Exception {
    PacketServerStatus status = manager.getServerStatus();
    manager.login();
    manager.updateServerStatus();

    assertThat(manager.getServerStatus()).isNotEqualTo(status);
  }

}
