package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;

/**
 * Test for {@link ComputerPlayer}.
 * 
 * @author nodchip
 */
public class ComputerPlayerTest {

  @Rule
  public final MockitoRule mocks = MockitoJUnit.rule();

  private static final int FAKE_PROBLEM_ID = 12345;
  @Mock
  private PlayerAnswer.Factory mockPlayerAnswerFactory;
  private ComputerPlayer player;

  @BeforeEach
  public void setUp() throws Exception {
    player = new ComputerPlayer(mockPlayerAnswerFactory, Arrays.asList(1425));
  }

  @Test
  @Disabled
  public void testSelectIconFileName() {
    fail("Not yet implemented");
  }

  @Test
  @Disabled
  public void testNewPlayer() {
    fail("Not yet implemented");
  }

  @Test
  public void getAnswerShouldReturnJunbanAnswer() {
    PacketProblem problem = TestDataProvider.getProblem();
    problem.type = ProblemType.Junban;
    problem.answers = new String[] { "a", "b", "c", "d" };
    problem.choices = new String[] {};
    problem.prepareShuffledAnswersAndChoices();
    player.getAnswer(problem,
        Arrays.asList("a" + Constant.DELIMITER_GENERAL + "b" + Constant.DELIMITER_GENERAL + "c"));
    // TODO(nodchip):テストを書く
  }

  @Test
  @Disabled
  public void testGetGreeting() {
    fail("Not yet implemented");
  }

  @Test
  public void getAnswerMojiPanelShouldReturnRandomAnswer() {
    PacketProblem problem = TestDataProvider.getProblem();
    problem.id = FAKE_PROBLEM_ID;
    problem.type = ProblemType.MojiPanel;
    problem.shuffledChoices = new String[] { "01234567" };
    problem.shuffledAnswers = new String[] { "0123" };

    player = spy(player);
    doReturn(false).when(player).correct();
    doReturn(null).when(player).joke(FAKE_PROBLEM_ID);

    String answer = player.getAnswerMojiPanel(problem);
    assertEquals(4, answer.length());
  }

  @Test
  public void getAnswerMojiPanelShouldTreatInvalidData() {
    PacketProblem problem = TestDataProvider.getProblem();
    problem.id = FAKE_PROBLEM_ID;
    problem.type = ProblemType.MojiPanel;
    problem.shuffledChoices = new String[] { "012" };
    problem.shuffledAnswers = new String[] { "0123" };

    player = spy(player);
    doReturn(false).when(player).correct();
    doReturn(null).when(player).joke(FAKE_PROBLEM_ID);

    String answer = player.getAnswerMojiPanel(problem);
    assertEquals(3, answer.length());
  }
}
