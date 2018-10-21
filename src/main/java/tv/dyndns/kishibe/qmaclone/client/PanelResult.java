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
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketResult;
import tv.dyndns.kishibe.qmaclone.client.report.ProblemReportUi;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelResult extends VerticalPanel {
	public interface MyTemplate extends SafeHtmlTemplates {
		@Template("{0} → <span style='color:green'>{1} ▲</span>")
		SafeHtml up(int oldRating, int newRating);

		@Template("{0} → {1} ＝")
		SafeHtml equal(int oldRating, int newRating);

		@Template("{0} → <span style='color:red'>{1} ▼</span>")
		SafeHtml down(int oldRating, int newRating);
	}

	private static final MyTemplate TEMPLATE = GWT.create(MyTemplate.class);
	private Grid grid;
	private VerticalPanel panelTransition = new VerticalPanel();
	private SimplePanel panelPlayer = new SimplePanel();
	private SimplePanel panelProblem = new SimplePanel();

	public PanelResult(List<PacketProblem> problems) {
		setWidth("800px");
		setHorizontalAlignment(ALIGN_CENTER);
		setVerticalAlignment(ALIGN_MIDDLE);

		Label title = new Label("成績発表");
		add(title);
		setCellHeight(title, "50px");
		setCellWidth(title, "800px");

		{
			add(panelTransition);
			panelTransition.setVisible(false);
			panelTransition.setHorizontalAlignment(ALIGN_CENTER);

			HTML html = new HTML("<a href='http://kishibe.dyndns.tv/QMAClone/'>ロビーに戻る</a>");
			panelTransition.add(html);
			panelTransition.setCellHeight(html, "50px");
			panelTransition.setCellWidth(html, "800px");
		}

		add(panelPlayer);

		// 問題
		panelProblem.setWidget(new ProblemReportUi(problems, true, false,
				Constant.MAX_PROBLEMS_PER_SESSION));

		add(panelProblem);

		SharedData.get().setIsPlaying(false);
	}

	public void setPlayerList(List<PacketResult> result) {
		panelTransition.setVisible(true);

		grid = new Grid(result.size() + 1, 5);
		grid.addStyleName("gridFrame");
		grid.addStyleName("gridFontNormal");
		grid.setText(0, 1, "プレイヤー名");
		grid.setText(0, 2, "得点");
		grid.setText(0, 3, "順位");
		grid.setText(0, 4, "レーティング");

		for (int i = 0; i < result.size(); ++i) {
			PacketResult player = result.get(i);
			Image image = new Image(Constant.ICON_URL_PREFIX + player.imageFileName);
			image.setPixelSize(Constant.ICON_SIZE, Constant.ICON_SIZE);
			int row = i + 1;
			grid.setWidget(row, 0, image);
			grid.setHTML(row, 1, player.playerSummary.asResultSafeHtml());
			grid.setText(row, 2, player.score + "点");
			grid.setText(row, 3, player.rank + "位");

			int newRating = player.newRating;
			if (newRating <= 0) {
				continue;
			}

			SafeHtml ratingChange;
			int oldRating = player.playerSummary.rating;
			if (oldRating < newRating) {
				ratingChange = TEMPLATE.up(oldRating, newRating);
			} else if (oldRating == newRating) {
				ratingChange = TEMPLATE.equal(oldRating, newRating);
			} else {
				ratingChange = TEMPLATE.down(oldRating, newRating);
			}
			grid.setHTML(row, 4, ratingChange.asString());
		}

		panelPlayer.add(grid);
	}
}
