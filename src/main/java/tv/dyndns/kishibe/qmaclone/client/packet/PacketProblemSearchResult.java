package tv.dyndns.kishibe.qmaclone.client.packet;

import java.io.Serializable;
import java.util.List;

/**
 * 問題検索結果とページング情報を保持します。
 */
public class PacketProblemSearchResult implements Serializable {
  private static final long serialVersionUID = 1L;

  public List<PacketProblem> problems;
  public int totalCount;
  public int offset;
  public int limit;
}
