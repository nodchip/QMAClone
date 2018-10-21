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
package tv.dyndns.kishibe.qmaclone.client.lobby;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Controller;
import tv.dyndns.kishibe.qmaclone.client.SceneBase;
import tv.dyndns.kishibe.qmaclone.client.SceneMatching;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.StatusUpdater;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.GameMode;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRegistrationData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;

import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SceneLobby extends SceneBase {
	private static final Logger logger = Logger.getLogger(SceneLobby.class.getName());
	public static final int SESSION_TYPE_VS_COM = 0;
	public static final int SESSION_TYPE_WHOLE = 1;
	public static final int SESSION_TYPE_EVENT = 2;
	public static final int SESSION_TYPE_THEME = 3;
	private static final int UPDATE_INTERVAL = 10 * 1000;
	private LobbyUi lobbyUi = new LobbyUi(this);
	private final StatusUpdater<PacketServerStatus> updater = new StatusUpdater<PacketServerStatus>(
			PacketServerStatus.class.getName(), UPDATE_INTERVAL) {
		@Override
		protected void request(AsyncCallback<PacketServerStatus> callback) {
			Service.Util.getInstance().getServerStatus(callback);
		}

		@Override
		protected PacketServerStatus parse(String json) {
			return PacketServerStatus.Json.READER.read(json);
		}

		@Override
		protected void onReceived(PacketServerStatus status) {
			setServerStatus(status);
		}
	};
	private boolean addPenalty = false;
	private boolean event;
	private boolean themeMode;

	private void updateServerData() {
		Service.Util.getInstance().getServerStatus(callbackGetServerStatus);
	}

	private final AsyncCallback<PacketServerStatus> callbackGetServerStatus = new AsyncCallback<PacketServerStatus>() {
		public void onSuccess(PacketServerStatus result) {
			setServerStatus(result);
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "サーバー状態の取得中にエラーが発生しました", caught);
		}
	};

	private void setServerStatus(PacketServerStatus status) {
		if (status == null) {
			logger.log(Level.WARNING, "取得したサーバー情報が壊れています");
			return;
		}

		if (lobbyUi == null) {
			return;
		}
		lobbyUi.setServerStatus(status);
		lobbyUi.setLastestPlayers(status.lastestPlayers);
	}

	public void register(int sessionType) {
		PacketPlayerSummary playlerSummary = lobbyUi.getPlayerSummary();
		Set<ProblemGenre> genres = lobbyUi.getGenres();
		Set<ProblemType> types = lobbyUi.getTypes();
		String greeting = lobbyUi.getGreeting();
		NewAndOldProblems newAndOldProblems = lobbyUi.getNewAndOldProblems();
		boolean publicEvent = lobbyUi.getPublicEvent();

		UserData record = UserData.get();
		int classNumber = record.getClassLevel();
		int classLevel = classNumber / Constant.STEP_PER_CLASS_LEVEL;
		if (classLevel >= Constant.MAX_CLASS_LEVEL) {
			classLevel = Constant.MAX_CLASS_LEVEL;
		}
		String imageFileName = record.getImageFileName();

		GameMode gameMode;
		String roomName;
		String theme;
		switch (sessionType) {
		case SESSION_TYPE_VS_COM: {
			gameMode = GameMode.VS_COM;
			roomName = null;
			theme = null;
			this.event = false;
			this.themeMode = false;
			addPenalty = false;
			break;
		}
		case SESSION_TYPE_WHOLE: {
			gameMode = GameMode.WHOLE;
			roomName = null;
			theme = null;
			this.event = true;
			this.themeMode = false;
			addPenalty = true;
			break;
		}
		case SESSION_TYPE_EVENT: {
			gameMode = GameMode.EVENT;
			roomName = lobbyUi.getEventName();
			theme = null;
			this.event = true;
			this.themeMode = false;
			addPenalty = true;
			break;
		}
		case SESSION_TYPE_THEME: {
			gameMode = GameMode.THEME;
			roomName = null;
			theme = lobbyUi.getThemeModeTheme();
			if (Strings.isNullOrEmpty(theme)) {
				return;
			}
			this.event = true;
			this.themeMode = true;
			addPenalty = false;
			break;
		}
		default: {
			gameMode = null;
			roomName = null;
			theme = null;
			this.event = false;
			addPenalty = true;
			break;
		}
		}

		int difficultSelect = lobbyUi.getDifficultSelect();
		int rating = UserData.get().getRating();
		int userCode = UserData.get().getUserCode();
		int volatility = UserData.get().getVolatility();
		int playCount = UserData.get().getPlayCount();
		Service.Util.getInstance().register(playlerSummary, genres, types, greeting, gameMode,
				roomName, theme, imageFileName, classLevel, difficultSelect, rating, userCode,
				volatility, playCount, newAndOldProblems, publicEvent, callbackRegister);
	}

	private final AsyncCallback<PacketRegistrationData> callbackRegister = new AsyncCallback<PacketRegistrationData>() {
		public void onSuccess(PacketRegistrationData result) {
			if (result == null) {
				logger.log(Level.SEVERE, "登録処理中にエラーが発生しました。ゲームを中断します。頻繁に発生する場合は管理者に御連絡ください。");
				return;
			}

			if (result.sessionId == 0) {
				logger.log(Level.SEVERE, "登録データにエラーが見つかりました。ゲームを中断します。頻繁に発生する場合は管理者に御連絡ください。");
				return;
			}

			SessionData sessionData = new SessionData(result.sessionId, result.playerListIndex,
					addPenalty, event, themeMode);

			// シーン切り替え
			Controller.getInstance().setScene(new SceneMatching(sessionData));
			lobbyUi = null;
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.SEVERE, "登録処理中にエラーが発生しました。頻繁に発生する場合は管理者に御連絡ください。", caught);
		}
	};

	@Override
	protected void onLoad() {
		super.onLoad();
		Controller.getInstance().setGamePanel(lobbyUi);
		updateServerData();
		updater.start();
	}

	@Override
	protected void onUnload() {
		updater.stop();
		super.onUnload();
	}
}
