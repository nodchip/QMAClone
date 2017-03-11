package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import tv.dyndns.kishibe.qmaclone.client.game.GameMode;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.Transition;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRoomKey;

@RunWith(MockitoJUnitRunner.class)
public class GameManagerTest {
  @Mock
  private Game.Factory mockGameFactory;
  @Mock
  private Game mockGame1, mockGame2, mockGame3, mockGame4;
  @Mock
  private ServerStatusManager mockServerStatusManager;
  @Mock
  private RestrictedUserUtils mockRestrictedUserUtils;
  private GameManager gameManager;

  @Before
  public void setUp() throws Exception {
    gameManager = new GameManager(mockGameFactory, mockRestrictedUserUtils);
  }

  @Test
  public void getOrCreateMatchingSessionShouldReturnMatchingGameSession() {
    when(mockGameFactory.create(1, 0, false, false, null, false, GameMode.WHOLE))
        .thenReturn(mockGame1);

    Game game = gameManager.getOrCreateMatchingSession(GameMode.WHOLE, null, 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), false,
        mockServerStatusManager, 12345678, "192.168.0.1");

    assertSame(mockGame1, game);
  }

  @Test
  public void getOrCreateMatchingSessionShouldReturnSameMatchingGameSessionForSecondTime() {
    when(mockGameFactory.create(1, 0, false, false, null, false, GameMode.WHOLE))
        .thenReturn(mockGame1);
    when(mockGame1.getTransition()).thenReturn(Transition.Matching);

    List<Game> games = Lists.newArrayList();

    for (int i = 0; i < 2; ++i) {
      Game game = gameManager.getOrCreateMatchingSession(GameMode.WHOLE, null, 0, null,
          EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), false,
          mockServerStatusManager, 12345678, "192.168.0.1");
      games.add(game);
    }

    assertEquals(ImmutableList.of(mockGame1, mockGame1), games);
  }

  @Test
  public void getOrCreateMatchingSessionShouldReturnNewMatchingGameSessionIfFull() {
    when(mockGameFactory.create(1, 0, false, false, null, false, GameMode.WHOLE))
        .thenReturn(mockGame1);
    when(mockGameFactory.create(2, 0, false, false, null, false, GameMode.WHOLE))
        .thenReturn(mockGame2);
    when(mockGame1.getTransition()).thenReturn(Transition.Matching, Transition.Matching,
        Transition.Matching, Transition.Matching, Transition.Matching, Transition.Matching,
        Transition.Matching, Transition.Ready);

    List<Game> games = Lists.newArrayList();

    for (int i = 0; i < 9; ++i) {
      Game game = gameManager.getOrCreateMatchingSession(GameMode.WHOLE, null, 0, null,
          EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), false,
          mockServerStatusManager, 12345678, "192.168.0.1");
      games.add(game);
    }

    assertEquals(ImmutableList.of(mockGame1, mockGame1, mockGame1, mockGame1, mockGame1, mockGame1,
        mockGame1, mockGame1, mockGame2), games);
  }

  @Test
  public void getSessionShouldReturnCreatedGame() throws Exception {
    when(mockGameFactory.create(1, 0, false, false, null, false, GameMode.WHOLE))
        .thenReturn(mockGame1);

    gameManager.getOrCreateMatchingSession(GameMode.WHOLE, null, 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), false,
        mockServerStatusManager, 12345678, "192.168.0.1");
    Game game = gameManager.getSession(1);

    assertSame(mockGame1, game);
  }

  @Test
  public void getPublicMatchingEventRooms() {
    when(mockGameFactory.create(1, 0, true, false, null, true, GameMode.EVENT))
        .thenReturn(mockGame1);
    when(mockGame1.isEvent()).thenReturn(true);
    when(mockGame1.isPublicEvent()).thenReturn(true);
    when(mockGame1.getTransition()).thenReturn(Transition.Matching);

    when(mockGameFactory.create(2, 0, true, false, null, false, GameMode.EVENT))
        .thenReturn(mockGame2);
    when(mockGame2.isEvent()).thenReturn(true);
    when(mockGame2.isPublicEvent()).thenReturn(false);
    when(mockGame2.getTransition()).thenReturn(Transition.Matching);

    when(mockGameFactory.create(3, 0, false, false, null, false, GameMode.WHOLE))
        .thenReturn(mockGame3);
    when(mockGame3.isEvent()).thenReturn(false);
    when(mockGame3.isPublicEvent()).thenReturn(false);
    when(mockGame3.getTransition()).thenReturn(Transition.Matching);

    when(mockGameFactory.create(4, 0, true, false, null, true, GameMode.EVENT))
        .thenReturn(mockGame3);
    when(mockGame4.isEvent()).thenReturn(false);
    when(mockGame4.isPublicEvent()).thenReturn(false);
    when(mockGame4.getTransition()).thenReturn(Transition.Problem);

    gameManager.getOrCreateMatchingSession(GameMode.EVENT, "public EVENT name", 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), true,
        mockServerStatusManager, 12345678, "192.168.0.1");
    gameManager.getOrCreateMatchingSession(GameMode.EVENT, "closed EVENT name", 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), false,
        mockServerStatusManager, 12345678, "192.168.0.1");
    gameManager.getOrCreateMatchingSession(GameMode.WHOLE, null, 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), false,
        mockServerStatusManager, 12345678, "192.168.0.1");
    gameManager.getOrCreateMatchingSession(GameMode.EVENT, "playing EVENT name", 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), true,
        mockServerStatusManager, 12345678, "192.168.0.1");
    List<PacketRoomKey> rooms = gameManager.getPublicMatchingEventRooms();

    assertEquals(ImmutableList.of(new PacketRoomKey(GameMode.EVENT, "public EVENT name",
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu))), rooms);
  }

  @Test
  public void getNumberOfPlayersInWholeShouldReturnNumberOfHumanPlayers() {
    when(mockGameFactory.create(1, 0, false, false, null, false, GameMode.WHOLE))
        .thenReturn(mockGame1);
    when(mockGame1.getNumberOfHumanPlayer()).thenReturn(3);
    when(mockGame1.getTransition()).thenReturn(Transition.Matching);

    gameManager.getOrCreateMatchingSession(GameMode.WHOLE, null, 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), false,
        mockServerStatusManager, 12345678, "192.168.0.1");

    assertEquals(3, gameManager.getNumberOfPlayersInWhole());
  }

  @Test
  public void getNumberOfSessionsShouldReturnSizeOfSessions() {
    when(mockGameFactory.create(1, 0, true, false, null, true, GameMode.EVENT))
        .thenReturn(mockGame1);
    when(mockGameFactory.create(2, 0, true, false, null, false, GameMode.EVENT))
        .thenReturn(mockGame2);
    when(mockGameFactory.create(3, 0, false, false, null, false, GameMode.WHOLE))
        .thenReturn(mockGame3);

    assertEquals(0, gameManager.getNumberOfSessions());

    gameManager.getOrCreateMatchingSession(GameMode.EVENT, "public EVENT name", 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), true,
        mockServerStatusManager, 12345678, "192.168.0.1");

    assertEquals(1, gameManager.getNumberOfSessions());

    gameManager.getOrCreateMatchingSession(GameMode.EVENT, "closed EVENT name", 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), false,
        mockServerStatusManager, 12345678, "192.168.0.1");

    assertEquals(2, gameManager.getNumberOfSessions());

    gameManager.getOrCreateMatchingSession(GameMode.WHOLE, null, 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), false,
        mockServerStatusManager, 12345678, "192.168.0.1");

    assertEquals(3, gameManager.getNumberOfSessions());
  }

  @Test
  public void getNumberOfPlayersShouldReturnTotalNumberOfHumanPlayers() {
    when(mockGameFactory.create(1, 0, true, false, null, true, GameMode.EVENT))
        .thenReturn(mockGame1);
    when(mockGame1.getNumberOfHumanPlayer()).thenReturn(1);

    when(mockGameFactory.create(2, 0, true, false, null, false, GameMode.EVENT))
        .thenReturn(mockGame2);
    when(mockGame2.getNumberOfHumanPlayer()).thenReturn(2);

    gameManager.getOrCreateMatchingSession(GameMode.EVENT, "public EVENT name", 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), true,
        mockServerStatusManager, 12345678, "192.168.0.1");
    gameManager.getOrCreateMatchingSession(GameMode.EVENT, "closed EVENT name", 0, null,
        EnumSet.of(ProblemGenre.Anige), EnumSet.of(ProblemType.Marubatsu), false,
        mockServerStatusManager, 12345678, "192.168.0.1");

    assertEquals(3, gameManager.getNumberOfPlayers());
  }

}
