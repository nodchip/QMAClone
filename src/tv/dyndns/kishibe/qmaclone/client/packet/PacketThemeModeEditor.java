package tv.dyndns.kishibe.qmaclone.client.packet;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketThemeModeEditor implements IsSerializable {
	public enum ThemeModeEditorStatus implements IsSerializable {
		Applying, Accepted, Refected,
	}

	public int userCode;
	public String name;
	public ThemeModeEditorStatus themeModeEditorStatus;
}
