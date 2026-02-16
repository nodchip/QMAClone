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
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.lobby.LobbyUi;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRoomKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * ロビーのイベント名候補を選択するポップアップ。
 */
public class PopupPanelEventRooms extends PopupPanel implements ClickHandler {
	private final LobbyUi lobbyUi;
	private final Map<Button, PacketRoomKey> buttonToRoomKey = new HashMap<Button, PacketRoomKey>();
	private final Button buttonCancel = new Button("キャンセル", this);

	public PopupPanelEventRooms(LobbyUi panelLobby, List<PacketRoomKey> eventRooms) {
		super(true, true);
		setAnimationEnabled(true);
		this.lobbyUi = panelLobby;
		setStyleName("lobbyEventPopup");
		final boolean hasEventRooms = eventRooms != null && !eventRooms.isEmpty();

		final VerticalPanel rootPanel = new VerticalPanel();
		rootPanel.setStyleName("lobbyEventPopupContent");
		if (hasEventRooms) {
			final Label headingLabel = new Label("参加したいイベントを選んでください");
			headingLabel.setStyleName("lobbyEventPopupHeading");
			rootPanel.add(headingLabel);
		}

		// イベント名
		if (hasEventRooms) {
			for (PacketRoomKey roomKey : eventRooms) {
				final Button button = new Button(formatRoomLabel(roomKey), this);
				button.setStyleName("lobbyEventPopupRoomButton");
				buttonToRoomKey.put(button, roomKey);
				rootPanel.add(button);
			}
		} else {
			final Label emptyMessage = new Label("現在、公開中のイベントはありません。");
			emptyMessage.setStyleName("lobbyEventPopupEmptyMessage");
			rootPanel.add(emptyMessage);
			final Label emptySubMessage = new Label("時間をおいてから、もう一度「参照」を押してください。");
			emptySubMessage.setStyleName("lobbyEventPopupEmptySubMessage");
			rootPanel.add(emptySubMessage);
			buttonCancel.setText("閉じる");
		}

		// キャンセルボタン
		buttonCancel.setStyleName("lobbyEventPopupCancelButton");
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

	/**
	 * イベント選択ボタンに表示する説明文を生成する。
	 *
	 * @param roomKey イベント部屋キー
	 * @return 表示用の説明文
	 */
	private static String formatRoomLabel(PacketRoomKey roomKey) {
		return "イベント名: " + formatEventName(roomKey.getName()) + " / ジャンル: "
				+ joinGenres(roomKey.getGenres()) + " / 形式: " + joinTypes(roomKey.getTypes());
	}

	/**
	 * イベント名が空の場合に案内文へ置換する。
	 *
	 * @param eventName イベント名
	 * @return 表示用イベント名
	 */
	private static String formatEventName(String eventName) {
		return eventName == null || eventName.trim().isEmpty() ? "(未設定)" : eventName;
	}

	/**
	 * ジャンル一覧を表示用文字列へ変換する。
	 *
	 * @param genres ジャンル集合
	 * @return 表示用文字列
	 */
	private static String joinGenres(Set<ProblemGenre> genres) {
		if (genres == null || genres.isEmpty()) {
			return "(未設定)";
		}
		StringBuilder sb = new StringBuilder();
		for (ProblemGenre genre : genres) {
			if (sb.length() != 0) {
				sb.append("・");
			}
			sb.append(genre.toString());
		}
		return sb.toString();
	}

	/**
	 * 出題形式一覧を表示用文字列へ変換する。
	 *
	 * @param types 形式集合
	 * @return 表示用文字列
	 */
	private static String joinTypes(Set<ProblemType> types) {
		if (types == null || types.isEmpty()) {
			return "(未設定)";
		}
		StringBuilder sb = new StringBuilder();
		for (ProblemType type : types) {
			if (sb.length() != 0) {
				sb.append("・");
			}
			sb.append(type.toString());
		}
		return sb.toString();
	}
}
