package tv.dyndns.kishibe.qmaclone.server;

import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;
import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;
import tv.dyndns.kishibe.qmaclone.server.util.IntArray;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

@RunWith(JUnit4.class)
public class ThemeModeProblemManagerTest {

	private static final int LARGE_LOOP = 10000;

	@Rule
	public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
	@Inject
	private ThemeModeProblemManager manager;

	@Test
	public void testSelectProblemForThemeMode() throws InterruptedException {
		// 問題選択をLOOP回ランダムに行ってチェック
		final Random random = new Random(0);

		ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors() * 2);
		List<Callable<Void>> tasks = Lists.newArrayList();

		for (int loop = 0; loop < LARGE_LOOP; ++loop) {
			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					assertSelectProblemShouldWork(random);
					return null;
				}
			});
		}

		service.invokeAll(tasks);
		service.shutdown();
		service.awaitTermination(1, TimeUnit.MINUTES);
	}

	private void assertSelectProblemShouldWork(final Random random) throws Exception {
		List<String> themes = Lists.newArrayList(manager.getThemesAndProblems().keySet());
		String theme = themes.get(random.nextInt(themes.size()));
		Set<Integer> problemIds = Sets.newHashSet();
		Set<Integer> selectedProblemIds = Sets.newHashSet();
		for (int problemCount = 0; problemCount < Constant.MAX_PROBLEMS_PER_SESSION; ++problemCount) {
			int classLevel = random.nextInt(Constant.NUMBER_OF_CLASSES + 5);
			int difficultSelect = random.nextInt(5);

			PacketProblemMinimum problem = manager.selectProblem(theme, difficultSelect,
					classLevel, selectedProblemIds);
			assertNotNull(problem);
			selectedProblemIds.add(problem.id);
		}

		int numberOfProblems = problemIds.size();
		assertEquals(Constant.MAX_PROBLEMS_PER_SESSION, numberOfProblems);
		assertEquals(problemIds, selectedProblemIds);
	}

	@Test
	public void testGetThemes() {
		Map<String, IntArray> themesAndProblems = manager.getThemesAndProblems();

		List<List<String>> themes = manager.getThemes();
		assertNotNull(themes);
		assertEquals(6, themes.size());
		assertFalse(themes.get(0).isEmpty());
		assertFalse(themes.get(1).isEmpty());
		assertFalse(themes.get(2).isEmpty());
		assertFalse(themes.get(3).isEmpty());
		assertFalse(themes.get(4).isEmpty());
		assertFalse(themes.get(5).isEmpty());

		for (List<String> themesForEachGenre : themes) {
			for (String theme : themesForEachGenre) {
				assertThat(themesAndProblems.get(theme).size(),
						greaterThanOrEqualTo(Constant.MIN_NUMBER_OF_THEME_MODE_PROBLEMS));
			}
		}
	}

	@Test
	public void testGetProblemTablesForThemeMode() {
		Map<String, IntArray> themesAndProblems = manager.getThemesAndProblems();
		assertNotNull(themesAndProblems);
		assertFalse(themesAndProblems.isEmpty());
		// for (Entry<String, IntArray> entry : themesAndProblems.entrySet()) {
		// IntArray problemIds = entry.getValue();
		// assertFalse(entry.getKey(), problemIds.isEmpty());
		// }
	}

	@Test
	public void selectProblemShouldNotSelectSameProblemSoMuch() throws Exception {
		Multiset<Integer> problemIds = HashMultiset.create();
		for (int loop = 0; loop < 10; ++loop) {
			Set<Integer> selectedProblemIds = new HashSet<Integer>();
			for (int problemCount = 0; problemCount < Constant.MAX_PROBLEMS_PER_SESSION; ++problemCount) {
				PacketProblemMinimum problem = manager.selectProblem("とある魔術の禁書",
						Constant.DIFFICULT_SELECT_NORMAL, Constant.CLASS_LEVEL_NORMAL,
						selectedProblemIds);
				problemIds.add(problem.id);
			}
		}

		for (int problemId : problemIds) {
			assertThat(problemIds.count(problemId), lessThanOrEqualTo(5));
		}
	}
}
