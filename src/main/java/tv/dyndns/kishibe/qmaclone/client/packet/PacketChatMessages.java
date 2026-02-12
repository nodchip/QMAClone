package tv.dyndns.kishibe.qmaclone.client.packet;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketChatMessages implements IsSerializable {
  public List<PacketChatMessage> list;

  public static PacketChatMessages fromJson(String json) {
    JSONObject object = PacketJsonParser.parseObject(json);
    PacketChatMessages messages = new PacketChatMessages();
    JSONArray array = PacketJsonParser.getArray(object, "list");
    if (array == null) {
      return messages;
    }

    messages.list = Lists.newArrayList();
    for (int i = 0; i < array.size(); ++i) {
      JSONValue value = array.get(i);
      JSONObject item = value == null ? null : value.isObject();
      if (item != null) {
        messages.list.add(PacketChatMessage.fromJsonObject(item));
      }
    }
    return messages;
  }

  public static PacketChatMessages fromMessage(PacketChatMessage message) {
    PacketChatMessages list = new PacketChatMessages();
    list.list = Lists.newArrayList(message);
    return list;
  }

  public static PacketChatMessages fromMessages(Collection<PacketChatMessage> messages) {
    PacketChatMessages list = new PacketChatMessages();
    list.list = Lists.newArrayList(messages);
    return list;
  }
}
