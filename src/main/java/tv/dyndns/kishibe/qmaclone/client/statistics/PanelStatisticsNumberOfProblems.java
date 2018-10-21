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
package tv.dyndns.kishibe.qmaclone.client.statistics;

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelStatisticsNumberOfProblems extends VerticalPanel {
	private static final Logger logger = Logger.getLogger(PanelStatisticsNumberOfProblems.class
			.getName());
	private final GridNumberOfProblems gridNumberOfProblem = new GridNumberOfProblems();
	private boolean first = true;
	private final AsyncCallback<int[][]> callbackGetStatisticsOfProblemCount = new AsyncCallback<int[][]>() {
		public void onSuccess(int[][] result) {
			gridNumberOfProblem.setData(result);
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "ジャンル・出題形式一覧の取得に失敗しました", caught);
		}
	};

	public PanelStatisticsNumberOfProblems() {
		setHorizontalAlignment(ALIGN_CENTER);
		add(new HTML("<b>問題数統計</b>"));
		add(gridNumberOfProblem);
	}

	protected void onLoad() {
		super.onLoad();

		if (!first) {
			return;
		}
		first = false;

		Service.Util.getInstance().getStatisticsOfProblemCount(callbackGetStatisticsOfProblemCount);
	}
}
