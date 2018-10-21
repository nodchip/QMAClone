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

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelReadyForGame extends VerticalPanel {

	private final Label label = new Label("通信中");

	private final SimplePanel panelGrid = new SimplePanel();

	public PanelReadyForGame() {
		setPixelSize(800, 600);
		setHorizontalAlignment(ALIGN_CENTER);
		setVerticalAlignment(ALIGN_MIDDLE);

		add(label);
		setCellHeight(label, "100px");

		add(panelGrid);
		setCellWidth(panelGrid, "800px");
		setCellHeight(panelGrid, "500px");

		SharedData.get().setIsPlaying(true);
	}

	public void setPlayerList(List<PacketMatchingPlayer> players) {
		final Grid grid = new Grid(players.size(), 3);
		grid.addStyleName("gridFrame");
		grid.addStyleName("gridFontNormal");

		for (int row = 0; row < players.size(); ++row) {
			final PacketMatchingPlayer player = players.get(row);
			final Image image = new Image(Constant.ICON_URL_PREFIX + player.imageFileName);
			image.setPixelSize(Constant.ICON_SIZE, Constant.ICON_SIZE);

			grid.setWidget(row, 0, image);
			grid.setHTML(row, 1, player.playerSummary.asSafeHtml());
			grid.setText(row, 2, player.greeting);
		}

		panelGrid.setWidget(grid);
	}

	public void setRestSecond(int rest) {
		label.setText("ゲーム開始まで残り約" + rest + "秒");
	}
}
