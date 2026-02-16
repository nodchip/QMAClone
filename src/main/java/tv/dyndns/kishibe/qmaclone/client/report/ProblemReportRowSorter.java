package tv.dyndns.kishibe.qmaclone.client.report;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketSimilarProblem;

/**
 * 問題レポート行データの生成・並び替えを提供するユーティリティ。
 */
public final class ProblemReportRowSorter {

  private ProblemReportRowSorter() {
  }

  /**
   * 通常の問題リストを行データへ変換する。
   *
   * @param problems 問題リスト
   * @return 行データ
   */
  public static List<ProblemReportRow> fromProblems(List<PacketProblem> problems) {
    List<ProblemReportRow> rows = Lists.newArrayList();
    if (problems == null) {
      return rows;
    }
    for (PacketProblem problem : problems) {
      if (problem == null) {
        continue;
      }
      rows.add(new ProblemReportRow(problem, null, null));
    }
    return rows;
  }

  /**
   * 類似問題検索結果を行データへ変換する。
   *
   * @param similarProblems 類似問題検索結果
   * @return 行データ
   */
  public static List<ProblemReportRow> fromSimilarProblems(
      List<PacketSimilarProblem> similarProblems) {
    List<ProblemReportRow> rows = Lists.newArrayList();
    if (similarProblems == null) {
      return rows;
    }
    for (PacketSimilarProblem similar : similarProblems) {
      if (similar == null || similar.problem == null) {
        continue;
      }
      rows.add(new ProblemReportRow(similar.problem, similar.score, similar.rank));
    }
    return rows;
  }

  /**
   * 表示用途に応じた既定順へ並び替える。
   *
   * @param rows 行データ
   * @param initialSort true: 問題番号昇順、false: 類似度降順（類似度を持つ場合のみ）
   */
  public static void sortForDisplay(List<ProblemReportRow> rows, boolean initialSort) {
    if (rows == null || rows.size() <= 1) {
      return;
    }
    if (initialSort) {
      Collections.sort(rows, BY_PROBLEM_ID_ASC);
      return;
    }
    if (hasSimilarity(rows)) {
      Collections.sort(rows, BY_SIMILARITY_DESC_THEN_ID_DESC);
    }
  }

  /**
   * 登録問題一覧向けの既定順で並び替える。
   *
   * @param rows 行データ
   */
  public static void sortForRatioReport(List<ProblemReportRow> rows) {
    if (rows == null || rows.size() <= 1) {
      return;
    }
    Collections.sort(rows, BY_ANSWER_COUNT_DESC_THEN_ACCURACY_ASC_THEN_ID_DESC);
  }

  private static boolean hasSimilarity(List<ProblemReportRow> rows) {
    for (ProblemReportRow row : rows) {
      if (row != null && row.similarityScore != null) {
        return true;
      }
    }
    return false;
  }

  private static final Comparator<ProblemReportRow> BY_PROBLEM_ID_ASC =
      new Comparator<ProblemReportRow>() {
        @Override
        public int compare(ProblemReportRow left, ProblemReportRow right) {
          return safeProblemId(left) - safeProblemId(right);
        }
      };

  private static final Comparator<ProblemReportRow> BY_SIMILARITY_DESC_THEN_ID_DESC =
      new Comparator<ProblemReportRow>() {
        @Override
        public int compare(ProblemReportRow left, ProblemReportRow right) {
          int compareScore = Float.compare(safeSimilarity(right), safeSimilarity(left));
          if (compareScore != 0) {
            return compareScore;
          }
          return safeProblemId(right) - safeProblemId(left);
        }
      };

  private static final Comparator<ProblemReportRow> BY_ANSWER_COUNT_DESC_THEN_ACCURACY_ASC_THEN_ID_DESC =
      new Comparator<ProblemReportRow>() {
        @Override
        public int compare(ProblemReportRow left, ProblemReportRow right) {
          PacketProblem l = safeProblem(left);
          PacketProblem r = safeProblem(right);
          int lAnswers = l.good + l.bad;
          int rAnswers = r.good + r.bad;
          int byCount = rAnswers - lAnswers;
          if (byCount != 0) {
            return byCount;
          }
          int byRate = l.getAccuracyRate() - r.getAccuracyRate();
          if (byRate != 0) {
            return byRate;
          }
          return safeProblemId(right) - safeProblemId(left);
        }
      };

  private static int safeProblemId(ProblemReportRow row) {
    if (row == null || row.problem == null) {
      return Integer.MIN_VALUE;
    }
    return row.problem.id;
  }

  private static float safeSimilarity(ProblemReportRow row) {
    if (row == null || row.similarityScore == null) {
      return Float.NEGATIVE_INFINITY;
    }
    return row.similarityScore;
  }

  private static PacketProblem safeProblem(ProblemReportRow row) {
    return row == null || row.problem == null ? new PacketProblem() : row.problem;
  }
}
