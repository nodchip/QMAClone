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
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRatingDistribution;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.AreaChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;

public class PanelStatisticsRatingDistribution extends VerticalPanel {
	private static final Logger logger = Logger.getLogger(PanelStatisticsRatingDistribution.class
			.getName());

	public interface MyTemplate extends SafeHtmlTemplates {
		@Template("個人レーティングの計算式は<a href='http://apps.topcoder.com/wiki/display/tc/Algorithm+Competition+Rating+System' target='_blank'>Algorithm Competition Rating System - TopCoder Wiki</a><br>"
				+ "最低レーティング…{0}<br>" + "最高レーティング…{1}")
		SafeHtml description(int min, int max);
	}

	private static final MyTemplate TEMPLATE = GWT.create(MyTemplate.class);
	private boolean first = true;
	private final AsyncCallback<PacketRatingDistribution> callbackGetRatingDistribution = new AsyncCallback<PacketRatingDistribution>() {
		public void onSuccess(PacketRatingDistribution result) {
			show(result);
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "レーティング分布の取得に失敗しました", caught);
		}
	};

	private void show(PacketRatingDistribution ratingDistribution) {
		int[] distribution = ratingDistribution.distribution;
		int min = ratingDistribution.min;
		int max = ratingDistribution.max;

		add(new RatingDistributionChart(distribution, min, max));
		add(new HTML(TEMPLATE.description(min, max)));
	}

	protected void onLoad() {
		super.onLoad();

		if (!first) {
			return;
		}
		first = false;

		Service.Util.getInstance().getRatingDistribution(callbackGetRatingDistribution);
	}

	private static class RatingDistributionChart extends AreaChart {
		public RatingDistributionChart(int[] distribution, int min, int max) {
			super(table(distribution, min, max), options());
		}

		private static AbstractDataTable table(int[] distribution, int min, int max) {
			DataTable data = DataTable.create();
			data.addRows(distribution.length);

			int saturateThreshold;
			if (distribution.length == 1) {
				saturateThreshold = distribution[0];
			} else {
				List<Integer> sorted = Lists.newArrayList();
				for (int rating : distribution) {
					sorted.add(rating);
				}
				Collections.sort(sorted);
				saturateThreshold = (int) (sorted.get(sorted.size() - 2) * 1.5);
			}

			data.addColumn(ColumnType.NUMBER, "レーティング");
			data.addColumn(ColumnType.NUMBER, "人数");
			for (int row = 0; row < distribution.length; ++row) {
				data.setValue(row, 0, (double) row / distribution.length * (max - min) + min);
				data.setValue(row, 1, Math.min(saturateThreshold, distribution[row]));
			}

			return data;
		}

		private static Options options() {
			Options options = Options.create();
			options.setCurveType("function");
			options.setWidth(600);
			options.setHeight(400);
			options.setTitle("レーティング分布");
			return options;
		}
	}
}
