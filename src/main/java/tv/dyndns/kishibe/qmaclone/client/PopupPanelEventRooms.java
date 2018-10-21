//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.dyndns.kishibe.qmaclone.client.lobby.LobbyUi;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRoomKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PopupPanelEventRooms extends PopupPanel implements ClickHandler {
	private final LobbyUi lobbyUi;
	private final Map<Button, PacketRoomKey> buttonToRoomKey = new HashMap<Button, PacketRoomKey>();
	private final Button buttonCancel = new Button("キャンセル", this);

	public PopupPanelEventRooms(LobbyUi panelLobby, List<PacketRoomKey> eventRooms) {
		super(true, true);
		setAnimationEnabled(true);
		this.lobbyUi = panelLobby;

		final VerticalPanel rootPanel = new VerticalPanel();

		// イベント名
		for (PacketRoomKey roomKey : eventRooms) {
			final Button button = new Button(roomKey.toString(), this);
			buttonToRoomKey.put(button, roomKey);
			rootPanel.add(button);
		}

		// キャンセルボタン
		rootPanel.add(buttonCancel);

		setWidget(rootPanel);
	}

	@Override
	public void onClick(ClickEvent event) {
		final Object sender = event.getSource();
		if (buttonToRoomKey.containsKey(sender)) {
			final PacketRoomKey roomKey = buttonToRoomKey.get(sender);
			lobbyUi.setGenres(roomKey.getGenres());
			lobbyUi.setTypes(roomKey.getTypes());
			lobbyUi.setEventName(roomKey.getName());
			hide();
		} else if (sender == buttonCancel) {
			hide();
		}
	}
}
