package tv.dyndns.kishibe.qmaclone.client.chat;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.ProvidesKey;

public class CellListChatLog extends CellList<PacketChatMessage> {
	private static final int rowCount = 100;
	private static final ProvidesKey<PacketChatMessage> providesKey = new ProvidesKey<PacketChatMessage>() {
		@Override
		public Object getKey(PacketChatMessage item) {
			return item.resId;
		}
	};

	public CellListChatLog(AbstractDataProvider<PacketChatMessage> dataProvider) {
		super(new CellChatLog(), GWT.<Resources> create(Resources.class), providesKey);
		setRowCount(rowCount);
		dataProvider.addDataDisplay(this);
	}
}
