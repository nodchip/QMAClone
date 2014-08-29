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

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingData;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SceneMatching extends SceneBase {

	private static class MatchingStatusUpdater extends StatusUpdater<PacketMatchingData> {

		private final SceneMatching scene;
		private final SessionData sessionData;

		public MatchingStatusUpdater(SceneMatching scene, SessionData sessionData) {
			super(PacketMatchingData.class.getName() + Constant.WEBSOCKET_PROTOCOL_SEPARATOR
					+ sessionData.getSessionId(), TIMER_INTERVAL);
			this.scene = Preconditions.checkNotNull(scene);
			this.sessionData = Preconditions.checkNotNull(sessionData);
		}

		@Override
		protected void request(AsyncCallback<PacketMatchingData> callback) {
			int sessionId = sessionData.getSessionId();
			Service.Util.getInstance().getMatchingData(sessionId, callback);
		}

		@Override
		protected void onReceived(PacketMatchingData status) {
			scene.callbackGetMatchingData.onSuccess(status);
		}

		@Override
		protected PacketMatchingData parse(String json) {
			return PacketMatchingData.Json.READER.read(json);
		}

	}

	private static final Logger logger = Logger.getLogger(SceneMatching.class.getName());
	private static final int TIMER_INTERVAL = 1000;
	private PanelMatching panel;
	private boolean transited = false;
	private final StatusUpdater<PacketMatchingData> updater;
	private final SessionData sessionData;

	public SceneMatching(SessionData sessionData) {
		this.sessionData = Preconditions.checkNotNull(sessionData);
		updater = new MatchingStatusUpdater(this, sessionData);
		panel = new PanelMatching(this);

		Controller.getInstance().setGamePanel(panel);
	}

	final AsyncCallback<PacketMatchingData> callbackGetMatchingData = new AsyncCallback<PacketMatchingData>() {
		@Override
		public void onSuccess(PacketMatchingData result) {
			if (transited) {
				return;
			}

			if (result == null || result.players == null) {
				String message = "無効なマッチング情報が返されました: "
						+ Objects.toStringHelper(this).add("sessionId", sessionData.getSessionId())
								.add("playerListIndex", sessionData.getPlayerListIndex())
								.add("userCode", UserData.get().getUserCode())
								.add("packetMatchingData", result).toString();
				logger.log(Level.WARNING, message);
				return;
			}

			panel.setPlayerList(result.players);

			if (result.restSeconds <= 0) {
				transited = true;
				Controller.getInstance().setScene(new SceneReadyForGame(sessionData));
				updater.stop();
			} else {
				panel.setRestSecond(result.restSeconds);
			}
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "マッチング情報の取得中にエラーが発生しました", caught);
		}
	};

	public void requestSkip() {
		int sessionId = sessionData.getSessionId();
		int playerListId = sessionData.getPlayerListIndex();

		Service.Util.getInstance().requestSkip(sessionId, playerListId, callbackRequestSkip);
	}

	private final AsyncCallback<Integer> callbackRequestSkip = new AsyncCallback<Integer>() {
		public void onSuccess(Integer result) {
		}

		public void onFailure(Throwable caught) {
			Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
				@Override
				public boolean execute() {
					requestSkip();
					return false;
				}
			}, 1000);

			logger.log(Level.WARNING, "スキップリクエスト送信中にエラーが発生しました。パケットを再送します", caught);
		}
	};

	@Override
	protected void onLoad() {
		super.onLoad();
		updater.start();
	}

	@Override
	protected void onUnload() {
		updater.stop();
		super.onUnload();
	}
}
