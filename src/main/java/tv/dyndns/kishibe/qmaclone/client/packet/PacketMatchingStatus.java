package tv.dyndns.kishibe.qmaclone.client.packet;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.game.Transition;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketMatchingStatus implements IsSerializable {
  public int restSeconds;
  public List<PacketMatchingPlayer> players;
  public Transition transition;

  public static PacketMatchingStatus fromJson(String json) {
    JSONObject object = PacketJsonParser.parseObject(json);
    PacketMatchingStatus status = new PacketMatchingStatus();
    status.restSeconds = PacketJsonParser.getInt(object, "restSeconds");
    String transitionName = PacketJsonParser.getString(object, "transition");
    if (transitionName != null) {
      status.transition = Transition.valueOf(transitionName);
    }

    JSONArray array = PacketJsonParser.getArray(object, "players");
    if (array != null) {
      status.players = Lists.newArrayList();
      for (int i = 0; i < array.size(); ++i) {
        JSONValue value = array.get(i);
        JSONObject playerObject = value == null ? null : value.isObject();
        if (playerObject != null) {
          status.players.add(PacketMatchingPlayer.fromJsonObject(playerObject));
        }
      }
    }
    return status;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("restSeconds", restSeconds)
        .add("transition", transition).add("players", players).toString();
  }
}
