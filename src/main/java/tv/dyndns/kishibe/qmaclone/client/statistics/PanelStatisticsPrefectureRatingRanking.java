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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelStatisticsPrefectureRatingRanking extends VerticalPanel {
	private static final Logger logger = Logger
			.getLogger(PanelStatisticsPrefectureRatingRanking.class.getName());
	private static final String DESCRIPTION_HTML = "県別に集計したトップランカーたちの平均レーティングです<br>"
			+ "個人レーティングの計算式は<a href='http://apps.topcoder.com/wiki/display/tc/Algorithm+Competition+Rating+System' target='_blank'>Algorithm Competition Rating System - TopCoder Wiki</a><br>"
			+ "平均レーティングの計算式は<a href='http://www.topcoder.com/tc?module=Static&d1=statistics&d2=info&d3=topSchools' target='_blank'>TopCoder Info</a>";
	private final GridPrefectureRanking gridPrefectureRanking = new GridPrefectureRanking();
	private boolean first = true;
	private final AsyncCallback<int[][]> callbackGetPrefectureRanking = new AsyncCallback<int[][]>() {
		public void onSuccess(int[][] result) {
			add(new ChartPrefectureRatingRanking(result));
			add(new HTML(new SafeHtmlBuilder().appendHtmlConstant(DESCRIPTION_HTML).toSafeHtml()));
			gridPrefectureRanking.setData(result);
			add(gridPrefectureRanking);
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "県別平均レーティングの取得に失敗しました", caught);
		}
	};

	public PanelStatisticsPrefectureRatingRanking() {
		setHorizontalAlignment(ALIGN_CENTER);
	}

	protected void onLoad() {
		super.onLoad();

		if (!first) {
			return;
		}
		first = false;

		Service.Util.getInstance().getPrefectureRanking(callbackGetPrefectureRanking);
	}
}
