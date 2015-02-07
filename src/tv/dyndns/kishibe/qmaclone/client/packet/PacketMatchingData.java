package tv.dyndns.kishibe.qmaclone.client.packet;

import java.util.List;

import name.pehl.piriti.json.client.JsonReader;
import tv.dyndns.kishibe.qmaclone.client.game.Transition;

import com.google.common.base.MoreObjects;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketMatchingData implements IsSerializable {
  public static class Json {
    public interface PacketMatchingDataReader extends JsonReader<PacketMatchingData> {
    }

    public static final PacketMatchingDataReader READER = GWT
        .create(PacketMatchingDataReader.class);
  }

  public int restSeconds;
  public List<PacketMatchingPlayer> players;
  public Transition transition;

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("restSeconds", restSeconds)
        .add("transition", transition).add("players", players).toString();
  }
}
