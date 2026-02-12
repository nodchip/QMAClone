package tv.dyndns.kishibe.qmaclone.server;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableList;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.websocket.MessageSender;

@ExtendWith(MockitoExtension.class)
public class ServerStatusManagerTest {

  @Mock
  private Database mockDatabase;
  @Mock
  private GameManager mockGameManager;
  @Mock
  private NormalModeProblemManager mockNormalModeProblemManager;
  @Mock
  private PlayerHistoryManager mockPlayerHistoryManager;
  @Mock
  private MessageSender<PacketServerStatus> mockServerStatusWebSockets;
  @Mock
  private ThreadPool mockThreadPool;

  private ServerStatusManager manager;

  @BeforeEach
  public void setUp() throws Exception {
    when(mockDatabase.loadPageView()).thenReturn(new PageView());
    when(mockDatabase.getNumberOfActiveUsers()).thenReturn(12345);
    when(mockGameManager.getNumberOfPlayersInWhole()).thenReturn(1);
    when(mockNormalModeProblemManager.getNumberOfProblem()).thenReturn(210000);
    when(mockPlayerHistoryManager.get()).thenReturn(ImmutableList.of(new PacketPlayerSummary()));

    manager = new ServerStatusManager(mockDatabase, mockGameManager, mockNormalModeProblemManager,
        mockPlayerHistoryManager, mockServerStatusWebSockets, mockThreadPool);
  }

  @Test
  public void testLogin() throws Exception {
    int numberOfPageView = manager.getServerStatus().numberOfPageView;
    manager.login();
    manager.updateServerStatus();

    assertThat(manager.getServerStatus().numberOfPageView).isEqualTo(numberOfPageView + 1);

    verify(mockServerStatusWebSockets, times(2)).send(isA(PacketServerStatus.class));
  }

  @Test
  public void testGetServerStatus() throws Exception {
    assertThat(manager.getServerStatus()).isNotNull();
  }

  @Test
  @Disabled
  public void testSaveServerStatus() {
    fail("Not yet implemented");
  }

  @Test
  public void testKeepAlive() {
    manager.keepAlive(123456789);

    assertThat((Iterable<Integer>) manager.loginUserCodes).contains(123456789);

    verify(mockServerStatusWebSockets).send(isA(PacketServerStatus.class));
  }

  @Test
  @Disabled
  public void testGetLoginUsers() {
    fail("Not yet implemented");
  }

  @Test
  @Disabled
  public void testChangeStatics() {
    fail("Not yet implemented");
  }

  @Test
  public void updateServerStatusShouldNotUpdateFieldIfNoChange() throws Exception {
    PacketServerStatus status = manager.getServerStatus();
    manager.updateServerStatus();

    assertThat(manager.getServerStatus()).isSameInstanceAs(status);

    verify(mockServerStatusWebSockets).send(isA(PacketServerStatus.class));
  }

  @Test
  public void updateServerStatusShouldUpdateFieldIfChangeExists() throws Exception {
    PacketServerStatus status = manager.getServerStatus();
    manager.login();
    manager.updateServerStatus();

    assertThat(manager.getServerStatus()).isNotEqualTo(status);

    verify(mockServerStatusWebSockets, times(2)).send(isA(PacketServerStatus.class));
  }

  @Test
  public void getServerStatusMessageSenderReturnsInstance() {
    assertThat(manager.getServerStatusMessageSender()).isNotNull();
  }
}
