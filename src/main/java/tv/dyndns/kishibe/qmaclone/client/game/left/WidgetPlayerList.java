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
package tv.dyndns.kishibe.qmaclone.client.game.left;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.game.click.MarkedCanvas;
import tv.dyndns.kishibe.qmaclone.client.geom.Point;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus.GamePlayerStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingPlayer;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class WidgetPlayerList extends AbsolutePanel {
	private static final int UPDATE_DURATION = 100;
	private final List<WidgetPlayer> players = Lists.newArrayList();
	private MarkedCanvas markedCanvas;
	private final RepeatingCommand commandUpdate = new RepeatingCommand() {
		@Override
		public boolean execute() {
			updatePosition();
			return isAttached();
		}
	};

	public WidgetPlayerList(List<PacketMatchingPlayer> players) {
		setPixelSize(180, WidgetPlayer.HEIGHT * players.size());

		int rank = 0;
		for (PacketMatchingPlayer player : players) {
			WidgetPlayer w = new WidgetPlayer(player.playerSummary, player.imageFileName, this,
					rank++);
			this.players.add(w);
			add(w, w.getCurrentX(), w.getCurrentY());
		}

		updatePosition();
	}

	public void setPlayerSummary(List<PacketPlayerSummary> playerSummaries) {
		int n = Math.min(players.size(), playerSummaries.size());
		for (int i = 0; i < n; ++i) {
			players.get(i).setPlayerSummary(playerSummaries.get(i));
		}
	}

	public void onNextProblem(PacketProblem problem) {
		for (WidgetPlayer player : players) {
			player.clearAnswer();
			player.setProblem(problem);
		}

		if (markedCanvas != null) {
			markedCanvas.hide();
			markedCanvas = null;
		}
	}

	public void onSendAnswer() {
		for (WidgetPlayer player : players) {
			player.open();
		}

		if (markedCanvas != null) {
			markedCanvas.update();
		}
	}

	public void onAnswer() {
		for (WidgetPlayer player : players) {
			player.recieveTimeUp();
		}

		if (markedCanvas != null) {
			markedCanvas.update();
		}
	}

	private void updatePosition() {
		for (WidgetPlayer player : players) {
			player.updatePosition();
		}

		if (markedCanvas != null) {
			markedCanvas.update();
		}
	}

	public void onGamePlayerStatusReceived(GamePlayerStatus[] status) {
		int n = Math.min(status.length, players.size());
		for (int i = 0; i < n; ++i) {
			WidgetPlayer player = players.get(i);

			int rank = status[i].rank;
			player.setRank(rank);

			String answer = status[i].answer;
			if (answer != null) {
				player.recieveAnswer(answer);
			}
		}
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		if (UserData.get().isRankingMove()) {
			Scheduler.get().scheduleFixedDelay(commandUpdate, UPDATE_DURATION);
		}
	}

	@Override
	protected void onUnload() {
		if (markedCanvas != null) {
			markedCanvas.hide();
			markedCanvas = null;
		}
		super.onUnload();
	}

	private static final int CANVAS_WIDTH = 800;
	private static final int CANVAS_HEIGHT = 800;

	/**
	 * クリッククイズの回答表示用のパネルを準備する。 このメソッドが呼ばれるとゲーム画面全体に透明のパネルが表示され、 マウスイベントを盗んでしまうため、 呼ぶタイミングに注意する必要がある。
	 * 
	 * @return {@link MarkedCanvas}
	 */
	public MarkedCanvas ensureCanvas() {
		if (markedCanvas == null) {
			markedCanvas = new MarkedCanvas(this, CANVAS_WIDTH, CANVAS_HEIGHT);
			markedCanvas.show();
		}

		return markedCanvas;
	}

	public Point getOffset() {
		return new Point(getAbsoluteLeft(), getAbsoluteTop());
	}
}
