package tv.dyndns.kishibe.qmaclone.server.websocket;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.server.exception.InvalidGameSessionIdException;

/**
 * {@link GameUtil}のテスト
 * 
 * @author nodchip
 */
@RunWith(MockitoJUnitRunner.class)
public class GameUtilTest {
  @Mock
  private Session session;
  private FakeUpgradeRequest request;

  @Before
  public void setUp() throws Exception {
    request = new FakeUpgradeRequest();
  }

  @Test
  public void extractGameSessionIdReturnsGameSessionId() throws Exception {
    request.setParameterMap(ImmutableMap.of(Constant.KEY_GAME_SESSION_ID, ImmutableList.of("121")));

    when(session.getUpgradeRequest()).thenReturn(request);

    assertEquals(121, GameUtil.extractGameSessionId(session));
  }

  @Test(expected = InvalidGameSessionIdException.class)
  public void extractGameSessionIdThrowsExceptionIfQueryParameterNotExist() throws Exception {
    request.setParameterMap(ImmutableMap.of());

    when(session.getUpgradeRequest()).thenReturn(request);

    GameUtil.extractGameSessionId(session);
  }

  @Test(expected = InvalidGameSessionIdException.class)
  public void extractGameSessionIdThrowsExceptionIfValueNotExist() throws Exception {
    request.setParameterMap(ImmutableMap.of(Constant.KEY_GAME_SESSION_ID, ImmutableList.of()));

    when(session.getUpgradeRequest()).thenReturn(request);

    GameUtil.extractGameSessionId(session);
  }

  @Test(expected = InvalidGameSessionIdException.class)
  public void extractGameSessionIdThrowsExceptionIfInvalidFormat() throws Exception {
    request.setParameterMap(
        ImmutableMap.of(Constant.KEY_GAME_SESSION_ID, ImmutableList.of("121", "232")));

    when(session.getUpgradeRequest()).thenReturn(request);

    GameUtil.extractGameSessionId(session);
  }
}
