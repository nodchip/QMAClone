package tv.dyndns.kishibe.qmaclone.server.database;

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsResponse;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsThread;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketLinkData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMonth;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemCreationLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor.ThemeModeEditorStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;
import tv.dyndns.kishibe.qmaclone.server.PageView;
import tv.dyndns.kishibe.qmaclone.server.database.DirectDatabase.WrongAnswerHandler;
import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;
import tv.dyndns.kishibe.qmaclone.server.util.Normalizer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseTest {

  private static final int FAKE_USER_CODE = 12345678;
  private static final int FAKE_BBS_ID = -98543495;
  private static final String FAKE_REMOTE_ADDRESS = "1.2.3.4";
  private static final int FAKE_SCORE = 11111;
  private static final String FAKE_THEME = "THEME";
  private static final int FAKE_PROBLEM_ID = 11111111;
  private static final String FAKE_ANSWER = "fake answer";
  private static final int FAKE_COUNT = 22222;
  private static final String FAKE_GOOGLE_PLUS_ID = "fake google plus id";

  @Rule
  public GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
  @Inject
  private CachedDatabase database;
  @Mock
  private ResultSet mockResultSet;
  @Inject
  private QueryRunner runner;
  @Inject
  private WrongAnswerHandler wrongAnswerHandler;

  @Before
  public void setUp() {
    database.clearCache();
  }

  private static boolean contains(String sentence, String query) {
    sentence = Normalizer.normalize(sentence);
    query = Normalizer.normalize(query);
    return sentence.contains(query);
  }

  private static boolean equals(String a, String b) {
    a = Normalizer.normalize(a);
    b = Normalizer.normalize(b);
    return a.equals(b);
  }

  @Test
  public void testProblemIdsForReport() throws Exception {
    database.clearProblemIdFromReport(FAKE_USER_CODE);

    List<Integer> problemIds = new ArrayList<Integer>();
    problemIds.add(23456);
    problemIds.add(34567);
    problemIds.add(123456);
    database.addProblemIdsToReport(FAKE_USER_CODE, problemIds);

    // 追加した問題IDから問題が引けるか？
    List<PacketProblem> problems;

    problems = database.getUserProblemReport(FAKE_USER_CODE);
    assertNotNull(problems);
    for (PacketProblem problem : problems) {
      assertTrue(problemIds.contains(problem.id));
    }

    // 問題の削除が出来るか？
    database.removeProblemIdFromReport(FAKE_USER_CODE, 123456);
    problems = database.getUserProblemReport(FAKE_USER_CODE);
    assertNotNull(problems);
    for (PacketProblem problem : problems) {
      assertTrue(problem.id != 123456);
    }

    // 問題のクリアが出来るか？
    database.clearProblemIdFromReport(FAKE_USER_CODE);
    problems = database.getUserProblemReport(FAKE_USER_CODE);
    assertNotNull(problems);
    assertTrue(problems.isEmpty());
  }

  @Test
  public void testUserData() throws Exception {
    PacketUserData userData = TestDataProvider.getUserData();
    userData.userCode = FAKE_USER_CODE;

    database.setUserData(userData);

    PacketUserData userData2 = database.getUserData(FAKE_USER_CODE);

    assertEquals(userData2.userCode, FAKE_USER_CODE);
    assertEquals(userData.playerName, userData2.playerName);
  }

  @Test
  public void testUserDataWithGooglePlusId() throws Exception {
    PacketUserData userData = TestDataProvider.getUserData();
    userData.userCode = FAKE_USER_CODE;
    userData.googlePlusId = FAKE_GOOGLE_PLUS_ID;

    database.setUserData(userData);

    assertEquals(ImmutableList.of(userData),
        database.lookupUserCodeByGooglePlusId(FAKE_GOOGLE_PLUS_ID));
  }

  @Test
  public void disconnectUserCodeFromGooglePlusShould() throws Exception {
    PacketUserData userData = TestDataProvider.getUserData();
    userData.userCode = FAKE_USER_CODE;
    userData.googlePlusId = FAKE_GOOGLE_PLUS_ID;

    database.setUserData(userData);
    database.clearCache();
    database.disconnectUserCodeFromGooglePlus(FAKE_USER_CODE);

    assertEquals(Lists.<PacketUserData> newArrayList(),
        database.lookupUserCodeByGooglePlusId(FAKE_GOOGLE_PLUS_ID));
  }

  @Test
  public void testLoadPageView() throws Exception {
    PageView pageView = database.loadPageView();
    assertNotNull(pageView);
    pageView.numberOfPageView += 10;
    pageView.numberOfPlayers += 10;
    pageView.numberOfSessions += 10;

    database.savePageView(pageView);
    PageView pageView2 = database.loadPageView();
    assertNotNull(pageView2);
    assertEquals(pageView.numberOfPageView, pageView2.numberOfPageView);
    assertEquals(pageView.numberOfPlayers, pageView2.numberOfPlayers);
    assertEquals(pageView.numberOfSessions, pageView2.numberOfSessions);
  }

  /**
   * 問題を追加した後、取得できるかどうか
   * 
   * @throws Exception
   */
  @Test
  public void testGetProblemAfterAddProblem() throws Exception {
    PacketProblem expected = TestDataProvider.getProblem();
    expected.voteGood = 0;
    expected.voteBad = 0;
    expected.id = database.addProblem(expected);
    assertTrue(0 < expected.id);

    List<PacketProblem> problems = database.getProblem(Arrays.asList(expected.id));
    assertNotNull(problems);
    PacketProblem actual = problems.get(0);
    assertEquals(expected, actual);
  }

  /**
   * 問題を追加した後、検索できるかどうか
   * 
   * @throws Exception
   */
  @Test
  public void testSearchProblemAfterAddProblem() throws Exception {
    PacketProblem expected = TestDataProvider.getProblem();
    expected.id = database.addProblem(expected);
    assertTrue(0 < expected.id);

    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack-QMAClone/296
    // write直後にreadした場合、reopen()していない場合もここで引っかかる
    List<PacketProblem> problems = database.searchProblem(expected.sentence, null, false,
        EnumSet.of(expected.genre), EnumSet.of(expected.type), EnumSet.of(expected.randomFlag));
    int foundCount = 0;
    for (PacketProblem p : problems) {
      if (p.id == expected.id) {
        ++foundCount;
      }
    }
    assertEquals(1, foundCount);
  }

  /**
   * 問題を追加、更新した後、取得できるかどうか
   * 
   * @throws Exception
   */
  @Test
  public void testGetProblemAfterAddAndUpdateProblem() throws Exception {
    PacketProblem expected = TestDataProvider.getProblem();
    expected.voteGood = 0;
    expected.voteBad = 0;
    expected.id = database.addProblem(expected);
    assertTrue(0 < expected.id);

    expected.sentence = "ユニットテスト用問題文(更新後)";
    database.updateProblem(expected);

    List<PacketProblem> problems = database.getProblem(Arrays.asList(expected.id));
    assertNotNull(problems);
    PacketProblem actual = problems.get(0);
    assertEquals(expected, actual);
  }

  /**
   * 問題を追加、更新した後、検索できるかどうか
   * 
   * @throws Exception
   */
  @Test
  public void testSearchProblemAfterAddAndUpdateProblem() throws Exception {
    PacketProblem expected = TestDataProvider.getProblem();
    expected.id = database.addProblem(expected);
    assertTrue(0 < expected.id);

    expected.sentence = "ユニットテスト用問題文(更新後)";
    database.updateProblem(expected);

    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack-QMAClone/296
    List<PacketProblem> problems = database.searchProblem("ユニットテスト用問題文", null, false,
        EnumSet.of(expected.genre), EnumSet.of(expected.type), EnumSet.of(expected.randomFlag));
    int foundCount = 0;
    for (PacketProblem p : problems) {
      if (p.id == expected.id) {
        ++foundCount;
      }
    }
    assertEquals(1, foundCount);
  }

  /**
   * 問題を追加、最小情報のみ更新後、取得できるかどうか
   * 
   * @throws Exception
   */
  @Test
  public void testGetProblemAfterAddProblemAndUpdateMinimumProblem() throws Exception {
    PacketProblem expected = TestDataProvider.getProblem();
    expected.voteGood = 0;
    expected.voteBad = 0;
    expected.id = database.addProblem(expected);
    assertTrue(0 < expected.id);

    // 最小情報の更新ではgood/badしか更新されない点に注意する
    PacketProblemMinimum expectedMinimum = expected.asMinimum();
    expectedMinimum.good = 1234;
    expectedMinimum.bad = 2345;
    database.updateMinimumProblem(expectedMinimum);

    expected.good = 1234;
    expected.bad = 2345;

    List<PacketProblem> problems = database.getProblem(Arrays.asList(expected.id));
    assertNotNull(problems);
    PacketProblem actual = problems.get(0);
    assertEquals(expected, actual);

  }

  /**
   * 問題を追加後、最小情報のみ取得できるかどうか
   * 
   * @throws Exception
   */
  @Test
  public void testGetProblemMinimumAfterAddProblem() throws Exception {
    PacketProblem expected = TestDataProvider.getProblem();
    expected.voteGood = 0;
    expected.voteBad = 0;
    expected.id = database.addProblem(expected);
    assertTrue(0 < expected.id);

    PacketProblemMinimum actualMinimum = database.getProblemMinimum(expected.id);
    assertEquals(expected.asMinimum(), actualMinimum);
  }

  /**
   * 問題を追加後、最新の問題一覧に追加されるかどうか
   * 
   * @throws Exception
   */
  @Test
  public void testGetProblemCreationHistoryAfterAddProblem() throws Exception {
    PacketProblem expected = TestDataProvider.getProblem();
    expected.id = database.addProblem(expected);
    assertTrue(0 < expected.id);

    List<PacketProblemCreationLog> logs = database.getProblemCreationHistory(expected.id);
    for (PacketProblemCreationLog log : logs) {
      assertEquals(expected.creator, log.name);
    }
  }

  /**
   * 削除された文章が検索に反映される BugTrack-QMAClone/604 - QMAClone wiki
   * http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F604
   */
  @Test
  public void updateProblemShouldHandleSentenceUpdate() throws Exception {
    PacketProblem problem = TestDataProvider.getProblem();
    problem.sentence = "A%nB%nC%nD";
    problem.type = ProblemType.Rensou;
    problem.id = database.addProblem(problem);
    assertTrue(0 < problem.id);

    problem.sentence = "A%w%nB%w%nC%w%nD";
    database.updateProblem(problem);

    List<PacketProblem> problems = database.searchProblem(null, "%n -%w", true, null, null, null);
    for (PacketProblem p : problems) {
      assertFalse(p.id == problem.id);
    }
  }

  /**
   * 問題番号87662の問題作成者に関して BugTrack-QMAClone/606 - QMAClone wiki
   * http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F606
   */
  @Test
  public void updateProblemShouldHandleDifferentCreator() throws Exception {
    PacketProblem problem = TestDataProvider.getProblem();
    problem.creator = "AAAAA";
    problem.id = database.addProblem(problem);
    assertTrue(0 < problem.id);

    problem.creator = "BBBBB";
    database.updateProblem(problem);

    problem.creator = "AAAAA";
    database.updateProblem(problem);

    List<PacketProblem> problems = database.searchProblem(null, "BBBBB", true, null, null, null);
    for (PacketProblem p : problems) {
      assertFalse(p.id == problem.id);
    }
  }

  @Test
  public void testSearchProblem() throws Exception {
    List<PacketProblem> problems;

    problems = database.searchProblem("クイズ", null, false, EnumSet.noneOf(ProblemGenre.class),
        EnumSet.noneOf(ProblemType.class), EnumSet.noneOf(RandomFlag.class));
    assertNotNull(problems);
    assertFalse(problems.isEmpty());
    for (PacketProblem problem : problems) {
      String searchQuery = problem.getSearchDocument();
      assertThat(searchQuery, containsString("クイズ"));
    }

    problems = database.searchProblem(null, "チップ", false, EnumSet.noneOf(ProblemGenre.class),
        EnumSet.noneOf(ProblemType.class), EnumSet.noneOf(RandomFlag.class));
    assertNotNull(problems);
    assertFalse(problems.isEmpty());
    for (PacketProblem problem : problems) {
      assertThat(problem.creator, containsString("チップ"));
    }

    problems = database.searchProblem(null, "ノドチップ", true, EnumSet.noneOf(ProblemGenre.class),
        EnumSet.noneOf(ProblemType.class), EnumSet.noneOf(RandomFlag.class));
    assertNotNull(problems);
    assertFalse(problems.isEmpty());
    for (PacketProblem problem : problems) {
      assertTrue(equals(problem.creator, "ノドチップ"));
    }

    problems = database.searchProblem(null, null, false, EnumSet.of(ProblemGenre.Anige),
        EnumSet.noneOf(ProblemType.class), EnumSet.noneOf(RandomFlag.class));
    assertNotNull(problems);
    assertFalse(problems.isEmpty());
    for (PacketProblem problem : problems) {
      assertEquals(ProblemGenre.Anige, problem.genre);
    }

    problems = database.searchProblem(null, null, false,
        EnumSet.of(ProblemGenre.Anige, ProblemGenre.Geinou),
        EnumSet.of(ProblemType.Marubatsu, ProblemType.Rensou),
        EnumSet.of(RandomFlag.Random1, RandomFlag.Random3));
    assertNotNull(problems);
    assertFalse(problems.isEmpty());
    for (PacketProblem problem : problems) {
      assertThat(problem.genre, isOneOf(ProblemGenre.Anige, ProblemGenre.Geinou));
      assertThat(problem.type, isOneOf(ProblemType.Marubatsu, ProblemType.Rensou));
      assertThat(problem.randomFlag, isOneOf(RandomFlag.Random1, RandomFlag.Random3));
    }

    problems = database.searchProblem("クイズ マジック アカデミー", null, false,
        EnumSet.noneOf(ProblemGenre.class), EnumSet.noneOf(ProblemType.class),
        EnumSet.noneOf(RandomFlag.class));

    assertNotNull(problems);
    assertFalse(problems.isEmpty());
    for (PacketProblem problem : problems) {
      String searchQuery = problem.getSearchDocument();
      assertThat(searchQuery, containsString("クイズ"));
      assertThat(searchQuery, containsString("マジック"));
      assertThat(searchQuery, containsString("アカデミー"));
    }

    problems = database.searchProblem("floccinaucinihilipilification", null, false,
        EnumSet.noneOf(ProblemGenre.class), EnumSet.noneOf(ProblemType.class),
        EnumSet.noneOf(RandomFlag.class));

    assertNotNull(problems);
    assertTrue(problems.isEmpty());
  }

  @Test
  public void testSearchProblemNot() throws Exception {
    List<PacketProblem> problems;

    // not演算子で期待したもの以上が除外されるバグの回帰テスト
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack-QMAClone/325
    problems = database.searchProblem("フルメタル・パニック -ノベル", null, false,
        EnumSet.noneOf(ProblemGenre.class), EnumSet.noneOf(ProblemType.class),
        EnumSet.noneOf(RandomFlag.class));

    assertNotNull(problems);
    assertFalse(problems.isEmpty());
    for (PacketProblem problem : problems) {
      String searchQuery = problem.getSearchDocument();
      assertThat(searchQuery, containsString("フルメタル・パニック"));
      assertFalse(contains(searchQuery, "ノベル"));
    }
  }

  private Matcher<String> containsString(final String query) {
    return new BaseMatcher<String>() {
      private String sentence;

      @Override
      public void describeTo(Description arg0) {
        arg0.appendText(String.format("\"%s\" does not contains \"%s\"", sentence, query));
      }

      @Override
      public boolean matches(Object arg0) {
        String sentence = (String) arg0;
        this.sentence = Normalizer.normalize(sentence);
        return this.sentence.contains(Normalizer.normalize(query));
      }
    };
  }

  @Test
  public void testSearchProblemNotCount() throws Exception {
    List<PacketProblem> problems;

    // not演算子で除外される問題数
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack-QMAClone/325
    problems = database.searchProblem("フルメタル・パニック", null, false,
        EnumSet.noneOf(ProblemGenre.class), EnumSet.noneOf(ProblemType.class),
        EnumSet.noneOf(RandomFlag.class));

    assertNotNull(problems);
    assertFalse(problems.isEmpty());
    int a = problems.size();

    problems = database.searchProblem("フルメタル・パニック 小説", null, false,
        EnumSet.noneOf(ProblemGenre.class), EnumSet.noneOf(ProblemType.class),
        EnumSet.noneOf(RandomFlag.class));

    assertNotNull(problems);
    assertFalse(problems.isEmpty());
    int b = problems.size();

    problems = database.searchProblem("フルメタル・パニック -小説", null, false,
        EnumSet.noneOf(ProblemGenre.class), EnumSet.noneOf(ProblemType.class),
        EnumSet.noneOf(RandomFlag.class));

    assertNotNull(problems);
    assertFalse(problems.isEmpty());
    int c = problems.size();

    assertEquals(a, b + c);
  }

  @Ignore
  @Test
  public void searchProblemShouldWorkWithEscapeSequences() throws Exception {
    List<PacketProblem> problems = database.searchProblem("%n -%w", null, false,
        EnumSet.noneOf(ProblemGenre.class), EnumSet.of(ProblemType.Rensou),
        EnumSet.noneOf(RandomFlag.class));
    assertNotNull(problems);
    assertFalse(problems.isEmpty());
    for (PacketProblem problem : problems) {
      String searchQuery = problem.getSearchDocument();
      assertThat(searchQuery, containsString("%n"));
      assertFalse(contains(searchQuery, "%w"));
      assertEquals(ProblemType.Rensou, problem.type);
    }
  }

  @Test
  public void testSearchSimilarProblem() throws Exception {
    PacketProblem problem = new PacketProblem();
    problem.id = 12345;
    problem.sentence = "「Google」で2020年完成予定の 人工知能で会話しつつ検索などを行うサービスを 「Google　○○○○○」という？ ";
    problem.answers = new String[] { "ＢＲＡＩＮ", "", "", "" };
    List<PacketProblem> problems = database.searchSimilarProblemFromDatabase(problem);
    assertFalse(problems.isEmpty());
    // System.out.println("testSearchSimilarProblem()");
    // for (PacketProblem p : problems) {
    // System.out.println(p.toString());
    // }
  }

  @Test
  public void testPlayerAnswers() throws Exception {
    // プレイヤー解答の追加と取得
    database.addPlayerAnswers(123456789, ProblemType.Typing, Arrays.asList("テスト"));
    for (PacketWrongAnswer wrongAnswer : database.getPlayerAnswers(123456789)) {
      assertEquals(wrongAnswer.answer, "テスト");
    }

    // プレイヤー解答の削除
    database.removePlayerAnswers(123456789);
    assertTrue(database.getPlayerAnswers(123456789).isEmpty());
  }

  @Test
  public void addPlayerAnswerShouldNormalizeSenmusubiAnswers() throws Exception {
    database.removePlayerAnswers(FAKE_PROBLEM_ID);
    database.addPlayerAnswers(FAKE_PROBLEM_ID, ProblemType.Senmusubi,
        Arrays.asList("\nC<--->c\nB<--->b\nA<--->a\n"));
    database.addPlayerAnswers(FAKE_PROBLEM_ID, ProblemType.Senmusubi,
        Arrays.asList("C<--->c\nA<--->a\nB<--->b"));
    for (PacketWrongAnswer wrongAnswer : database.getPlayerAnswers(FAKE_PROBLEM_ID)) {
      assertEquals("A<--->a\nB<--->b\nC<--->c", wrongAnswer.answer);
      assertEquals(2, wrongAnswer.count);
    }
  }

  @Test
  public void getPlayerAnswersShouldMergeAnswers() throws Exception {
    database.removePlayerAnswers(FAKE_PROBLEM_ID);
    database.addPlayerAnswers(FAKE_PROBLEM_ID, ProblemType.Senmusubi,
        Arrays.asList("\nC---c\nB---b\nA---a\n"));
    database.addPlayerAnswers(FAKE_PROBLEM_ID, ProblemType.Senmusubi,
        Arrays.asList("C<--->c\nA<--->a\nB<--->b"));
    for (PacketWrongAnswer wrongAnswer : database.getPlayerAnswers(FAKE_PROBLEM_ID)) {
      assertEquals("A<--->a\nB<--->b\nC<--->c", wrongAnswer.answer);
      assertEquals(2, wrongAnswer.count);
    }
  }

  @Test
  public void testGetRankingData() throws Exception {
    List<List<PacketRankingData>> ranking = database.getGeneralRankingData();
    assertEquals(6, ranking.size());
  }

  @Test
  public void testGetThemeModeEditors() throws Exception {
    List<PacketThemeModeEditor> editors = database.getThemeModeEditors();
    assertNotNull(editors);
    assertFalse(editors.isEmpty());
    for (PacketThemeModeEditor editor : editors) {
      assertThat(editor.userCode, greaterThanOrEqualTo(0));
      assertNotNull(editor.name);
      assertNotNull(editor.themeModeEditorStatus);
    }
  }

  @Test
  public void testGetThemeModeEditorsStatus() throws Exception {
    QueryRunner runner = getQueryRunner();
    runner.update("REPLACE theme_mode_editor (userCode, status) VALUES (0, 1)");

    ThemeModeEditorStatus status = database.getThemeModeEditorsStatus(0);
    assertNotNull(status);
    assertEquals(ThemeModeEditorStatus.Accepted, status);
  }

  private QueryRunner getQueryRunner() throws Exception {
    return ((DirectDatabase) ((CachedDatabase) database).database).runner;
  }

  @Test
  public void testAddRemoveIgnoreUserCode() throws Exception {
    runner.update("DELETE FROM ignore_id WHERE USER_CODE = 111");
    database.addIgnoreUserCode(111, 222);
    assertEquals(1L, (long) runner.query(
        "SELECT COUNT(*) FROM ignore_id WHERE USER_CODE = 111 AND TARGET_USER_CODE = 222",
        new ScalarHandler<Long>()));
    database.removeIgnoreUserCode(111, 222);
    assertEquals(0L, (long) runner.query(
        "SELECT COUNT(*) FROM ignore_id WHERE USER_CODE = 111 AND TARGET_USER_CODE = 222",
        new ScalarHandler<Long>()));
  }

  @Test
  public void testAddGetServerIgnoreUserCode() throws Exception {
    runner.update("DELETE FROM ignore_id WHERE USER_CODE = 0");
    assertEquals(0L, (long) runner.query(
        "SELECT COUNT(*) FROM ignore_id WHERE USER_CODE = 0 AND TARGET_USER_CODE = 111",
        new ScalarHandler<Long>()));
    assertTrue(database.getServerIgnoreUserCode().isEmpty());

    database.addServerIgnoreUserCode(111);
    assertEquals(1L, (long) runner.query(
        "SELECT COUNT(*) FROM ignore_id WHERE USER_CODE = 0 AND TARGET_USER_CODE = 111",
        new ScalarHandler<Long>()));
    assertThat(database.getServerIgnoreUserCode(), hasItem(111));
  }

  @Test
  public void testAddChatLog() throws Exception {
    long count = (long) runner.query("SELECT COUNT(*) FROM chat_log", new ScalarHandler<Long>());
    database.addChatLog(new PacketChatMessage());
    assertEquals(count + 1,
        (long) runner.query("SELECT COUNT(*) FROM chat_log", new ScalarHandler<Long>()));
  }

  @Test
  public void testGetProblemCreationHistory() throws Exception {
    long countUserCode = (long) runner.query(
        "SELECT COUNT(*) FROM creation_log WHERE USER_CODE = 111", new ScalarHandler<Long>());
    assertEquals(countUserCode, database.getNumberOfCreationLogWithUserCode(111, 0));

    long countMachineIp = (long) runner.query(
        "SELECT COUNT(*) FROM creation_log WHERE MACHINE_IP = '222'", new ScalarHandler<Long>());
    List<PacketProblemCreationLog> log0 = database.getProblemCreationHistory(0);
    assertEquals(countMachineIp, database.getNumberOfCreationLogWithUserCode(111, 0));

    PacketProblem problem = TestDataProvider.getProblem();
    problem.id = 0;
    database.addCreationLog(problem, 111, "222");

    List<PacketProblemCreationLog> log1 = database.getProblemCreationHistory(0);
    assertThat(log1.size()).isInclusivelyInRange(log0.size(), log0.size() + 1);

    assertEquals(countUserCode + 1, database.getNumberOfCreationLogWithUserCode(111, 0));
    assertEquals(countMachineIp + 1, database.getNumberOfCreationLogWithUserCode(111, 0));
  }

  @Test
  public void testBuildGetBbsThread() throws Exception {
    long count = runner.query("SELECT COUNT(*) FROM bbs_thread WHERE bbsId = ?",
        new ScalarHandler<Long>(), FAKE_BBS_ID);
    assertEquals(count, database.getBbsThreads(FAKE_BBS_ID, 0, Integer.MAX_VALUE).size());
    assertEquals(count, database.getNumberOfBbsThread(FAKE_BBS_ID));

    PacketBbsThread thread = new PacketBbsThread();
    thread.title = "title";

    PacketBbsResponse response = new PacketBbsResponse();
    response.name = "name";
    response.userCode = 123456789;
    response.remoteAddress = "remote address";
    response.dispInfo = 0;
    response.body = "body";

    database.buildBbsThread(FAKE_BBS_ID, thread, response);
    assertEquals(count + 1, database.getBbsThreads(FAKE_BBS_ID, 0, Integer.MAX_VALUE).size());
    assertEquals(count + 1, database.getNumberOfBbsThread(FAKE_BBS_ID));

    int threadId = runner.query("SELECT MAX(id) FROM bbs_thread", new ScalarHandler<BigInteger>())
        .intValue();
    List<PacketBbsResponse> responses = database.getBbsResponses(threadId, 10);
    assertThat(responses.size(), greaterThan(0));
    assertThat(responses.size(), lessThanOrEqualTo(11));
  }

  @Test
  public void testWriteGetBbsResponses() throws Exception {
    int count = database.getBbsResponses(0, Integer.MAX_VALUE).size();
    PacketBbsResponse response = new PacketBbsResponse();
    response.threadId = 0;
    response.name = "";
    response.userCode = 123456789;
    response.remoteAddress = "remote address";
    response.dispInfo = 0;
    response.body = "body";

    database.writeToBbs(response, true);
    assertEquals(count + 1, database.getBbsResponses(0, Integer.MAX_VALUE).size());
  }

  @Test
  public void testIsUsedUserCode() throws Exception {
    runner.update("DELETE FROM player WHERE USER_CODE = 0");
    assertFalse(database.isUsedUserCode(0));
    database.setUserData(new PacketUserData());
    assertTrue(database.isUsedUserCode(0));
    runner.update("DELETE FROM player WHERE USER_CODE = 0");
  }

  @Test
  public void testAddUpdateRemoveGetLinkData() throws Exception {
    runner.update("DELETE FROM link");
    assertTrue(database.getLinkDatas(0, Integer.MAX_VALUE).isEmpty());
    assertEquals(0, database.getNumberOfLinkDatas());

    PacketLinkData linkData = new PacketLinkData();
    linkData.homePageName = "home page name";
    linkData.authorName = "author name";
    linkData.url = "url";
    linkData.bannerUrl = "url";
    linkData.description = "description";

    database.addLinkData(linkData);
    assertEquals(1, database.getLinkDatas(0, Integer.MAX_VALUE).size());
    assertEquals(1, database.getNumberOfLinkDatas());

    linkData.id = database.getLinkDatas(0, Integer.MAX_VALUE).get(0).id;
    linkData.userCode = 111;
    database.updateLinkData(linkData);
    assertEquals(111, database.getLinkDatas(0, Integer.MAX_VALUE).get(0).userCode);
    assertEquals(1, database.getNumberOfLinkDatas());

    database.removeLinkData(linkData.id);
    assertTrue(database.getLinkDatas(0, Integer.MAX_VALUE).isEmpty());
    assertEquals(0, database.getNumberOfLinkDatas());
  }

  @Test
  public void testGetNumberOfActiveUsers() throws Exception {
    runner.update("DELETE FROM player WHERE USER_CODE = 111");
    database.clearCache();
    int count = database.getNumberOfActiveUsers();

    PacketUserData data = new PacketUserData();
    data.userCode = 111;
    data.playCount = 100;
    database.setUserData(data);
    assertTrue(database.getNumberOfActiveUsers() == count
        || database.getNumberOfActiveUsers() == count + 1);
  }

  @Test
  public void testGetRatingGroupedByPrefecture() throws Exception {
    Map<Integer, List<Integer>> rating = database.getRatingGroupedByPrefecture();
    for (Entry<Integer, List<Integer>> entry : rating.entrySet()) {
      assertFalse(entry.getValue().isEmpty());
    }
  }

  @Test
  public void testGetAddGetRatingHistory() throws Exception {
    runner.update("DELETE FROM rating_history WHERE USER_CODE = 111");
    assertTrue(database.getRatingHistory(111).isEmpty());

    database.addRatingHistory(111, 0);
    assertEquals(1, database.getRatingHistory(111).size());
  }

  @Test
  public void testGetWholeRating() throws Exception {
    assertNotNull(database.getWholeRating());
  }

  @Test
  public void testGetAddRemoveThemeModeQuery() throws Exception {
    runner.update("DELETE FROM theme_mode WHERE THEME = 'test'");
    assertThat(getThemeModeQueries(), not(hasKey("test")));

    database.addThemeModeQuery("test", "query");
    assertEquals(ImmutableList.of("query"), getThemeModeQueries().get("test"));

    database.removeThemeModeQuery("test", "query");
    assertThat(getThemeModeQueries(), not(hasKey("test")));
  }

  @Test
  public void getThemeModeQueriesShouldLimitReturnValues() throws Exception {
    assertThat(database.getThemeModeQueries(), not(empty()));
  }

  @Test
  public void getNumberOfThemeQueriesShouldReturnProperValue() throws Exception {
    assertThat(database.getNumberOfThemeQueries(), greaterThan(0));
  }

  private Map<String, List<String>> getThemeModeQueries() throws DatabaseException {
    Map<String, List<String>> queries = Maps.newHashMap();
    for (PacketThemeQuery query : database.getThemeModeQueries()) {
      if (!queries.containsKey(query.getTheme())) {
        queries.put(query.getTheme(), new ArrayList<String>());
      }
      queries.get(query.getTheme()).add(query.query);
    }
    return queries;
  }

  @Test
  public void addThemeModeQueryShouldAcceptSecondQuery() throws Exception {
    // テーマモードの編集において、ジャンル（例えば「アニメ＆ゲーム」）とサブジャンル（例えば「ランダム１」）と検索ワード（例えば「ノドチップ」）のような組み合わせを複数登録することができない
    // BugTrack-QMAClone/520
    runner.update("DELETE FROM theme_mode WHERE THEME = 'test'");
    assertThat(getThemeModeQueries(), not(hasKey("test")));

    database.addThemeModeQuery("test", "ジャンル:アニメ＆ゲーム ランダム:1 なのは");
    assertEquals(ImmutableList.of("ジャンル:アニメ＆ゲーム ランダム:1 なのは"), getThemeModeQueries().get("test"));

    database.addThemeModeQuery("test", "ジャンル:アニメ＆ゲーム ランダム:1 まどか");
    assertEquals(ImmutableList.of("ジャンル:アニメ＆ゲーム ランダム:1 なのは", "ジャンル:アニメ＆ゲーム ランダム:1 まどか"),
        getThemeModeQueries().get("test"));
  }

  @Test
  public void testUpdateThemeModeScore() throws Exception {
    runner.update("DELETE FROM theme_mode_score WHERE USER_CODE = 111");
    database.updateThemeModeScore(111, "test", 0);
    assertEquals(1L, (long) runner.query(
        "SELECT COUNT(*) FROM theme_mode_score WHERE USER_CODE = 111", new ScalarHandler<Long>()));
  }

  @Test
  public void testVoteToProblem() throws Exception {
    long voteGood = (long) MoreObjects.firstNonNull(
        runner.query("SELECT VOTE_GOOD FROM problem WHERE ID = 1", new ScalarHandler<Long>()), 0L);
    long voteBad = (long) MoreObjects.firstNonNull(
        runner.query("SELECT VOTE_BAD FROM problem WHERE ID = 1", new ScalarHandler<Long>()), 0L);

    database.voteToProblem(1, true, "");
    assertEquals(voteGood + 1, (long) MoreObjects.firstNonNull(
        runner.query("SELECT VOTE_GOOD FROM problem WHERE ID = 1", new ScalarHandler<Long>()), 0L));
    assertEquals(voteBad, (long) MoreObjects.firstNonNull(
        runner.query("SELECT VOTE_BAD FROM problem WHERE ID = 1", new ScalarHandler<Long>()), 0L));

    database.voteToProblem(1, false, "");
    assertEquals(voteGood + 1, (long) MoreObjects.firstNonNull(
        runner.query("SELECT VOTE_GOOD FROM problem WHERE ID = 1", new ScalarHandler<Long>()), 0L));
    assertEquals(voteBad + 1, (long) MoreObjects.firstNonNull(
        runner.query("SELECT VOTE_BAD FROM problem WHERE ID = 1", new ScalarHandler<Long>()), 0L));
  }

  @Test
  public void testResetVote() throws Exception {
    runner.update("UPDATE problem SET VOTE_GOOD = 123, VOTE_BAD = 456 WHERE ID = 1");

    database.resetVote(1);
    assertEquals(0, (long) MoreObjects.firstNonNull(
        runner.query("SELECT VOTE_GOOD FROM problem WHERE ID = 1", new ScalarHandler<Long>()), 0L));
    assertEquals(0, (long) MoreObjects.firstNonNull(
        runner.query("SELECT VOTE_BAD FROM problem WHERE ID = 1", new ScalarHandler<Long>()), 0L));
  }

  @Test
  public void testClearGetProblemFeedback() throws Exception {
    runner.update("DELETE FROM problem_questionnaire WHERE problemId = 0");
    assertTrue(database.getProblemFeedback(0).isEmpty());

    database.voteToProblem(0, false, "");
    assertEquals(1, database.getProblemFeedback(0).size());

  }

  @Test
  public void testProcessProblems() throws Exception {
    final long[] counter = new long[1];
    database.processProblems(new ProblemProcessable() {
      @Override
      public void process(PacketProblem problem) throws Exception {
        ++counter[0];
      }
    });
    assertEquals((long) runner.query("SELECT COUNT(*) FROM problem", new ScalarHandler<Long>()),
        counter[0]);
  }

  @Test
  public void testGetLastestProblems() throws Exception {
    assertNotNull(database.getLastestProblems());
  }

  @Test
  public void testUpdateGetThemeModeEditorsStatus() throws Exception {
    database.updateThemeModeEdtorsStatus(111, ThemeModeEditorStatus.Applying);
    assertEquals(ThemeModeEditorStatus.Applying, database.getThemeModeEditorsStatus(111));

    database.updateThemeModeEdtorsStatus(111, ThemeModeEditorStatus.Accepted);
    assertEquals(ThemeModeEditorStatus.Accepted, database.getThemeModeEditorsStatus(111));
  }

  @Test
  public void testGetNumberOfChatLog() throws Exception {
    assertTrue(database.getNumberOfChatLog() > 0);
  }

  @Test
  public void testGetChatLogId() throws Exception {
    assertThat(database.getChatLogId(2009, 6, 6, 16, 45, 54), isOneOf(1787420, 1788066));
  }

  @Test
  public void testGetChatLog() throws Exception {
    List<PacketChatMessage> chatData = database.getChatLog(1787420);
    assertNotNull(chatData);
    assertFalse(chatData.isEmpty());
    for (int index = 0; index < chatData.size(); ++index) {
      PacketChatMessage data = chatData.get(index);
      assertEquals(1787420 + index, data.resId);
    }
  }

  @Test
  public void testGetNumberOfCreationLogWithUserCode() throws Exception {
    QueryRunner runner = getQueryRunner();
    long date = System.currentTimeMillis();
    runner
        .update(
            "INSERT creation_log (PROBLEM_ID, USER_CODE, DATE, MACHINE_IP) VALUES (123456, 123456789, ?, '1.2.3.4')",
            new Timestamp(date));

    assertThat(database.getNumberOfCreationLogWithUserCode(123456789, date - 1000),
        greaterThanOrEqualTo(1));
  }

  @Test
  public void testGetNumberOfCreationLogWithMachineIp() throws Exception {
    QueryRunner runner = getQueryRunner();
    long date = System.currentTimeMillis();
    runner
        .update(
            "INSERT creation_log (PROBLEM_ID, USER_CODE, DATE, MACHINE_IP) VALUES (123456, 123456789, ?, '1.2.3.4')",
            new Timestamp(date));

    assertThat(database.getNumberOfCreationLogWithUserCode(123456789, date - 1000),
        greaterThanOrEqualTo(1));
  }

  @Test
  public void testUserDataHandlerShouldReturnUserData() throws Exception {
    expectResultSetReturnsUserData();

    when(mockResultSet.getString("CORRECT_COUNT"))
        .thenReturn(
            "0,0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,8,0,9,0,10,0,11,0,12,0,13,0,14,0,15,0,16,0,17,0,18,0,19,0,20,0,21,0\n"
                + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n"
                + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n"
                + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n"
                + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n"
                + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n");

    PacketUserData data = DirectDatabase.userDataHandler.handle(mockResultSet).get(0);

    assertEquals(0, data.correctCount[0][0][0]);
    assertEquals(1, data.correctCount[0][1][0]);
    assertEquals(2, data.correctCount[0][2][0]);
    assertEquals(3, data.correctCount[0][3][0]);
    assertEquals(4, data.correctCount[0][4][0]);
    assertEquals(5, data.correctCount[0][5][0]);
    assertEquals(6, data.correctCount[0][6][0]);
    assertEquals(7, data.correctCount[0][7][0]);
    assertEquals(8, data.correctCount[0][8][0]);
    assertEquals(9, data.correctCount[0][9][0]);
    assertEquals(10, data.correctCount[0][10][0]);
    assertEquals(11, data.correctCount[0][11][0]);
    assertEquals(12, data.correctCount[0][12][0]);
    assertEquals(13, data.correctCount[0][13][0]);
    assertEquals(14, data.correctCount[0][14][0]);
    assertEquals(15, data.correctCount[0][15][0]);
    assertEquals(16, data.correctCount[0][16][0]);
    assertEquals(17, data.correctCount[0][17][0]);
    assertEquals(18, data.correctCount[0][18][0]);
    assertEquals(19, data.correctCount[0][19][0]);
    assertEquals(20, data.correctCount[0][20][0]);
    assertEquals(21, data.correctCount[0][21][0]);
  }

  @Test
  public void testUserDataHandlerShouldUpgradeOldUserData() throws Exception {
    expectResultSetReturnsUserData();

    when(mockResultSet.getString("CORRECT_COUNT")).thenReturn(
        "0,0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,8,0,9,0,10,0,11,0,12,0,13,0,14,0,15,0,16,0,17,0,18,0,19,0,20,0\n"
            + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n"
            + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n"
            + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n"
            + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n"
            + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n");

    PacketUserData data = DirectDatabase.userDataHandler.handle(mockResultSet).get(0);

    assertEquals(0, data.correctCount[0][0][0]);
    assertEquals(1, data.correctCount[0][1][0]);
    assertEquals(2, data.correctCount[0][2][0]);
    assertEquals(3, data.correctCount[0][3][0]);
    assertEquals(4, data.correctCount[0][4][0]);
    assertEquals(5, data.correctCount[0][5][0]);
    assertEquals(6, data.correctCount[0][6][0]);
    assertEquals(7, data.correctCount[0][7][0]);
    assertEquals(8, data.correctCount[0][8][0]);
    assertEquals(9, data.correctCount[0][9][0]);
    assertEquals(10, data.correctCount[0][10][0]);
    assertEquals(11, data.correctCount[0][11][0]);
    assertEquals(12, data.correctCount[0][12][0]);
    assertEquals(13, data.correctCount[0][13][0]);
    assertEquals(14, data.correctCount[0][14][0]);
    assertEquals(15, data.correctCount[0][15][0]);
    assertEquals(0, data.correctCount[0][16][0]);
    assertEquals(16, data.correctCount[0][17][0]);
    assertEquals(17, data.correctCount[0][18][0]);
    assertEquals(18, data.correctCount[0][19][0]);
    assertEquals(19, data.correctCount[0][20][0]);
    assertEquals(20, data.correctCount[0][21][0]);
  }

  private void expectResultSetReturnsUserData() throws Exception {
    PacketUserData expected = TestDataProvider.getUserData();
    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    when(mockResultSet.getInt("USER_CODE")).thenReturn(expected.userCode);
    when(mockResultSet.getString("NAME")).thenReturn(expected.playerName);
    when(mockResultSet.getString("GREETING")).thenReturn(expected.greeting);
    when(mockResultSet.getInt("HIGH_SCORE")).thenReturn(expected.highScore);
    when(mockResultSet.getInt("AVERAGE_SCORE")).thenReturn(expected.averageScore);
    when(mockResultSet.getInt("PLAY_COUNT")).thenReturn(expected.playCount);
    when(mockResultSet.getInt("VICTORY_POINT")).thenReturn(expected.rating);
    when(mockResultSet.getInt("LEVEL_NAME")).thenReturn(expected.levelName);
    when(mockResultSet.getInt("LEVEL_NUMBER")).thenReturn(expected.levelNumber);
    when(mockResultSet.getFloat("AVERAGE_RANK")).thenReturn(expected.averageRank);
    when(mockResultSet.getInt("GENRE")).thenReturn(ProblemGenre.toBitFlag(expected.genres));
    when(mockResultSet.getInt("TYPE")).thenReturn(ProblemType.toBitFlag(expected.types));
    when(mockResultSet.getInt("CLASS_LEVEL")).thenReturn(expected.classLevel);
    when(mockResultSet.getString("IMAGE_FILE_NAME")).thenReturn(expected.imageFileName);
    when(mockResultSet.getBoolean("PLAY_SOUND")).thenReturn(expected.playSound);
    when(mockResultSet.getBoolean("MULTI_GENRE")).thenReturn(expected.multiGenre);
    when(mockResultSet.getBoolean("MULTI_TYPE")).thenReturn(expected.multiType);
    when(mockResultSet.getInt("DIFFICULT_SELECT")).thenReturn(expected.difficultSelect);
    when(mockResultSet.getBoolean("RANKING_MOVE")).thenReturn(expected.rankingMove);
    when(mockResultSet.getInt("BBS_DISP_INFO")).thenReturn(expected.bbsDispInfo);
    when(mockResultSet.getBoolean("BBS_AGE")).thenReturn(expected.bbsAge);
    when(mockResultSet.getInt("TIMER_MODE")).thenReturn(expected.timerMode);
    when(mockResultSet.getInt("PREFECTURE")).thenReturn(expected.prefecture);
    when(mockResultSet.getBoolean("CHAT")).thenReturn(expected.chat);
    when(mockResultSet.getInt("NEW_AND_OLD")).thenReturn(expected.newAndOldProblems.ordinal());
    when(mockResultSet.getBoolean("PUBLIC_EVENT")).thenReturn(expected.publicEvent);
    when(mockResultSet.getBoolean("HIDE_ANSWER")).thenReturn(expected.hideAnswer);
    when(mockResultSet.getBoolean("SHOW_INFO")).thenReturn(expected.showInfo);
    when(mockResultSet.getBoolean("REFLECT_EVENT_RESULT")).thenReturn(expected.reflectEventResult);
    when(mockResultSet.getInt("WEB_SOCKET_USAGE")).thenReturn(expected.webSocketUsage.getIndex());
    when(mockResultSet.getInt("VOLATILITY")).thenReturn(expected.volatility);
    when(mockResultSet.getBoolean("QWERTY_HIRAGANA")).thenReturn(expected.qwertyHiragana);
    when(mockResultSet.getBoolean("QWERTY_KATAKANA")).thenReturn(expected.qwertyKatakana);
    when(mockResultSet.getBoolean("QWERTY_ALPHABET")).thenReturn(expected.qwertyAlphabet);
    when(mockResultSet.getBoolean("REGISTER_CREATED_PROBLEM")).thenReturn(
        expected.registerCreatedProblem);
    when(mockResultSet.getBoolean("REGISTER_INDICATED_PROBLEM")).thenReturn(
        expected.registerIndicatedProblem);
    when(mockResultSet.getString("THEME")).thenReturn(expected.theme);
  }

  @Test
  public void getIndicatedProblemsShouldReturnIndicatedProblems() throws Exception {
    List<PacketProblem> indicatedProblems = database.getIndicatedProblems();
    assertThat(indicatedProblems, not(empty()));
    for (PacketProblem problem : indicatedProblems) {
      assertTrue(problem.indication != null
          || problem.indicationResolved != null
          && problem.indicationResolved.compareTo(new Date(System.currentTimeMillis() - 7L * 24
              * 60 * 60 * 1000)) == 1);
    }
  }

  @Test
  public void getNumberOfThemeModeEditLogShouldReturnValue() throws Exception {
    int expected = (int) (long) runner.query("SELECT COUNT(*) FROM theme_mode_edit_log",
        new ScalarHandler<Long>());
    assertEquals(expected, database.getNumberOfThemeModeEditLog());
  }

  @Test
  public void addThemeModeEditLogShouldAddRow() throws Exception {
    int before = database.getNumberOfThemeModeEditLog();

    PacketThemeModeEditLog log = new PacketThemeModeEditLog();
    log.setUserCode(FAKE_USER_CODE);
    log.setTimeMs(System.currentTimeMillis());
    log.setType("Add");
    log.setTheme("THEME");
    log.setQuery("query");
    database.addThemeModeEditLog(log);

    int after = database.getNumberOfThemeModeEditLog();
    assertEquals(before + 1, after);
  }

  @Test
  public void getThemeModeEditLogShouldReturnLog() throws Exception {
    int before = database.getNumberOfThemeModeEditLog();

    PacketThemeModeEditLog log = new PacketThemeModeEditLog();
    log.setUserCode(FAKE_USER_CODE);
    log.setTimeMs(System.currentTimeMillis());
    log.setType("Add");
    log.setTheme("THEME");
    log.setQuery("query");
    database.addThemeModeEditLog(log);

    assertEquals(ImmutableList.of(log), database.getThemeModeEditLog(before, 1));
  }

  @Test
  public void testGetAdsenseProblems() throws Exception {
    List<PacketProblem> problem;

    problem = database.getAdsenseProblems("オンライン クイズ ゲーム");
    assertNotNull(problem);
    assertFalse(problem.isEmpty());

    problem = database.getAdsenseProblems("クイズ");
    assertNotNull(problem);
    assertFalse(problem.isEmpty());

    problem = database.getAdsenseProblems("RPG");
    assertNotNull(problem);
    assertFalse(problem.isEmpty());

    problem = database.getAdsenseProblems("MMO");
    assertNotNull(problem);
    assertFalse(problem.isEmpty());
    for (PacketProblem p : problem) {
      System.out.println(p);
    }
  }

  @Test
  public void restrictedUserCodeIntegrationTest() throws Exception {
    database.addRestrictedUserCode(FAKE_USER_CODE, RestrictionType.CHAT);
    assertTrue(database.getRestrictedUserCodes(RestrictionType.CHAT).contains(FAKE_USER_CODE));
    database.removeRestrictedUserCode(FAKE_USER_CODE, RestrictionType.CHAT);
    assertFalse(database.getRestrictedUserCodes(RestrictionType.CHAT).contains(FAKE_USER_CODE));
    database.addRestrictedUserCode(FAKE_USER_CODE, RestrictionType.CHAT);
    assertTrue(database.getRestrictedUserCodes(RestrictionType.CHAT).contains(FAKE_USER_CODE));
    database.removeRestrictedUserCode(FAKE_USER_CODE, RestrictionType.CHAT);
    assertFalse(database.getRestrictedUserCodes(RestrictionType.CHAT).contains(FAKE_USER_CODE));
  }

  @Test
  public void restrictedRemoteAddressIntegrationTest() throws Exception {
    database.addRestrictedRemoteAddress(FAKE_REMOTE_ADDRESS, RestrictionType.CHAT);
    assertTrue(database.getRestrictedRemoteAddresses(RestrictionType.CHAT).contains(
        FAKE_REMOTE_ADDRESS));
    database.removeRestrictedRemoteAddress(FAKE_REMOTE_ADDRESS, RestrictionType.CHAT);
    assertFalse(database.getRestrictedRemoteAddresses(RestrictionType.CHAT).contains(
        FAKE_REMOTE_ADDRESS));
    database.addRestrictedRemoteAddress(FAKE_REMOTE_ADDRESS, RestrictionType.CHAT);
    assertTrue(database.getRestrictedRemoteAddresses(RestrictionType.CHAT).contains(
        FAKE_REMOTE_ADDRESS));
    database.removeRestrictedRemoteAddress(FAKE_REMOTE_ADDRESS, RestrictionType.CHAT);
    assertFalse(database.getRestrictedRemoteAddresses(RestrictionType.CHAT).contains(
        FAKE_REMOTE_ADDRESS));
  }

  @Test
  public void getThemeRankingOldShouldReturnYearMonthZero() throws Exception {
    runner.update("DELETE FROM theme_mode_score WHERE USER_CODE = ?", FAKE_USER_CODE);

    database.updateThemeModeScore(FAKE_USER_CODE, FAKE_THEME, FAKE_SCORE);
    assertEquals(1, (long) runner.query(
        "SELECT COUNT(*) FROM theme_mode_score WHERE USER_CODE = ?", new ScalarHandler<Long>(),
        FAKE_USER_CODE));

    assertEquals(0, database.getThemeRankingOld(FAKE_THEME).size());
  }

  @Test
  public void getThemeRankingAllShouldReturnYearMonthNonZero() throws Exception {
    runner.update("DELETE FROM theme_mode_score WHERE USER_CODE = ?", FAKE_USER_CODE);

    database.updateThemeModeScore(FAKE_USER_CODE, FAKE_THEME, FAKE_SCORE);
    assertEquals(1, (long) runner.query(
        "SELECT COUNT(*) FROM theme_mode_score WHERE USER_CODE = ?", new ScalarHandler<Long>(),
        FAKE_USER_CODE));

    assertEquals(1, database.getThemeRankingAll(FAKE_THEME).size());
  }

  @Test
  public void getThemeRankingYearShouldReturnMatchedYearAllMonth() throws Exception {
    runner.update("DELETE FROM theme_mode_score WHERE USER_CODE = ?", FAKE_USER_CODE);

    database.updateThemeModeScore(FAKE_USER_CODE, FAKE_THEME, FAKE_SCORE);
    assertEquals(1, (long) runner.query(
        "SELECT COUNT(*) FROM theme_mode_score WHERE USER_CODE = ?", new ScalarHandler<Long>(),
        FAKE_USER_CODE));

    DateTime dateTime = new DateTime();
    assertEquals(1, database.getThemeRanking(FAKE_THEME, dateTime.getYear()).size());
    assertEquals(0, database.getThemeRanking(FAKE_THEME, dateTime.getYear() + 1).size());
  }

  @Test
  public void getThemeRankingYearMonthShouldReturnMatchedYearAndMonth() throws Exception {
    runner.update("DELETE FROM theme_mode_score WHERE USER_CODE = ?", FAKE_USER_CODE);

    database.updateThemeModeScore(FAKE_USER_CODE, FAKE_THEME, FAKE_SCORE);
    assertEquals(1, (long) runner.query(
        "SELECT COUNT(*) FROM theme_mode_score WHERE USER_CODE = ?", new ScalarHandler<Long>(),
        FAKE_USER_CODE));

    DateTime dateTime = new DateTime();
    assertEquals(1,
        database.getThemeRanking(FAKE_THEME, dateTime.getYear(), dateTime.getMonthOfYear()).size());
    assertEquals(0,
        database.getThemeRanking(FAKE_THEME, dateTime.getYear() + 1, dateTime.getMonthOfYear())
            .size());
    assertEquals(0,
        database.getThemeRanking(FAKE_THEME, dateTime.getYear(), dateTime.getMonthOfYear() + 1)
            .size());
  }

  @Test
  public void getThemeRankingDateRangesShouldReturnYearAndMonth() throws Exception {
    runner.update("DELETE FROM theme_mode_score WHERE USER_CODE = ?", FAKE_USER_CODE);

    database.updateThemeModeScore(FAKE_USER_CODE, FAKE_THEME, FAKE_SCORE);
    List<PacketMonth> dateRanges = database.getThemeRankingDateRanges();
    DateTime dateTime = new DateTime();
    PacketMonth expected = new PacketMonth();
    expected.year = dateTime.getYear();
    expected.month = dateTime.getMonthOfYear();
    assertThat(dateRanges, hasItem(expected));
  }

  @Test
  public void wrongAnswerHandlerShouldReturnAsIsIfNotSenmusubi() throws Exception {
    when(mockResultSet.next()).thenReturn(true, false);
    when(mockResultSet.getString("ANSWER")).thenReturn(FAKE_ANSWER);
    when(mockResultSet.getInt("COUNT")).thenReturn(FAKE_COUNT);

    PacketWrongAnswer expected = new PacketWrongAnswer();
    expected.answer = FAKE_ANSWER;
    expected.count = FAKE_COUNT;
    assertEquals(expected, wrongAnswerHandler.handleRow(mockResultSet));
  }

  @Test
  public void wrongAnswerHandlerShouldReturnSortAndNormalizeIfSenmusubi() throws Exception {
    when(mockResultSet.next()).thenReturn(true, false);
    when(mockResultSet.getString("ANSWER")).thenReturn("C---c\nB---b\nA---a");
    when(mockResultSet.getInt("COUNT")).thenReturn(FAKE_COUNT);

    PacketWrongAnswer expected = new PacketWrongAnswer();
    expected.answer = "A<--->a\nB<--->b\nC<--->c";
    expected.count = FAKE_COUNT;
    assertEquals(expected, wrongAnswerHandler.handleRow(mockResultSet));
  }

}
