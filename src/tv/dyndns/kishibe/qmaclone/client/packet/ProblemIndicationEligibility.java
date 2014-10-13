package tv.dyndns.kishibe.qmaclone.client.packet;

public enum ProblemIndicationEligibility {
  /**
   * 指摘可能
   */
  OK,
  /**
   * 単位時間あたりの指定回数の上限に達したため指摘不可能
   */
  REACHED_MAX_NUMBER_OF_REQUESTS_PER_UNIT_TIME,
  /**
   * プレイヤー名が変更されていないため指摘不可能
   */
  PLAYER_NAME_UNCHANGED
}
