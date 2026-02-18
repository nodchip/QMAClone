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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelMatching extends VerticalPanel implements ClickHandler {
	private SceneMatching scene;

	private final Label labelCountdown;
	private final Label labelPlayerCount;

	private SimplePanel gridPanel = new SimplePanel();

	private Button buttonDontWait = new Button("マッチングを待たずに開始する", this);;

	public PanelMatching(SceneMatching sceneMatching) {
		setHorizontalAlignment(ALIGN_CENTER);
		setVerticalAlignment(ALIGN_MIDDLE);
		setStyleName("matchingRoot");

		scene = sceneMatching;

		final VerticalPanel headerPanel = new VerticalPanel();
		headerPanel.setStyleName("matchingHeader");

		final Label labelTitle = new Label("マッチング中");
		labelTitle.setStyleName("matchingTitle");
		headerPanel.add(labelTitle);

		labelCountdown = new Label("通信待機中です。しばらくお待ちください。");
		labelCountdown.setStyleName("matchingCountdown");
		headerPanel.add(labelCountdown);

		final Label labelGuide = new Label("対戦相手が集まり次第、ゲームを開始します。");
		labelGuide.setStyleName("matchingGuide");
		headerPanel.add(labelGuide);

		labelPlayerCount = new Label("参加プレイヤー: 0人");
		labelPlayerCount.setStyleName("matchingPlayerCount");
		headerPanel.add(labelPlayerCount);

		setWidth("800px");
		setHeight("600px");

		add(headerPanel);
		setCellWidth(headerPanel, "800px");
		setCellHeight(headerPanel, "140px");

		buttonDontWait.setStyleName("matchingSkipButton");
		add(buttonDontWait);
		setCellWidth(buttonDontWait, "800px");
		setCellHeight(buttonDontWait, "44px");

		gridPanel.setStyleName("matchingPlayerFrame");
		add(gridPanel);
		setCellWidth(gridPanel, "800px");
		setCellHeight(gridPanel, "416px");
		setCellVerticalAlignment(gridPanel, ALIGN_TOP);
	}

	public void setRestSecond(int rest) {
		labelCountdown.setText("マッチング終了まで残り約" + rest + "秒");
	}

	public void setPlayerList(List<PacketMatchingPlayer> players) {
		final VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setStyleName("matchingPlayerList");

		for (PacketMatchingPlayer player : players) {
			final HorizontalPanel horizontalPanel = new HorizontalPanel();
			horizontalPanel.setVerticalAlignment(ALIGN_MIDDLE);
			horizontalPanel.setStyleName("matchingPlayerRow");

			final Image image = new Image(Constant.ICON_URL_PREFIX + player.imageFileName);
			image.setPixelSize(Constant.ICON_SIZE, Constant.ICON_SIZE);
			image.setStyleName("matchingPlayerIcon");
			horizontalPanel.add(image);

			final FlowPanel summaryPanel = new FlowPanel();
			summaryPanel.setStyleName("matchingPlayerSummaryArea");

			final HTML html = new HTML(player.playerSummary.asSafeHtml());
			html.setStyleName("matchingPlayerSummary");
			if (player.isRequestSkip) {
				html.addStyleName("matchingPlayerSummarySkip");
				horizontalPanel.addStyleName("matchingPlayerRowSkip");
			}
			summaryPanel.add(html);

			if (player.isRequestSkip) {
				final Label labelSkip = new Label("待機スキップ希望");
				labelSkip.setStyleName("matchingSkipBadge");
				summaryPanel.add(labelSkip);
			}

			horizontalPanel.add(summaryPanel);

			verticalPanel.add(horizontalPanel);
		}

		labelPlayerCount.setText("参加プレイヤー: " + players.size() + "人");
		gridPanel.setWidget(verticalPanel);
	}

	@Override
	public void onClick(ClickEvent event) {
		final Object sender = event.getSource();
		if (sender == buttonDontWait) {
			buttonDontWait.setEnabled(false);
			scene.requestSkip();
		}
	}
}
