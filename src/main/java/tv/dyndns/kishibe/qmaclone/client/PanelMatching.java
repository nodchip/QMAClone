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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelMatching extends VerticalPanel implements ClickHandler {
	private SceneMatching scene;

	private Label label;

	private SimplePanel gridPanel = new SimplePanel();

	private Button buttonDontWait = new Button("マッチングを待たずに開始する", this);;

	public PanelMatching(SceneMatching sceneMatching) {
		setHorizontalAlignment(ALIGN_CENTER);
		setVerticalAlignment(ALIGN_MIDDLE);

		scene = sceneMatching;

		label = new Label("通信待機中です\nしばらくお待ちください");

		setWidth("800px");
		setHeight("600px");

		add(label);
		setCellWidth(label, "800px");
		setCellHeight(label, "100px");

		add(buttonDontWait);
		setCellWidth(buttonDontWait, "800px");
		setCellHeight(buttonDontWait, "50px");

		add(gridPanel);
		setCellWidth(gridPanel, "800px");
		setCellHeight(gridPanel, "450px");
		setCellVerticalAlignment(gridPanel, ALIGN_TOP);
	}

	public void setRestSecond(int rest) {
		label.setText("マッチング終了まで残り約" + rest + "秒");
	}

	public void setPlayerList(List<PacketMatchingPlayer> players) {
		final VerticalPanel verticalPanel = new VerticalPanel();

		for (PacketMatchingPlayer player : players) {
			final HorizontalPanel horizontalPanel = new HorizontalPanel();
			horizontalPanel.setVerticalAlignment(ALIGN_MIDDLE);

			final Image image = new Image(Constant.ICON_URL_PREFIX + player.imageFileName);
			image.setPixelSize(Constant.ICON_SIZE, Constant.ICON_SIZE);
			horizontalPanel.add(image);

			final HTML html = new HTML(player.playerSummary.asSafeHtml());
			if (player.isRequestSkip) {
				html.addStyleDependentName("matchingSkip");
			}
			horizontalPanel.add(html);

			verticalPanel.add(horizontalPanel);
		}

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
