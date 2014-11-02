package tv.dyndns.kishibe.qmaclone.client.packet;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketImageLink implements IsSerializable, Comparable<PacketImageLink> {
  public int problemId;
  public String url;
  public int statusCode;

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("problemId", problemId)
        .add("statusCode", statusCode).add("url", url).toString();
  }

  @Override
  public int compareTo(PacketImageLink o) {
    return problemId != o.problemId ? problemId - o.problemId : url.compareTo(o.url);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(problemId, url, statusCode);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PacketImageLink)) {
      return false;
    }
    PacketImageLink other = (PacketImageLink) obj;
    return problemId == other.problemId && Objects.equal(url, other.url)
        && statusCode == other.statusCode;
  }
}
