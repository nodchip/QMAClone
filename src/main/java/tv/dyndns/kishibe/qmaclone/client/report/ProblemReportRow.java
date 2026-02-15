package tv.dyndns.kishibe.qmaclone.client.report;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

/**
 * 問題一覧の1行分データ。
 */
public class ProblemReportRow {
  /**
   * 問題本体。
   */
  public final PacketProblem problem;

  /**
   * 類似度スコア（Step5 類似問題表示用）。
   */
  public final Float similarityScore;

  /**
   * 類似検索順位（1始まり）。
   */
  public final Integer similarityRank;

  /**
   * 行データを生成する。
   *
   * @param problem 問題本体
   * @param similarityScore 類似度スコア
   * @param similarityRank 類似検索順位
   */
  public ProblemReportRow(PacketProblem problem, Float similarityScore, Integer similarityRank) {
    this.problem = problem;
    this.similarityScore = similarityScore;
    this.similarityRank = similarityRank;
  }
}
