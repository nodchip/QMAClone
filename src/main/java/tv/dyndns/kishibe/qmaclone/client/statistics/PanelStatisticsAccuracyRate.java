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
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelStatisticsAccuracyRate extends VerticalPanel {
	private static final Logger logger = Logger.getLogger(PanelStatisticsAccuracyRate.class
			.getName());
	private final GridAccuracyRate gridAccuracyRate = new GridAccuracyRate();
	private boolean first = true;
	private final AsyncCallback<int[][]> callbackGetStatisticsOfAccuracyRate = new AsyncCallback<int[][]>() {
		public void onSuccess(int[][] result) {
			gridAccuracyRate.setData(result);
			add(new ChartAccuracyRate(result));
			add(gridAccuracyRate);
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "正解率一覧の取得に失敗しました", caught);
		}
	};

	public PanelStatisticsAccuracyRate() {
		setHorizontalAlignment(ALIGN_CENTER);
	}

	protected void onLoad() {
		super.onLoad();

		if (!first) {
			return;
		}
		first = false;

		Service.Util.getInstance().getStatisticsOfAccuracyRate(callbackGetStatisticsOfAccuracyRate);
	}
}
