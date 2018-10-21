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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketResult;
import tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatisticsRatingHistory;
import tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatisticsUserAccuracyRate;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SceneResult extends SceneBase {
	private static final Logger logger = Logger.getLogger(SceneResult.class.getName());
	private final PanelResult panel;
	private final SessionData sessionData;

	public SceneResult(List<PacketProblem> problems, SessionData sessionData) {
		this.panel = new PanelResult(problems);
		this.sessionData = Preconditions.checkNotNull(sessionData);
		Controller.getInstance().setGamePanel(panel);
	}

	private void getResult() {
		final int sessionId = sessionData.getSessionId();
		new RetryAsyncCallback<List<PacketResult>>() {
			@Override
			protected void request() {
				Service.Util.getInstance().getResult(sessionId, this);
			}

			@Override
			protected boolean onReceived(List<PacketResult> playerResults) {
				if (playerResults == null) {
					return false;
				}

				panel.setPlayerList(playerResults);

				PacketResult myResult = null;
				for (PacketResult result : playerResults) {
					if (result.playerListId == sessionData.getPlayerListIndex()) {
						myResult = result;
						break;
					}
				}

				Preconditions.checkNotNull(myResult);

				// プレイヤー履歴書き換え
				UserData record = UserData.get();

				// ハイスコア更新
				if (!sessionData.isThemeMode() && record.getHighScore() < myResult.score) {
					record.setHighScore(myResult.score);
				}

				// プレイ回数
				int playCount = record.getPlayCount();
				if (playCount == Integer.MAX_VALUE) {
					playCount = 0;
				}
				++playCount;
				record.setPlayCount(playCount);

				// 平均得点
				if (!sessionData.isThemeMode()) {
					int averageScore = record.getAverageScore() * (record.getPlayCount() - 1);
					averageScore += myResult.score;
					averageScore /= record.getPlayCount();
					record.setAverageScore(averageScore);
				}

				// 平均順位
				float avarageRank = record.getAverageRank() * (float) (record.getPlayCount() - 1);
				avarageRank += (float) myResult.rank;
				avarageRank /= record.getPlayCount();
				record.setAvarageRank(avarageRank);

				// レーティング計算
				int oldRating = record.getRating();
				int newRating = myResult.newRating;
				Service.Util.getInstance().notifyGameFinished(record.getUserCode(), oldRating,
						newRating, sessionData.getSessionId(), callbackNotifyGameFinished);
				record.setRating(myResult.newRating);
				record.setVolatility(myResult.newVolatility);
				Service.Util.getInstance().addRatingHistory(UserData.get().getUserCode(),
						myResult.newRating, callbackAddRatingHistory);
				PanelStatisticsRatingHistory.getInstance().resetFlag();

				// クラス
				int classLevel = record.getClassLevel();
				if (myResult.score >= classLevel * 100 + 500 || myResult.rank == 1) {
					if (++classLevel >= (Constant.MAX_CLASS_LEVEL + 1)
							* Constant.STEP_PER_CLASS_LEVEL) {
						classLevel = (Constant.MAX_CLASS_LEVEL + 1) * Constant.STEP_PER_CLASS_LEVEL
								- 1;
					}
				} else if (myResult.score < classLevel * 100 - 500 || myResult.rank == 8) {
					if (--classLevel <= 0) {
						classLevel = 0;
					}
				}
				record.setClassLevel(classLevel);

				record.save();

				if (PanelStatisticsUserAccuracyRate.getInstance() != null) {
					PanelStatisticsUserAccuracyRate.getInstance().update();
				}
				return true;
			}

			@Override
			protected void onLightFailure(Throwable caught) {
				logger.log(Level.WARNING, "最終結果の取得に失敗しました。リクエストを再送します。");
			}

			@Override
			protected void onHeavyFailure(Throwable caught) {
				logger.log(Level.SEVERE, "通信エラーが発生しました。ゲームを中断します。");
			}
		}.start();
	}

	private final AsyncCallback<Void> callbackAddRatingHistory = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "レーティング履歴の登録に失敗しました", caught);
		}
	};
	private final AsyncCallback<Void> callbackNotifyGameFinished = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "ゲーム終了の通知に失敗しました", caught);
		}
	};

	@Override
	protected void onLoad() {
		super.onLoad();
		new Timer() {
			@Override
			public void run() {
				getResult();
			}
		}.schedule(1000);
	}
}
