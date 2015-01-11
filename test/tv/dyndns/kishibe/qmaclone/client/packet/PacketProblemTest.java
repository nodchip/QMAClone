package tv.dyndns.kishibe.qmaclone.client.packet;

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;

@RunWith(JUnit4.class)
public class PacketProblemTest {
  private PacketProblem problem;

  @Before
  public void setUp() throws Exception {
    problem = TestDataProvider.getProblem();
  }

  @Test
  public void testSetSentence() {
    problem.setSentence("a\r\n                              <>&'");
    assertEquals("a%n%w%lt;%gt;%amp;%dash;", problem.sentence);
  }

  @Test
  public void testGetPanelSentence() {
    problem.sentence = "a%n%w%lt;%gt;%amp;%dash;";
    assertEquals("a\n                              <>&'", problem.getPanelSentence());
  }

  @Test
  public void testGetProblemCreationSentence() {
    problem.sentence = "a%n%w%lt;%gt;%amp;%dash;";
    assertEquals("a%n%w<>&'", problem.getProblemCreationSentence());
  }

  @Test
  public void testGetProblemReportSentence() {
    problem.answers = new String[] { "answer" };
    problem.sentence = "a%n%w%lt;%gt;%amp;%dash;!<div></div><object></object>";
    assertEquals("a <>&' answer", problem.getProblemReportSentence());
  }

  @Test
  public void testIsCorrect() {
    problem.type = ProblemType.Senmusubi;
    problem.answers = new String[] { "a", "b", "c", "d", null, null, null, null };
    problem.choices = new String[] { "A", "B", "C", "D", null, null, null, null };

    String answer = "A" + Constant.DELIMITER_KUMIAWASE_PAIR + "a" + Constant.DELIMITER_GENERAL
        + "B" + Constant.DELIMITER_KUMIAWASE_PAIR + "b" + Constant.DELIMITER_GENERAL + "C"
        + Constant.DELIMITER_KUMIAWASE_PAIR + "c" + Constant.DELIMITER_GENERAL + "D"
        + Constant.DELIMITER_KUMIAWASE_PAIR + "d";
    assertTrue(problem.isCorrect(answer));
  }

  @Test
  public void testGetNumberOfAnswers() {
    assertEquals(4, problem.getNumberOfAnswers());
  }

  @Test
  public void testGetNumberOfChoices() {
    assertEquals(4, problem.getNumberOfChoices());
  }

  @Test
  public void testGetAnswerList() {
    problem.answers = new String[] { "a", "b", "c", "d", null, null, null, null };
    assertEquals(Arrays.asList("a", "b", "c", "d"), problem.getAnswerList());
  }

  @Test
  public void testGetChoiceList() {
    problem.choices = new String[] { "A", "B", "C", "D", null, null, null, null };

    assertEquals(Arrays.asList("A", "B", "C", "D"), problem.getChoiceList());
  }

  @Test
  public void testGetSearchQuery() {
    problem.sentence = "問題文";
    problem.answers = new String[] { "a", "b", "c", "d", null, null, null, null };
    problem.choices = new String[] { "A", "B", "C", "D", null, null, null, null };
    problem.note = "ノート";

    assertEquals("問題文 a b c d A B C D ノート", problem.getSearchDocument());
  }

  @Test
  public void testCreateShuffledData() {
    problem.type = ProblemType.YonTaku;
    problem.answers = new String[] { "a" };
    problem.choices = new String[] { "a", "b", "c", "d" };
    problem.prepareShuffledAnswersAndChoices();
    assertThat(Arrays.asList(problem.shuffledAnswers), containsInAnyOrder("a"));
    assertThat(Arrays.asList(problem.shuffledChoices), containsInAnyOrder("a", "b", "c", "d"));
  }

