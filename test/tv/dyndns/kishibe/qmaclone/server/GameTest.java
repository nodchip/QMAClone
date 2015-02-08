package tv.dyndns.kishibe.qmaclone.server;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.GameMode;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.common.collect.EnumMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

@RunWith(JUnit4.class)
public class GameTest {

  private static final String FAKE_REMOTE_ADDRESS = "1.2.3.4";
  @Rule
  public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
  @Inject
  private Game.Factory gameFactory;
  private Game game;

  @Before
  public void setUp() throws Exception {
    game = gameFactory.create(0, 0, false, false, null, false, GameMode.WHOLE);
  }

  @Test
  public void testCalculateRating() {
    PlayerStatus player1 = new PlayerStatus(null, 1, 0, 0, true, null, null, 0, 1800, 10000000,
        300, 100);
    player1.addScore(5000);
    PlayerStatus player2 = new PlayerStatus(null, 2, 0, 0, true, null, null, 0, 1600, 10000001,
        300, 200);
    player2.addScore(4000);
    PlayerStatus player3 = new PlayerStatus(null, 3, 0, 0, true, null, null, 0, 1400, 10000002,
        300, 300);
    player3.addScore(3000);
    PlayerStatus player4 = new PlayerStatus(null, 4, 0, 0, true, null, null, 0, 1200, 10000003,
        300, 400);
    player4.addScore(2000);
    PlayerStatus player5 = new PlayerStatus(null, 5, 0, 0, true, null, null, 0, 1000, 10000004,
        300, 500);
    player5.addScore(1000);
    ArrayList<PlayerStatus> list = Lists.newArrayList(player1, player2, player3, player4, player5);

    game.calculateRating(list);

    assertThat(player1.getNewRating(), greaterThan(1800));
    assertThat(player2.getNewRating(), greaterThan(1600));
    assertThat(player4.getNewRating(), lessThan(1200));
    assertThat(player5.getNewRating(), lessThan(1000));
  }

  @Test
  public void testCalculateRating2() {
    PlayerStatus player1 = new PlayerStatus(null, 1, 0, 0, true, null, null, 0, 1274, 10000000,
        300, 100);
    player1.addScore(5000);
    PlayerStatus player2 = new PlayerStatus(null, 2, 0, 0, true, null, null, 0, 1274, 10000001,
        300, 200);
    player2.addScore(4000);
    ArrayList<PlayerStatus> list = Lists.newArrayList(player1, player2);

    game.calculateRating(list);

    assertThat(player1.getNewRating(), greaterThan(1274));
    assertThat(player2.getNewRating(), lessThan(1274));
  }

  @Test
  public void testCalculateRating3() {
    PlayerStatus player1 = new PlayerStatus(null, 1, 0, 0, true, null, null, 0, 1274, 10000000,
        300, 100);
    player1.addScore(5000);
    PlayerStatus player2 = new PlayerStatus(null, 2, 0, 0, true, null, null, 0, 1274, 10000001,
        300, 200);
    player2.addScore(5000);
    ArrayList<PlayerStatus> list = Lists.newArrayList(player1, player2);

    game.calculateRating(list);

    // assertEquals(1274, player1.getNewRating());
    // assertEquals(1274, player2.getNewRating());
  }

  @Test
  public void prepareProblemsShouldSelectSameGenreAndTypeIfOnlyOneIsSelected() {
    List<Integer> problemIds = Lists.newArrayList();
    Set<ProblemGenre> selectedGenres = EnumSet.of(ProblemGenre.Anige);
    Set<ProblemType> selectedTypes = EnumSet.of(ProblemType.Marubatsu);

    List<PacketProblem> problems = game.prepareProblems(0, NewAndOldProblems.Both, problemIds,
        selectedGenres, selectedTypes, 0, null);

    for (PacketProblem problem : problems) {
      assertEquals(ProblemGenre.Anige, problem.genre);
      assertEquals(ProblemType.Marubatsu, problem.type);
    }
  }

  @Test
  public void prepareProblemsShouldSelectFromSelectedGenresAndTypesEqually() {
    List<Integer> problemIds = Lists.newArrayList();
    Set<ProblemGenre> selectedGenres = EnumSet.of(ProblemGenre.Anige, ProblemGenre.Sports);
    Set<ProblemType> selectedTypes = EnumSet.of(ProblemType.Marubatsu, ProblemType.YonTaku,
        ProblemType.Rensou);

    List<PacketProblem> problems = game.prepareProblems(0, NewAndOldProblems.Both, problemIds,
        selectedGenres, selectedTypes, 0, null);

    Multiset<ProblemGenre> genres = EnumMultiset.create(ProblemGenre.class);
    Multiset<ProblemType> types = EnumMultiset.create(ProblemType.class);

    for (PacketProblem problem : problems) {
      assertThat(problem.genre, isIn(selectedGenres));
      genres.add(problem.genre);

      assertThat(problem.type, isIn(selectedTypes));
      types.add(problem.type);
    }

    // 問題が見つからなかった場合に問題数が少なくなる場合があることを許容する
    assertThat(genres.count(ProblemGenre.Anige), isOneOf(7, 8, 9));
    assertThat(genres.count(ProblemGenre.Sports), isOneOf(7, 8, 9));
    assertThat(types.count(ProblemType.Marubatsu), isOneOf(3, 4, 5, 6, 7));
    assertThat(types.count(ProblemType.YonTaku), isOneOf(3, 4, 5, 6, 7));
    assertThat(types.count(ProblemType.Rensou), isOneOf(3, 4, 5, 6, 7));
  }

  @Test
  public void prepareProblemsShouldSelectEvenIfGenresAndTypesAreEmpty() {
    // テーマモードではジャンルとタイプは空で送られてくる
    List<Integer> problemIds = Lists.newArrayList();
    Set<ProblemGenre> selectedGenres = EnumSet.noneOf(ProblemGenre.class);
    Set<ProblemType> selectedTypes = EnumSet.noneOf(ProblemType.class);

    List<PacketProblem> problems = game.prepareProblems(0, NewAndOldProblems.Both, problemIds,
        selectedGenres, selectedTypes, 0, null);

    assertEquals(16, problems.size());
  }
}
