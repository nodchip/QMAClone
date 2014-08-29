package tv.dyndns.kishibe.qmaclone.server.database;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;
import tv.dyndns.kishibe.qmaclone.server.util.IntArray;
import tv.dyndns.kishibe.qmaclone.server.util.Normalizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

@RunWith(JUnit4.class)
public class FullTextSearchTest {

  @Rule
  public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
  @Inject
  private Database database;
  @Inject
  private FullTextSearch fullTextSearch;

  @Test
  public final void testEscapeQuery() {
    assertEquals("\\\\", FullTextSearch.escapeQuery("\\"));
    assertEquals("\\\\post", FullTextSearch.escapeQuery("\\post"));
    assertEquals("pre\\\\", FullTextSearch.escapeQuery("pre\\"));
    assertEquals("pre\\\\post", FullTextSearch.escapeQuery("pre\\post"));

    assertEquals("\\+", FullTextSearch.escapeQuery("+"));
    assertEquals("\\+post", FullTextSearch.escapeQuery("+post"));
    assertEquals("pre\\+", FullTextSearch.escapeQuery("pre+"));
    assertEquals("pre\\+post", FullTextSearch.escapeQuery("pre+post"));

    assertEquals("\\-", FullTextSearch.escapeQuery("-"));
    assertEquals("\\-post", FullTextSearch.escapeQuery("-post"));
    assertEquals("pre\\-", FullTextSearch.escapeQuery("pre-"));
    assertEquals("pre\\-post", FullTextSearch.escapeQuery("pre-post"));

    assertEquals("\\&&", FullTextSearch.escapeQuery("&&"));
    assertEquals("\\&&post", FullTextSearch.escapeQuery("&&post"));
    assertEquals("pre\\&&", FullTextSearch.escapeQuery("pre&&"));
    assertEquals("pre\\&&post", FullTextSearch.escapeQuery("pre&&post"));

    assertEquals("\\||", FullTextSearch.escapeQuery("||"));
    assertEquals("\\||post", FullTextSearch.escapeQuery("||post"));
    assertEquals("pre\\||", FullTextSearch.escapeQuery("pre||"));
    assertEquals("pre\\||post", FullTextSearch.escapeQuery("pre||post"));

    assertEquals("\\!", FullTextSearch.escapeQuery("!"));
    assertEquals("\\!post", FullTextSearch.escapeQuery("!post"));
    assertEquals("pre\\!", FullTextSearch.escapeQuery("pre!"));
    assertEquals("pre\\!post", FullTextSearch.escapeQuery("pre!post"));

    assertEquals("\\(", FullTextSearch.escapeQuery("("));
    assertEquals("\\(post", FullTextSearch.escapeQuery("(post"));
    assertEquals("pre\\(", FullTextSearch.escapeQuery("pre("));
    assertEquals("pre\\(post", FullTextSearch.escapeQuery("pre(post"));

    assertEquals("\\)", FullTextSearch.escapeQuery(")"));
    assertEquals("\\)post", FullTextSearch.escapeQuery(")post"));
    assertEquals("pre\\)", FullTextSearch.escapeQuery("pre)"));
    assertEquals("pre\\)post", FullTextSearch.escapeQuery("pre)post"));

    assertEquals("\\{", FullTextSearch.escapeQuery("{"));
    assertEquals("\\{post", FullTextSearch.escapeQuery("{post"));
    assertEquals("pre\\{", FullTextSearch.escapeQuery("pre{"));
    assertEquals("pre\\{post", FullTextSearch.escapeQuery("pre{post"));

    assertEquals("\\}", FullTextSearch.escapeQuery("}"));
    assertEquals("\\}post", FullTextSearch.escapeQuery("}post"));
    assertEquals("pre\\}", FullTextSearch.escapeQuery("pre}"));
    assertEquals("pre\\}post", FullTextSearch.escapeQuery("pre}post"));

    assertEquals("\\[", FullTextSearch.escapeQuery("["));
    assertEquals("\\[post", FullTextSearch.escapeQuery("[post"));
    assertEquals("pre\\[", FullTextSearch.escapeQuery("pre["));
    assertEquals("pre\\[post", FullTextSearch.escapeQuery("pre[post"));

    assertEquals("\\]", FullTextSearch.escapeQuery("]"));
    assertEquals("\\]post", FullTextSearch.escapeQuery("]post"));
    assertEquals("pre\\]", FullTextSearch.escapeQuery("pre]"));
    assertEquals("pre\\]post", FullTextSearch.escapeQuery("pre]post"));

    assertEquals("\\^", FullTextSearch.escapeQuery("^"));
    assertEquals("\\^post", FullTextSearch.escapeQuery("^post"));
    assertEquals("pre\\^", FullTextSearch.escapeQuery("pre^"));
    assertEquals("pre\\^post", FullTextSearch.escapeQuery("pre^post"));

    assertEquals("\\\"", FullTextSearch.escapeQuery("\""));
    assertEquals("\\\"post", FullTextSearch.escapeQuery("\"post"));
    assertEquals("pre\\\"", FullTextSearch.escapeQuery("pre\""));
    assertEquals("pre\\\"post", FullTextSearch.escapeQuery("pre\"post"));

    assertEquals("\\~", FullTextSearch.escapeQuery("~"));
    assertEquals("\\~post", FullTextSearch.escapeQuery("~post"));
    assertEquals("pre\\~", FullTextSearch.escapeQuery("pre~"));
    assertEquals("pre\\~post", FullTextSearch.escapeQuery("pre~post"));

    assertEquals("\\*", FullTextSearch.escapeQuery("*"));
    assertEquals("\\*post", FullTextSearch.escapeQuery("*post"));
    assertEquals("pre\\*", FullTextSearch.escapeQuery("pre*"));
    assertEquals("pre\\*post", FullTextSearch.escapeQuery("pre*post"));

    assertEquals("\\?", FullTextSearch.escapeQuery("?"));
    assertEquals("\\?post", FullTextSearch.escapeQuery("?post"));
    assertEquals("pre\\?", FullTextSearch.escapeQuery("pre?"));
    assertEquals("pre\\?post", FullTextSearch.escapeQuery("pre?post"));

    assertEquals("\\:", FullTextSearch.escapeQuery(":"));
    assertEquals("\\:post", FullTextSearch.escapeQuery(":post"));
    assertEquals("pre\\:", FullTextSearch.escapeQuery("pre:"));
    assertEquals("pre\\:post", FullTextSearch.escapeQuery("pre:post"));
  }

