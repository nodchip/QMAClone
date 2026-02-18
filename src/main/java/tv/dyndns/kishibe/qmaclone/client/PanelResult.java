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
import com.google.gwt.user.client.ui.FlowPanel;
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
	private final FlowPanel resultHero = new FlowPanel();
	private final FlowPanel resultRankingList = new FlowPanel();
	private final FlowPanel resultFooter = new FlowPanel();
	private final SimplePanel panelProblem = new SimplePanel();

	public PanelResult(List<PacketProblem> problems) {
		setWidth("800px");
		setHorizontalAlignment(ALIGN_CENTER);
		setVerticalAlignment(ALIGN_MIDDLE);
		setStyleName("resultRoot");

		Label title = new Label("成績発表");
		add(title);
		setCellHeight(title, "50px");
		setCellWidth(title, "800px");

		{
			add(resultFooter);
			resultFooter.setStyleName("resultFooter");
			HTML html = new HTML("<a href='http://kishibe.dyndns.tv/QMAClone/'>ロビーに戻る</a>");
			resultFooter.add(html);
		}

		resultHero.setStyleName("resultHero");
		add(resultHero);

		resultRankingList.setStyleName("resultRankingList");
		add(resultRankingList);

		// 問題
		panelProblem.setWidget(new ProblemReportUi(problems, true, false,
				Constant.MAX_PROBLEMS_PER_SESSION));

		add(panelProblem);

		SharedData.get().setIsPlaying(false);
	}

	public void setPlayerList(List<PacketResult> result) {
		setPlayerList(result, -1);
	}

	public void setPlayerList(List<PacketResult> result, int myPlayerListId) {
		resultHero.clear();
		resultRankingList.clear();

		PacketResult myResult = null;
		for (PacketResult player : result) {
			if (player.playerListId == myPlayerListId) {
				myResult = player;
				break;
			}
		}
		if (myResult == null && !result.isEmpty()) {
			myResult = result.get(0);
		}

		if (myResult != null) {
			FlowPanel heroCard = new FlowPanel();
			heroCard.setStyleName("resultHeroCard");
			heroCard.add(new HTML("<b>あなたの結果</b>"));
			heroCard.add(new HTML("順位: " + myResult.rank + "位"));
			heroCard.add(new HTML("得点: " + myResult.score + "点"));
			heroCard.add(new HTML("レーティング: "
					+ renderRatingChange(myResult.playerSummary.rating, myResult.newRating).asString()));
			resultHero.add(heroCard);
		}

		for (int i = 0; i < result.size(); ++i) {
			PacketResult player = result.get(i);
			FlowPanel card = new FlowPanel();
			card.setStyleName("resultRankingCard");
			if (player.playerListId == myPlayerListId) {
				card.addStyleName("resultRankingCardMine");
			}
			Image image = new Image(Constant.ICON_URL_PREFIX + player.imageFileName);
			image.setPixelSize(Constant.ICON_SIZE, Constant.ICON_SIZE);
			image.addStyleName("resultRankingCardIcon");
			card.add(image);
			card.add(new HTML("<b>" + player.rank + "位</b> " + player.playerSummary.asResultSafeHtml().asString()));
			card.add(new HTML("得点: " + player.score + "点"));
			card.add(new HTML("レーティング: "
					+ renderRatingChange(player.playerSummary.rating, player.newRating).asString()));
			resultRankingList.add(card);
		}
	}

	private SafeHtml renderRatingChange(int oldRating, int newRating) {
		if (newRating <= 0) {
			return TEMPLATE.equal(oldRating, oldRating);
		}
		if (oldRating < newRating) {
			return TEMPLATE.up(oldRating, newRating);
		}
		if (oldRating == newRating) {
			return TEMPLATE.equal(oldRating, newRating);
		}
		return TEMPLATE.down(oldRating, newRating);
	}
}
