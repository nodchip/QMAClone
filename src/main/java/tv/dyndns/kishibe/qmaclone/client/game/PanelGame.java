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
package tv.dyndns.kishibe.qmaclone.client.game;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.left.WidgetPlayerList;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelGame extends HorizontalPanel {
	private SimplePanel panel[];
	private VerticalPanel verticalPanel;
	private Label labelCounter;
	private Label labelScore;

	public PanelGame() {
		setWidth("800px");

		verticalPanel = new VerticalPanel();
		panel = new SimplePanel[2];
		for (int i = 0; i < 2; ++i) {
			panel[i] = new SimplePanel();
		}

		labelCounter = new Label("ゲーム開始");
		labelScore = new Label("得点 : 0");

		verticalPanel.add(labelCounter);
		verticalPanel.add(labelScore);
		verticalPanel.add(panel[0]);

		add(verticalPanel);
		add(panel[1]);
		setCellWidth(panel[0], "200px");
		setCellHeight(panel[0], "600px");
		setCellWidth(panel[1], "600px");
	}

	public void setPlayerList(WidgetPlayerList list) {
		panel[0].clear();
		panel[0].setWidget(list);
	}

	public void setQuestionPanel(QuestionPanel question) {
		panel[1].setWidget(question);
	}

	public void setQuestionNumber(int number) {
		int rest = Constant.MAX_PROBLEMS_PER_SESSION - number - 1;
		labelCounter.setText((number + 1) + "問目 残り" + rest + "問");
	}

	public void setScore(int score) {
		labelScore.setText("得点 : " + score);
	}
}