  @Test
  public void prepareShuffledAnswersAndChoicesShouldTruncateAnswersAndChoices() {
    problem.type = ProblemType.Senmusubi;
    problem.answers = new String[] { "a", "b", "c", "d", "e", "f", "g", "h" };
    problem.choices = new String[] { "a", "b", "c", "d", "e", "f", "g", "h" };
    problem.prepareShuffledAnswersAndChoices();
    assertEquals(4, problem.shuffledAnswers.length);
    assertEquals(4, problem.shuffledChoices.length);
  }

  @Test
  public void testGetShuffledAnswerIndex() {
    problem.shuffledAnswers = new String[] { "d", "c", "b", "a" };

    assertEquals(0, problem.getShuffledAnswerIndex("d"));
  }

  @Test
  public void testGetShuffledChoiceIndex() {
    problem.shuffledChoices = new String[] { "D", "C", "B", "A" };

    assertEquals(0, problem.getShuffledChoiceIndex("D"));
  }

  @Test
  public void testHasImage() {
    assertTrue(problem.hasImage());
  }

  @Test
  public void testHasMovie() {
    assertTrue(problem.hasMovie());
  }

  @Test
  public void testAsMinimum() {
    Date date = new Date();

    problem.id = 12345;
    problem.genre = ProblemGenre.Anige;
    problem.type = ProblemType.Senmusubi;
    problem.good = 11;
    problem.bad = 22;
    problem.randomFlag = RandomFlag.Random1;
    problem.creatorHash = "作成者".hashCode();
    problem.userCode = 12345678;
    problem.indication = date;

    PacketProblemMinimum expected = new PacketProblemMinimum();
    expected.id = 12345;
    expected.genre = ProblemGenre.Anige;
    expected.type = ProblemType.Senmusubi;
    expected.good = 11;
    expected.bad = 22;
    expected.randomFlag = RandomFlag.Random1;
    expected.creatorHash = "作成者".hashCode();
    expected.userCode = 12345678;
    expected.indication = date;

    assertEquals(expected, problem.asMinimum());
  }

  @Test
  public void getProblemReportSentenceShouldReturnTestingProblem() {
    problem.answers = new String[] { "answer" };
    problem.sentence = "sentence";
    problem.testing = true;

    assertEquals("sentence (出題中)", problem.getProblemReportSentence());
  }

  @Test
  public void getProblemReportSentenceShouldWorkWithImageAnswer() {
    problem.answers = new String[] { "answer" };
    problem.sentence = "sentence";
    problem.testing = false;
    problem.imageAnswer = true;

    assertEquals("sentence (画像)", problem.getProblemReportSentence());
  }

  @Test
  public void getProblemReportSentenceShouldWorkWithImageChoice() {
    problem.answers = new String[] { "answer" };
    problem.sentence = "sentence";
    problem.testing = false;
    problem.imageChoice = true;

    assertEquals("sentence (画像)", problem.getProblemReportSentence());
  }

  @Test
  public void getProblemReportSentenceShouldWorkWithNormalProblem() {
    problem.answers = new String[] { "answer" };
    problem.sentence = "sentence";
    problem.testing = false;
    problem.imageAnswer = false;

    assertEquals("sentence answer", problem.getProblemReportSentence());
  }

  @Test
  public void cloneShouldCopyDeeply() {
    PacketProblem problem = TestDataProvider.getProblem();

    assertEquals(problem, problem.clone());
  }

  @Test
  public void cloneForCopyingProblemShouldClearFields() {
    PacketProblem problem = TestDataProvider.getProblem();

    PacketProblem cloned = problem.cloneForCopyingProblem();
    assertThat(cloned.id).isEqualTo(PacketProblem.CREATING_PROBLEM_ID);
    assertThat(cloned.good).isEqualTo(0);
    assertThat(cloned.bad).isEqualTo(0);
    assertThat(cloned.indication).isNull();
    assertThat(cloned.creator).isEqualTo(UserData.get().getPlayerName());
    assertThat(cloned.voteGood).isEqualTo(0);
    assertThat(cloned.voteBad).isEqualTo(0);
    assertThat(cloned.indicationMessage).isNull();
    assertThat(cloned.indicationResolved).isNull();
  }
}
