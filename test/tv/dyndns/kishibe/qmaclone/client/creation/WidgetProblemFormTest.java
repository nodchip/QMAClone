package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.Date;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

public class WidgetProblemFormTest extends QMACloneGWTTestCaseBase {
  private static final String INDICATION_MESSAGE = "indication message";
  private WidgetProblemForm form;
  private PacketProblem problem;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    form = new WidgetProblemForm(
        new CreationUi(new WrongAnswerPresenter(new WrongAnswerViewImpl())));
    problem = new PacketProblem();
    problem.id = 123456;
    problem.genre = ProblemGenre.Gakumon;
    problem.type = ProblemType.Senmusubi;
    problem.good = 123;
    problem.bad = 234;
    problem.randomFlag = RandomFlag.Random2;
    problem.creatorHash = 345;
    problem.userCode = 12345678;
    problem.sentence = "sentence";
    problem.answers = new String[] { "A", "B", "C", "D" };
    problem.choices = new String[] { "a", "b", "c", "d" };
    problem.creator = "creator";
    problem.note = "note";
    problem.shuffledAnswers = new String[] { "D", "C", "B", "A" };
    problem.shuffledChoices = new String[] { "d", "c", "b", "a" };
    problem.imageAnswer = false;
    problem.imageChoice = false;
    problem.voteGood = 0;
    problem.voteBad = 0;
    problem.imageUrl = "http://hoge";
    problem.movieUrl = "http://fuga";
    problem.indication = new Date(123456789L);
    problem.indicationMessage = INDICATION_MESSAGE;
    problem.numberOfDisplayedChoices = 3;
  }

  @Test
  public void testSetGetProblemWithCopy() {
    problem.indication = new Date();
    problem.indicationResolved = new Date();

    form.setProblem(problem.cloneForCopyingProblem());
    PacketProblem p = form.getProblem();

    assertEquals(-1, p.id);
    assertEquals(problem.genre, p.genre);
    assertEquals(problem.type, p.type);
    assertEquals(0, p.good);
    assertEquals(0, p.bad);
    assertEquals(problem.randomFlag, p.randomFlag);
    // assertEquals(problem.creatorHash, p.creatorHash);
    // assertEquals(problem.userCode, p.userCode);
    assertEquals(problem.sentence, p.sentence);
    assertEquals(problem.answers[0], p.answers[0]);
    assertEquals(problem.answers[1], p.answers[1]);
    assertEquals(problem.answers[2], p.answers[2]);
    assertEquals(problem.answers[3], p.answers[3]);
    assertEquals(problem.choices[0], p.choices[0]);
    assertEquals(problem.choices[1], p.choices[1]);
    assertEquals(problem.choices[2], p.choices[2]);
    assertEquals(problem.choices[3], p.choices[3]);
    assertEquals("未初期化です", p.creator);
    assertEquals(problem.note, p.note);
    assertEquals(problem.imageAnswer, p.imageAnswer);
    assertEquals(problem.imageChoice, p.imageChoice);
    assertEquals(0, p.voteGood);
    assertEquals(0, p.voteBad);
    // assertEquals(problem.imageUrl, p.imageUrl);
    // assertEquals(problem.movieUrl, p.movieUrl);
    // BugTrack-QMAClone/595 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F595
    assertNull(p.indication);
    assertNull(p.indicationResolved);
    // assertEquals(problem.indicationMessage, p.indicationMessage);

    assertTrue(form.htmlPlusOne.getHTML().isEmpty());
  }

  @Test
  public void testSetGetProblemWithoutCopy() {
    form.setProblem(problem);
    PacketProblem p = form.getProblem();

    assertEquals(problem.id, p.id);
    assertEquals(problem.genre, p.genre);
    assertEquals(problem.type, p.type);
    assertEquals(problem.good, p.good);
    assertEquals(problem.bad, p.bad);
    assertEquals(problem.randomFlag, p.randomFlag);
    // assertEquals(problem.creatorHash, p.creatorHash);
    // assertEquals(problem.userCode, p.userCode);
    assertEquals(problem.sentence, p.sentence);
    assertEquals(problem.answers[0], p.answers[0]);
    assertEquals(problem.answers[1], p.answers[1]);
    assertEquals(problem.answers[2], p.answers[2]);
    assertEquals(problem.answers[3], p.answers[3]);
    assertEquals(problem.choices[0], p.choices[0]);
    assertEquals(problem.choices[1], p.choices[1]);
    assertEquals(problem.choices[2], p.choices[2]);
    assertEquals(problem.choices[3], p.choices[3]);
    assertEquals(problem.creator, p.creator);
    assertEquals(problem.note, p.note);
    assertEquals(problem.imageAnswer, p.imageAnswer);
    assertEquals(problem.imageChoice, p.imageChoice);
    assertEquals(problem.voteGood, p.voteGood);
    assertEquals(problem.voteBad, p.voteBad);
    // assertEquals(problem.imageUrl, p.imageUrl);
    // assertEquals(problem.movieUrl, p.movieUrl);
    assertEquals(problem.indication, p.indication);
    // assertEquals(problem.indicationMessage, p.indicationMessage);

    assertFalse(form.htmlPlusOne.getHTML().isEmpty());
  }

  @Test
  public void testGetProblemShouldReturnWithIndicationIfNotChecked() {
    Date date = new Date();
    problem.indication = date;

    form.setProblem(problem);
    form.checkBoxUnindicate.setValue(false);

    PacketProblem p = form.getProblem();
    assertEquals(date, p.indication);
    assertNull(p.indicationResolved);
  }

  @Test
  public void testGetProblemShouldReturnWithIndicationResolvedIfChecked() {
    Date date = new Date();
    problem.indication = date;

    form.setProblem(problem);
    form.checkBoxUnindicate.setValue(true);

    PacketProblem p = form.getProblem();
    assertNull(p.indication);
    assertEquals(-1,
        p.indicationResolved.compareTo(new Date(System.currentTimeMillis() + 10 * 1000)));
  }

  @Test
  public void testGetProblemShouldReturnWithIndicationResolvedIfSet() {
    Date date = new Date();
    problem.indication = null;
    problem.indicationResolved = date;

    form.setProblem(problem);
    form.checkBoxUnindicate.setValue(false);

    PacketProblem p = form.getProblem();
    assertEquals(date, p.indicationResolved);
  }
}
