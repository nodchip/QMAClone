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

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.UserData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;

public class PanelStatisticsRatingHistory extends VerticalPanel {
	private static final Logger logger = Logger.getLogger(PanelStatisticsRatingHistory.class
			.getName());

	public interface MyTemplate extends SafeHtmlTemplates {
		@Template("<div class='statisticsRatingHistorySummary'>"
				+ "<div class='statisticsRatingHistorySummaryLead'>過去100戦のレーティング推移です。</div>"
				+ "<div class='statisticsRatingHistoryMetrics'>"
				+ "<div class='statisticsRatingHistoryMetric'><span class='statisticsRatingHistoryMetricLabel'>最低</span><span class='statisticsRatingHistoryMetricValue'>{0}</span></div>"
				+ "<div class='statisticsRatingHistoryMetric'><span class='statisticsRatingHistoryMetricLabel'>最高</span><span class='statisticsRatingHistoryMetricValue'>{1}</span></div>"
				+ "<div class='statisticsRatingHistoryMetric'><span class='statisticsRatingHistoryMetricLabel'>平均</span><span class='statisticsRatingHistoryMetricValue'>{2}</span></div>"
				+ "</div>"
				+ "<div class='statisticsRatingHistorySummaryNote'>"
				+ "個人レーティングの計算式: <a href='http://apps.topcoder.com/wiki/display/tc/Algorithm+Competition+Rating+System' target='_blank'>Algorithm Competition Rating System - TopCoder Wiki</a>"
				+ "</div>"
				+ "</div>")
		SafeHtml description(int min, int max, int average);
	}

	private static final MyTemplate TEMPLATE = GWT.create(MyTemplate.class);
	private static final PanelStatisticsRatingHistory instance = new PanelStatisticsRatingHistory();

	public static PanelStatisticsRatingHistory getInstance() {
		return instance;
	}

	private boolean first = true;
	private final AsyncCallback<List<Integer>> callbackGetRatingHistory = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<List<Integer>>() {
		public void onSuccess(List<Integer> result) {
			show(result);
		}

		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "レーティング履歴の取得に失敗しました", caught);
		}
	};

	private PanelStatisticsRatingHistory() {
		setHorizontalAlignment(ALIGN_CENTER);
		addStyleName("statisticsCard");
		addStyleName("statisticsSectionCard");
	}

	public void resetFlag() {
		first = true;
	}

	private void show(List<Integer> data) {
		Collections.reverse(data);

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int average = 0;
		for (int value : data) {
			if (min > value) {
				min = value;
			}
			if (max < value) {
				max = value;
			}
			average += value;
		}
		average /= data.size();

		if (min == Integer.MAX_VALUE) {
			HTML empty = new HTML("対戦履歴がありません。対戦後に自動で表示されます。");
			empty.addStyleName("statisticsRatingHistoryEmpty");
			add(empty);
			return;
		}

		clear();

		HTML title = new HTML("<b>レーティング履歴</b>");
		title.addStyleName("statisticsSectionTitle");
		add(title);
		RatingHistoryChart chart = new RatingHistoryChart(data);
		chart.addStyleName("statisticsRatingHistoryChart");
		add(chart);
		HTML description = new HTML(TEMPLATE.description(min, max, average));
		description.addStyleName("statisticsDescription");
		add(description);
	}

	protected void onLoad() {
		super.onLoad();

		if (!first) {
			return;
		}
		first = false;

		Service.Util.getInstance().getRatingHistory(UserData.get().getUserCode(),
				callbackGetRatingHistory);
	}

	private static class RatingHistoryChart extends LineChart {
		public RatingHistoryChart(List<Integer> rating) {
			super(table(rating), options());
		}

		private static AbstractDataTable table(List<Integer> rating) {
			DataTable data = DataTable.create();
			data.addRows(rating.size());

			// 正解率
			data.addColumn(ColumnType.NUMBER, "回数");
			data.addColumn(ColumnType.NUMBER, "レーティング");
			for (int row = 0; row < rating.size(); ++row) {
				data.setValue(row, 0, row);
				data.setValue(row, 1, rating.get(row));
			}

			return data;
		}

		private static Options options() {
			Options options = Options.create();
			options.setCurveType("function");
			options.setWidth(600);
			options.setHeight(400);
			options.setTitle("レーティング履歴");
			return options;
		}
	}
}

