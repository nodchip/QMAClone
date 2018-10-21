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
package tv.dyndns.kishibe.qmaclone.client.game.panel;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.WidgetTimeProgressBar;
import tv.dyndns.kishibe.qmaclone.client.game.input.InputWidget;
import tv.dyndns.kishibe.qmaclone.client.game.sentence.WidgetProblemSentence;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class QuestionPanel extends VerticalPanel {
	protected final PacketProblem problem;
	protected final AnswerView answerView;
	private InputWidget inputWidget;
	private final SimplePanel panelTimeProgressBar = new SimplePanel();
	private final Label labelRatio = new Label("正解率:???");
	private final Label labelCreator = new Label("作者:???");
	private final SessionData sessionData;

	protected QuestionPanel(PacketProblem problem, SessionData sessionData) {
		this.problem = Preconditions.checkNotNull(problem);
		this.sessionData = Preconditions.checkNotNull(sessionData);
		setHorizontalAlignment(ALIGN_CENTER);

		HorizontalPanel panelProblemInfo = new HorizontalPanel();
		panelProblemInfo.addStyleName("gwt-HorizontalPanel-problemInformation");
		panelProblemInfo.add(new Label(problem.genre.toString()));
		panelProblemInfo.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));
		panelProblemInfo.add(new Label(problem.type.toString()));
		panelProblemInfo.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));
		panelProblemInfo.add(labelRatio);

		if (!Strings.isNullOrEmpty(problem.creator)) {
			panelProblemInfo.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));
			panelProblemInfo.add(labelCreator);
		}

		// この順番で呼び出さなければならない(あまりよい設計とは言えない)
		WidgetProblemSentence problemStatement = createWidgetProblemSentence();
		answerView = createAnswerView();
		inputWidget = createWidgetInput();

		add(panelProblemInfo);
		add(problemStatement);
		add(panelTimeProgressBar);
		add(answerView);
		add(inputWidget);

		setWidth("600px");

		setCellHeight(inputWidget, "300px");
	}

	protected abstract WidgetProblemSentence createWidgetProblemSentence();

	protected abstract AnswerView createAnswerView();

	protected abstract InputWidget createWidgetInput();

	public void setWidgetTimeProgressBar(WidgetTimeProgressBar timeProgressBar) {
		panelTimeProgressBar.clear();
		panelTimeProgressBar.setWidget(timeProgressBar);
	}

	public void enableInput(boolean enable) {
		inputWidget.enable(enable);
	}

	public void showCorrectRatioAndCreator() {
		String stringRatio;
		if (problem.good + problem.bad == 0) {
			stringRatio = "正解率:New";
		} else if (problem.good + problem.bad < Constant.MAX_RATIO_CALCULATING) {
			stringRatio = "正解率:集計中";
		} else {
			stringRatio = "正解率:" + ((problem.good * 100) / (problem.good + problem.bad)) + "%";
		}

		labelRatio.setText(stringRatio);

		if (!Strings.isNullOrEmpty(problem.creator)) {
			labelCreator.setText("作者:" + problem.creator);
		}
	}

	public void onReceiveGameStatus(PacketGameStatus gameStatus) {
		inputWidget.onReceivedGameStatus(gameStatus);
	}

	protected SessionData getSessionData() {
		return sessionData;
	}
}
