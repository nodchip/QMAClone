package tv.dyndns.kishibe.qmaclone.server.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.websocket.Session;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.server.exception.InvalidGameSessionIdException;

/**
 * {@link GameUtil}のテスト
 * 
 * @author nodchip
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GameUtilTest {
  @Mock
  private Session session;
  private Map<String, List<String>> requestParameterMap;

  @BeforeEach
  public void setUp() throws Exception {
    requestParameterMap = ImmutableMap.of();
  }

  @Test
  public void extractGameSessionIdReturnsGameSessionId() throws Exception {
    requestParameterMap = ImmutableMap.of(Constant.KEY_GAME_SESSION_ID, ImmutableList.of("121"));
    when(session.getRequestParameterMap()).thenReturn(requestParameterMap);

    assertEquals(121, GameUtil.extractGameSessionId(session));
  }

  @Test
  public void extractGameSessionIdThrowsExceptionIfQueryParameterNotExist() throws Exception {
    requestParameterMap = ImmutableMap.of();
    when(session.getRequestParameterMap()).thenReturn(requestParameterMap);

    assertThrows(InvalidGameSessionIdException.class, () -> GameUtil.extractGameSessionId(session));
  }

  @Test
  public void extractGameSessionIdThrowsExceptionIfValueNotExist() throws Exception {
    requestParameterMap = ImmutableMap.of(Constant.KEY_GAME_SESSION_ID, ImmutableList.of());
    when(session.getRequestParameterMap()).thenReturn(requestParameterMap);

    assertThrows(InvalidGameSessionIdException.class, () -> GameUtil.extractGameSessionId(session));
  }

  @Test
  public void extractGameSessionIdThrowsExceptionIfInvalidFormat() throws Exception {
    requestParameterMap = ImmutableMap.of(Constant.KEY_GAME_SESSION_ID, ImmutableList.of("121", "232"));
    when(session.getRequestParameterMap()).thenReturn(requestParameterMap);

    assertThrows(InvalidGameSessionIdException.class, () -> GameUtil.extractGameSessionId(session));
  }
}
