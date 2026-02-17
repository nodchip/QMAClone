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
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelLoginPlayers extends VerticalPanel {
	private static final Logger logger = Logger.getLogger(PanelLoginPlayers.class.getName());
	private static final int UPDATE_DURATION = 60 * 1000;
	private final RepeatingCommand commandUpdate = new RepeatingCommand() {
		@Override
		public boolean execute() {
			update();
			return isAttached();
		}
	};
	private static final String LEAD = "ログイン中のプレイヤーを表示します。最大で2分程度のタイムラグが出ます。";

	public PanelLoginPlayers() {
		setWidth("100%");
		addStyleName("loginPlayersRoot");
		update();
	}

	private void update() {
		Service.Util.getInstance().getLoginUsers(callbackGetLoginUsers);
	}

	private final AsyncCallback<List<PacketUserData>> callbackGetLoginUsers = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<List<PacketUserData>>() {
		public void onSuccess(List<PacketUserData> result) {
			clear();
			add(createIntroCard());

			FlowPanel grid = new FlowPanel();
			grid.setStyleName("loginPlayersGrid");
			for (PacketUserData data : result) {
				final VerticalPanel panel = new VerticalPanel();
				panel.setStyleName("loginPlayersCard");
				panel.setHorizontalAlignment(ALIGN_CENTER);

				final Image image = new Image(Constant.ICON_URL_PREFIX + data.imageFileName);
				image.setPixelSize(Constant.ICON_SIZE_BIG, Constant.ICON_SIZE_BIG);
				image.setStyleName("loginPlayersAvatar");
				panel.add(image);

				final Label label = new Label(data.playerName);
				label.setStyleName("loginPlayersName");
				panel.add(label);

				grid.add(panel);
			}
			if (result.isEmpty()) {
				HTML empty = new HTML("現在ログイン中のプレイヤーはいません。");
				empty.setStyleName("loginPlayersEmpty");
				add(empty);
				return;
			}
			add(grid);
		}

		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "ログインプレイヤー一覧の取得に失敗しました", caught);
		}
	};

	@Override
	protected void onLoad() {
		super.onLoad();
		Scheduler.get().scheduleFixedDelay(commandUpdate, UPDATE_DURATION);
	}

	private HTML createIntroCard() {
		HTML intro = new HTML(
				"<h3 class='loginPlayersTitle'>プレイヤー一覧</h3><p class='loginPlayersLead'>" + LEAD
						+ "</p>");
		intro.setStyleName("loginPlayersIntroCard");
		return intro;
	}
}

