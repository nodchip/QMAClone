package tv.dyndns.kishibe.qmaclone.client.packet;

import name.pehl.piriti.json.client.JsonReader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketReadyForGame implements IsSerializable {
	public static class Json {
		public interface PacketReadyForGameReader extends JsonReader<PacketReadyForGame> {
		}

		public static final PacketReadyForGameReader READER = GWT
				.create(PacketReadyForGameReader.class);
	}

	public int restSeconds;
}
