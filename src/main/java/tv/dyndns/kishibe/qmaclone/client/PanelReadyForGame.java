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

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingPlayer;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelReadyForGame extends VerticalPanel {

	private final Label labelCountdown = new Label("通信中");
	private final Label labelPlayerCount = new Label("待機中プレイヤー: 0人");

	private final SimplePanel panelPlayerList = new SimplePanel();

	public PanelReadyForGame() {
		setHorizontalAlignment(ALIGN_CENTER);
		setVerticalAlignment(ALIGN_MIDDLE);
		setStyleName("readyForGameRoot");

		final VerticalPanel headerPanel = new VerticalPanel();
		headerPanel.setStyleName("readyForGameHeader");

		final Label labelTitle = new Label("ゲーム開始待機");
		labelTitle.setStyleName("readyForGameTitle");
		headerPanel.add(labelTitle);

		labelCountdown.setStyleName("readyForGameCountdown");
		headerPanel.add(labelCountdown);

		final Label labelGuide = new Label("対戦メンバーと問題の準備が完了し次第、自動でゲームへ進みます。");
		labelGuide.setStyleName("readyForGameGuide");
		headerPanel.add(labelGuide);

		labelPlayerCount.setStyleName("readyForGamePlayerCount");
		headerPanel.add(labelPlayerCount);

		setWidth("800px");

		add(headerPanel);
		setCellWidth(headerPanel, "800px");

		panelPlayerList.setStyleName("readyForGamePlayerFrame");
		add(panelPlayerList);
		setCellWidth(panelPlayerList, "800px");
		setCellVerticalAlignment(panelPlayerList, ALIGN_TOP);

		SharedData.get().setIsPlaying(true);
	}

	public void setPlayerList(List<PacketMatchingPlayer> players) {
		final VerticalPanel listPanel = new VerticalPanel();
		listPanel.setStyleName("readyForGamePlayerList");

		for (PacketMatchingPlayer player : players) {
			final PacketPlayerSummary summary = (player.playerSummary != null) ? player.playerSummary
					: PacketPlayerSummary.getDefaultPlayerSummary();
			final FlowPanel rowPanel = new FlowPanel();
			rowPanel.setStyleName("readyForGamePlayerRow");
			rowPanel.setWidth("100%");

			final Image image = new Image(Constant.ICON_URL_PREFIX + player.imageFileName);
			image.setPixelSize(Constant.ICON_SIZE, Constant.ICON_SIZE);
			image.setStyleName("readyForGamePlayerIcon");
			rowPanel.add(image);

			final Label labelName = new Label(summary.level + " " + summary.name);
			labelName.setStyleName("readyForGamePlayerName");
			rowPanel.add(labelName);

			final Label labelPrefecture = new Label(summary.prefecture);
			labelPrefecture.setStyleName("readyForGamePlayerAffiliation");
			rowPanel.add(labelPrefecture);

			final Label labelRating = new Label(String.valueOf(summary.rating));
			labelRating.setStyleName("readyForGamePlayerRating");
			rowPanel.add(labelRating);

			final String greeting = (player.greeting == null || player.greeting.isEmpty()) ? "-"
					: player.greeting;
			final Label labelGreeting = new Label(greeting);
			labelGreeting.setStyleName("readyForGamePlayerGreeting");
			rowPanel.add(labelGreeting);
			
			final Label labelStandby = new Label("準備完了待機");
			labelStandby.setStyleName("readyForGamePlayerBadge");
			rowPanel.add(labelStandby);

			listPanel.add(rowPanel);
		}

		labelPlayerCount.setText("待機中プレイヤー: " + players.size() + "人");
		panelPlayerList.setWidget(listPanel);
	}

	public void setRestSecond(int rest) {
		labelCountdown.setText("ゲーム開始まで残り約" + rest + "秒");
	}
}
