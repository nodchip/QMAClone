package tv.dyndns.kishibe.qmaclone.server;

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;
import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.common.collect.Lists;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

@RunWith(JUnit4.class)
public class NormalModeProblemManagerTest {

  private static final int LARGE_LOOP = 10000;
  private static final int SMALL_LOOP = 1000;

  @Rule
  public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
  @Inject
  private NormalModeProblemManager manager;

  @Test
  public final void testSelectProblemStressTest() throws Exception {
    // 問題選択をLOOP回ランダムに行ってチェック
    final Random random = new Random(0);

    ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime()
        .availableProcessors());
    List<Callable<Void>> tasks = Lists.newArrayList();

    for (int loop = 0; loop < LARGE_LOOP; ++loop) {
      tasks.add(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          Set<Integer> problemIds = new HashSet<Integer>();
          Set<Integer> selectedProblemIds = new HashSet<Integer>();
          Set<Integer> userCodes = new HashSet<Integer>();
          Set<Integer> createrHashes = new HashSet<Integer>();
          for (int problemCount = 0; problemCount < Constant.MAX_PROBLEMS_PER_SESSION; ++problemCount) {
            ProblemGenre genre = ProblemGenre.values()[random.nextInt(ProblemGenre.values().length)];
            Set<ProblemGenre> genres = EnumSet.of(genre);
            ProblemType type = ProblemType.values()[random.nextInt(ProblemType.values().length)];
            Set<ProblemType> types = EnumSet.of(type);
            int classLevel = random.nextInt(Constant.NUMBER_OF_CLASSES + 5);
            int difficultSelect = random.nextInt(5);

            PacketProblemMinimum problem = manager.selectProblem(genres, types, classLevel,
                difficultSelect, problemIds, true, NewAndOldProblems.Both, false, userCodes,
                createrHashes);
            assertNotNull(problem);
            assertEquals(genre, problem.genre);
            assertEquals(type, problem.type);
            selectedProblemIds.add(problem.id);
          }

          int numberOfProblems = problemIds.size();
          assertTrue(numberOfProblems == Constant.MAX_PROBLEMS_PER_SESSION);
          assertEquals(problemIds, selectedProblemIds);
          return null;
        }
      });
    }

    service.invokeAll(tasks);
    service.shutdown();
  }

  @Test
  public void testSelectProblemNewProblem() throws Exception {
    // 新問のみで新問のみが出題されるかどうか？
    Random random = new Random(0);
    for (int loop = 0; loop < SMALL_LOOP; ++loop) {
      Set<ProblemGenre> genres = EnumSet.of(ProblemGenre.values()[random.nextInt(ProblemGenre
          .values().length)]);
      Set<ProblemType> types = EnumSet
          .of(ProblemType.values()[random.nextInt(ProblemType.values().length)]);
      int classLevel = random.nextInt(Constant.NUMBER_OF_CLASSES + 5);
      int difficultSelect = random.nextInt(5);
      NewAndOldProblems newAndOldProblems = NewAndOldProblems.OnlyNew;

      PacketProblemMinimum problem = manager.selectProblem(genres, types, classLevel,
          difficultSelect, new HashSet<Integer>(), true, newAndOldProblems, false,
          new HashSet<Integer>(), new HashSet<Integer>());
      assertNotNull(problem);
    }
  }

  @Test
  public void selectProblemShouldReturnOnlyNew() throws Exception {
    // 新問のみで新問のみが出題されるかどうか？
    for (int loop = 0; loop < SMALL_LOOP; ++loop) {
      int classLevel = 0;
      int difficultSelect = Constant.DIFFICULT_SELECT_NORMAL;
      NewAndOldProblems newAndOldProblems = NewAndOldProblems.OnlyNew;

      PacketProblemMinimum problem = manager.selectProblem(EnumSet.of(ProblemGenre.Random),
          EnumSet.of(ProblemType.Random), classLevel, difficultSelect, new HashSet<Integer>(),
          true, newAndOldProblems, false, new HashSet<Integer>(), new HashSet<Integer>());
      assertNotNull(problem);
    }
  }

  @Test
  public void testSelectProblemOldProblem() throws Exception {
    // 旧問のみで旧問のみが出題されるかどうか？
    Random random = new Random(0);
    int numberOfOld = 0;
    for (int loop = 0; loop < SMALL_LOOP; ++loop) {
      Set<ProblemGenre> genres = EnumSet.of(ProblemGenre.values()[random.nextInt(ProblemGenre
          .values().length)]);
      Set<ProblemType> types = EnumSet
          .of(ProblemType.values()[random.nextInt(ProblemType.values().length)]);
      int classLevel = random.nextInt(Constant.NUMBER_OF_CLASSES + 5);
      int difficultSelect = random.nextInt(5);
      NewAndOldProblems newAndOldProblems = NewAndOldProblems.OnlyOld;

      PacketProblemMinimum problem = manager.selectProblem(genres, types, classLevel,
          difficultSelect, new HashSet<Integer>(), true, newAndOldProblems, false,
          new HashSet<Integer>(), new HashSet<Integer>());
      assertNotNull(problem);
      if (!problem.isNew()) {
        ++numberOfOld;
      }
    }

    assertThat(numberOfOld, greaterThanOrEqualTo(SMALL_LOOP * 9 / 10));
  }

  @Test
  public void testSelectProblemDuplicatedCreater() throws Exception {
    // 問題選択をLOOP回ランダムに行ってチェック
    for (int i = 0; i < SMALL_LOOP; ++i) {
      Set<Integer> problemIds = new HashSet<Integer>();
      Set<Integer> userCodes = new HashSet<Integer>();
      Set<Integer> createrHashes = new HashSet<Integer>();
      Set<ProblemGenre> genres = EnumSet.of(ProblemGenre.Gakumon);
      Set<ProblemType> types = EnumSet.of(ProblemType.fromRandomFlag(2));
      int classLevel = 0;
      int difficultSelect = 1 << Constant.DIFFICULT_SELECT_DIFFICULT;
      boolean first = true;
      NewAndOldProblems newAndOldProblems = NewAndOldProblems.Both;
      boolean tegaki = false;
      for (int numberOfProblems = 0; numberOfProblems < Constant.MAX_PROBLEMS_PER_SESSION; ++numberOfProblems) {
        PacketProblemMinimum problem = manager
            .selectProblem(genres, types, classLevel, difficultSelect, problemIds, first,
                newAndOldProblems, tegaki, userCodes, createrHashes);
        assertNotNull(problem);
        problemIds.add(problem.id);
      }
      assertEquals(problemIds.size(), Constant.MAX_PROBLEMS_PER_SESSION);
      assertThat(createrHashes.size()).isGreaterThan(Constant.MAX_PROBLEMS_PER_SESSION - 4);
    }
  }

  @Test
  public void testSelectProblemGenreType() throws Exception {
    Random random = new Random(0);

    for (int loop = 0; loop < SMALL_LOOP; ++loop) {
      Set<Integer> problemIds = new HashSet<Integer>();
      Set<Integer> userCodes = new HashSet<Integer>();
      Set<Integer> createrHashes = new HashSet<Integer>();
      for (int problemCount = 0; problemCount < Constant.MAX_PROBLEMS_PER_SESSION; ++problemCount) {
        Set<ProblemGenre> genres;
        do {
          genres = ProblemGenre.fromBitFlag(random.nextInt(1 << ProblemGenre.values().length));
          genres.remove(ProblemGenre.Random);

        } while (genres.isEmpty());

        Set<ProblemType> types;
        do {
          types = ProblemType.fromBitFlag(random
              .nextInt(1 << ProblemType.numberOfTypesWithoutRandom));
          types.remove(ProblemType.Random);
        } while (types.isEmpty() || types.equals(EnumSet.of(ProblemType.Tegaki)));

        int classLevel = random.nextInt(Constant.NUMBER_OF_CLASSES + 5);
        int difficultSelect = random.nextInt(5);

        PacketProblemMinimum problem = manager.selectProblem(genres, types, classLevel,
            difficultSelect, problemIds, true, NewAndOldProblems.Both, false, userCodes,
            createrHashes);
        assertNotNull(problem);
        assertTrue(genres.contains(problem.genre));
        assertTrue("types=" + types + " should contain type=" + problem.type,
            types.contains(problem.type));
      }
    }
  }
}
