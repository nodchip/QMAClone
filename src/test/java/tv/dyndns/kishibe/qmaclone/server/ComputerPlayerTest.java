package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
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
@RunWith(JUnit4.class)
public class ComputerPlayerTest {

  @Rule
  public final MockitoRule mocks = MockitoJUnit.rule();

  private static final int FAKE_PROBLEM_ID = 12345;
  @Mock
  private PlayerAnswer.Factory mockPlayerAnswerFactory;
  private ComputerPlayer player;

  @Before
  public void setUp() throws Exception {
    player = new ComputerPlayer(mockPlayerAnswerFactory, Arrays.asList(1425));
  }

  @Test
  @Ignore
  public void testSelectIconFileName() {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
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
  @Ignore
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