  private static final boolean contains(String sentence, String query) {
    sentence = Normalizer.normalize(sentence);
    query = Normalizer.normalize(query);
    return sentence.contains(query);
  }

  private void assertSearchProblemsForThemeModeReturnsProblemContainsQueryString(
      String... queryStrings) throws Exception {
    IntArray problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList
        .copyOf(queryStrings));
    assertNotNull(problemIds);
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      assertNotNull(problem);

      boolean contained = false;
      for (String queryString : queryStrings) {
        if (contained == contains(problem.getSearchQuery(), queryString)) {
          break;
        }
      }
      assertFalse(
          "searchQuery=\"" + problem.getSearchQuery() + "\" queryString="
              + Arrays.deepToString(queryStrings), contained);
    }
  }

  private void assertSearchProblemsForThemeModeReturnsNoProblems(String... queryStrings)
      throws Exception {
    IntArray problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList
        .copyOf(queryStrings));
    assertNotNull(problemIds);
    assertTrue(problemIds.isEmpty());
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithOneLetter() throws Exception {
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("A");
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithTwoLetters() throws Exception {
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("KA");
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithAlphabetLetters() throws Exception {
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("KAITO");
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithJapaneseWord() throws Exception {
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("ファッション");
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithBugTrack359_1() throws Exception {
    // BugTrack-QMAClone/359 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F359
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("Intel");
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithBugTrack600() throws Exception {
    // BugTrack-QMAClone/600 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F600
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("『ナイン』");
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithBugTrack359_2() throws Exception {
    // BugTrack-QMAClone/359 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F359
    assertSearchProblemsForThemeModeReturnsNoProblems("Corel");
  }

  @Test
  public final void searchProblemsForThemeModeShouldWorkWithMark() throws Exception {
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("「AIR」");
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("『AIR』");
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("けいおん!");

    assertSearchProblemsForThemeModeReturnsNoProblems("鏡音リン(");
    assertSearchProblemsForThemeModeReturnsNoProblems("鏡音レン)");
    assertSearchProblemsForThemeModeReturnsNoProblems("*MEIKO");
    assertSearchProblemsForThemeModeReturnsNoProblems("がくぽ[");
    assertSearchProblemsForThemeModeReturnsNoProblems("機動戦士]");
    assertSearchProblemsForThemeModeReturnsNoProblems("宇宙戦艦{");
    assertSearchProblemsForThemeModeReturnsNoProblems("アンパンマン}");
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithMultipleString() throws Exception {
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("カプコン", "逆転裁判", "ロックマン",
        "鬼武者", "アレスの翼", "クローバースタジ", "モンスターハンタ", "魔界村", "『大神』", "ブレスオブファイ", "ファイナルファイ", "ビューティフルジ",
        "バイオハザード", "デビルメイクライ", "天地を喰らう", "ソンソン", "戦国BASARA", "ジャスティス学園", "ストリートファイ", "ポケットファイタ",
        "プロ野球？殺人事", "プロギア", "ころしあむ", "ロストワールド", "岡本吉起", "パワードギア", "『パワーストーン", "バトルサーキット",
        "クイズなないろ", "QUIZなないろ", "ＱＵＩＺなないろ", "虹色町の奇跡", "虎への道", "デッドライジング", "エグゼドエグゼス", "クイズ殿様の野望",
        "19XX", "ガチャフォース", "ギガウイング", "ＧＯＤＨＡＮＤ", "三上真司", "ストライダー飛竜", "サイバーボッツ", "CAPCOM", "１９４３改",
        "ルースターズ", "ドンプル", "ひげ丸", "クイズ三国志", "サイドアーム", "戦場の狼", "戦いの挽歌", "超鋼戦紀", "キカイオー", "バルガス",
        "必殺無頼拳", "プロギアの嵐", "ンパイアハンター", "パイアセイヴァー", "魔界島", "逆転検事");
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("ジャンヌ", "セイントテール");
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("マリみて", "マリア様がみてる");
    assertSearchProblemsForThemeModeReturnsProblemContainsQueryString("マリみて", "マリア様がみてる");
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithAndQuery() throws Exception {
    IntArray problemIds;

    problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList.of("機動 ナデシコ"));
    assertNotNull(problemIds);
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      final String searchQuery = problem.getSearchQuery();
      assertTrue(contains(searchQuery, "機動"));
      assertTrue(contains(searchQuery, "ナデシコ"));
    }

    problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList.of("KAITO MEIKO"));
    assertNotNull(problemIds);
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      final String searchQuery = problem.getSearchQuery();
      assertTrue(contains(searchQuery, "KAITO"));
      assertTrue(contains(searchQuery, "MEIKO"));
    }
  }

  @Test
  public final void testGetMinimumProblemsForThemeModeNotOperator() throws Exception {
    List<String> queries = new ArrayList<String>();
    IntArray problemIds;

    queries.clear();
    queries.add("ローマ -神聖");
    problemIds = fullTextSearch.searchProblemsForThemeMode(queries);
    assertNotNull(problemIds);
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      String sentence = problem.sentence;
      assertTrue(contains(sentence, "ローマ"));
      assertFalse("「" + sentence + "」に「神聖」が含まれています", contains(sentence, "神聖"));
    }
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithGenre() throws Exception {
    IntArray problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList
        .of("四国 ジャンル:アニメ＆ゲーム"));
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      String sentence = problem.sentence;
      assertTrue(contains(sentence, "四国"));
      assertSame(ProblemGenre.Anige, problem.genre);
    }
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithGenreAndNotOperator() throws Exception {
    IntArray problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList
        .of("四国 -ジャンル:アニメ＆ゲーム"));
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      String sentence = problem.sentence;
      assertTrue(contains(sentence, "四国"));
      assertNotSame(ProblemGenre.Anige, problem.genre);
    }
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithType() throws Exception {
    IntArray problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList.of("四国 問題形式:○×"));
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      String sentence = problem.sentence;
      assertTrue(contains(sentence, "四国"));
      assertSame(ProblemType.Marubatsu, problem.type);
    }
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithTypeAndNotOperator() throws Exception {
    IntArray problemIds = fullTextSearch
        .searchProblemsForThemeMode(ImmutableList.of("四国 -問題形式:○×"));
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      String sentence = problem.sentence;
      assertTrue(contains(sentence, "四国"));
      assertNotSame(ProblemType.Marubatsu, problem.type);
    }
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithCreator() throws Exception {
    IntArray problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList
        .of("ヨーロッパ 問題作成者:FUWAWA"));
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      String sentence = problem.sentence;
      assertTrue(contains(sentence, "ヨーロッパ"));
      assertTrue(contains(problem.creator, "FUWAWA"));
    }
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithCreatorAndNotOperator() throws Exception {
    IntArray problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList
        .of("ヨーロッパ -問題作成者:FUWAWA"));
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      String sentence = problem.sentence;
      assertTrue(contains(sentence, "ヨーロッパ"));
      assertFalse(contains(problem.creator, "FUWAWA"));
    }
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithRandomFlag() throws Exception {
    IntArray problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList
        .of("ヨーロッパ ランダム:1"));
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      String sentence = problem.sentence;
      assertTrue(contains(sentence, "ヨーロッパ"));
      assertSame(RandomFlag.Random1, problem.randomFlag);
    }
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithRandomFlagAndNotOperator() throws Exception {
    IntArray problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList
        .of("ヨーロッパ -ランダム:1"));
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      String sentence = problem.sentence;
      assertTrue(contains(sentence, "ヨーロッパ"));
      assertNotSame(RandomFlag.Random1, problem.randomFlag);
    }
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithGenreSubGenreWord() throws Exception {
    IntArray problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList.of(
        "ジャンル:アニメ＆ゲーム ランダム:1 なのは", "ジャンル:アニメ＆ゲーム ランダム:1 まどか"));
    assertFalse(problemIds.isEmpty());
    int numberOfNanoha = 0;
    int numberOfMadoka = 0;
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      String sentence = problem.sentence;
      assertTrue("sentence=" + sentence, sentence.contains("なのは") || sentence.contains("まどか"));

      if (sentence.contains("なのは")) {
        ++numberOfNanoha;
      }

      if (sentence.contains("まどか")) {
        ++numberOfMadoka;
      }
    }

    assertThat(numberOfNanoha, greaterThan(0));
    assertThat(numberOfMadoka, greaterThan(0));
  }

  @Test
  public void searchProblemsForThemeModeShouldWorkWithWordAndProblemType() throws Exception {
    IntArray problemIds = fullTextSearch.searchProblemsForThemeMode(ImmutableList
        .of("どっち？  問題形式:○×"));
    assertFalse(problemIds.isEmpty());
    for (PacketProblem problem : database.getProblem(problemIds.asList())) {
      String sentence = problem.sentence;
      assertTrue(contains(sentence, "どっち？"));
      assertEquals(ProblemType.Marubatsu, problem.type);
    }
  }

  @Test
  public void searchProblemsForThemeModeShouldReceiveManyQueries() throws Exception {
    List<String> queryStrings = Lists.newArrayList();
    for (int i = 0; i < 10000; ++i) {
      queryStrings.add(String.valueOf(i));
    }
    fullTextSearch.searchProblemsForThemeMode(queryStrings);
  }

}
