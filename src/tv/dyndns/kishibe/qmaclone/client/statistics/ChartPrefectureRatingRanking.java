/**
 * 
 */
package tv.dyndns.kishibe.qmaclone.client.statistics;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.ColumnChart;
import com.google.gwt.visualization.client.visualizations.corechart.ComboChart.Options;

public class ChartPrefectureRatingRanking extends ColumnChart {
	private static final int NUMBER_OF_ITEMS = 10;

	public ChartPrefectureRatingRanking(int[][] ranking) {
		super(table(ranking), options());
	}

	private static AbstractDataTable table(int ranking[][]) {
		DataTable data = DataTable.create();
		data.addRows(NUMBER_OF_ITEMS);

		// 正解率
		data.addColumn(ColumnType.STRING, "正解率");
		data.addColumn(ColumnType.NUMBER, "平均レーティング");
		for (int row = 0; row < NUMBER_OF_ITEMS; ++row) {
			data.setValue(row, 0, Constant.PREFECTURE_NAMES[ranking[row][0]]);
			data.setValue(row, 1, ranking[row][1]);
		}

		return data;
	}

	private static Options options() {
		Options options = Options.create();
		options.setWidth(600);
		options.setHeight(400);
		options.setTitle("県別平均トップレーティング");
		return options;
	}
}
