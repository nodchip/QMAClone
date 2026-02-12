package tv.dyndns.kishibe.qmaclone.client.packet;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketReadyForGame implements IsSerializable {
	public int restSeconds;

	public static PacketReadyForGame fromJson(String json) {
		JSONObject object = PacketJsonParser.parseObject(json);
		PacketReadyForGame packet = new PacketReadyForGame();
		packet.restSeconds = PacketJsonParser.getInt(object, "restSeconds");
		return packet;
	}
}
