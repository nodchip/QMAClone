package tv.dyndns.kishibe.qmaclone.client.packet;

import java.util.Collection;
import java.util.List;

import name.pehl.piriti.json.client.JsonReader;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketChatMessages implements IsSerializable {
  public static class Json {
    public interface PacketChatDataListReader extends JsonReader<PacketChatMessages> {
    }

    public static final PacketChatDataListReader READER = GWT
        .create(PacketChatDataListReader.class);
  }

  public List<PacketChatMessage> list;

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
