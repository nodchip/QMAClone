package tv.dyndns.kishibe.qmaclone.client.report;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketSimilarProblem;

/**
 * {@link ProblemReportRowSorter} の並び順テスト。
 */
public class ProblemReportRowSorterTest {

  /**
   * 初期ソート有効時は問題番号昇順になることを確認する。
   */
  @Test
  public void testSortByIdAscendingWhenInitialSortEnabled() {
    List<ProblemReportRow> rows = Lists.newArrayList(
        new ProblemReportRow(createProblem(30), null, null),
        new ProblemReportRow(createProblem(10), null, null),
        new ProblemReportRow(createProblem(20), null, null));

    ProblemReportRowSorter.sortForDisplay(rows, true);

    assertEquals(10, rows.get(0).problem.id);
    assertEquals(20, rows.get(1).problem.id);
    assertEquals(30, rows.get(2).problem.id);
  }

  /**
   * 類似度付きの行は類似度降順、同値時は問題番号降順で並ぶことを確認する。
   */
  @Test
  public void testSortBySimilarityDescendingForStep5() {
    List<ProblemReportRow> rows = Lists.newArrayList(
        new ProblemReportRow(createProblem(11), 0.75f, 3),
        new ProblemReportRow(createProblem(21), 0.95f, 1),
        new ProblemReportRow(createProblem(31), 0.75f, 2));

    ProblemReportRowSorter.sortForDisplay(rows, false);

    assertEquals(21, rows.get(0).problem.id);
    assertEquals(31, rows.get(1).problem.id);
    assertEquals(11, rows.get(2).problem.id);
  }

  /**
   * 類似問題DTOから行データへ変換できることを確認する。
   */
  @Test
  public void testFromSimilarProblems() {
    PacketSimilarProblem similar = new PacketSimilarProblem();
    similar.problem = createProblem(123);
    similar.score = 0.88f;
    similar.rank = 2;

    List<ProblemReportRow> rows =
        ProblemReportRowSorter.fromSimilarProblems(Lists.newArrayList(similar));

    assertEquals(1, rows.size());
    assertEquals(123, rows.get(0).problem.id);
    assertEquals(Float.valueOf(0.88f), rows.get(0).similarityScore);
    assertEquals(Integer.valueOf(2), rows.get(0).similarityRank);
  }

  /**
   * テスト用の最小問題を生成する。
   */
  private static PacketProblem createProblem(int id) {
    PacketProblem problem = new PacketProblem();
    problem.id = id;
    return problem;
  }
}
