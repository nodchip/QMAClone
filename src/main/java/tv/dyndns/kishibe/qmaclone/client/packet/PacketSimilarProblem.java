package tv.dyndns.kishibe.qmaclone.client.packet;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 類似問題検索結果を表すデータ。
 */
public class PacketSimilarProblem implements IsSerializable {
  /**
   * 類似候補の問題ID。
   */
  public int problemId;

  /**
   * 類似検索スコア（LuceneのScoreDoc.score）。
   */
  public float score;

  /**
   * 類似検索結果の順位（1始まり）。
   */
  public int rank;

  /**
   * 類似候補の問題本体。
   */
  public PacketProblem problem;
}

