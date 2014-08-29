package tv.dyndns.kishibe.qmaclone.client.statistics;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;

public class ChartAccuracyRate extends LineChart {
	public ChartAccuracyRate(int rate[][]) {
		super(table(rate), options());
	}

	private static AbstractDataTable table(int rate[][]) {
		Preconditions
				.checkArgument(rate.length == ProblemGenre.values().length);

		DataTable data = DataTable.create();
		data.addRows(11);

		// 正解率
		data.addColumn(ColumnType.STRING, "正解率");
		for (int row = 0; row <= 10; ++row) {
			data.setValue(row, 0, (row * 10) + "%");
		}

		// ジャンルごとの正解率
		int column = 1;
		for (ProblemGenre genre : ProblemGenre.values()) {
			Preconditions.checkArgument(rate[genre.getIndex()].length == 11);

			data.addColumn(ColumnType.NUMBER, genre.toString());

			int sum = 0;
			for (int num : rate[genre.getIndex()]) {
				sum += num;
			}

			for (int row = 0; row <= 10; ++row) {
				data.setValue(row, column, 100.0 * rate[genre.getIndex()][row]
						/ sum);
			}

			++column;
		}

		return data;
	}

	private static Options options() {
		List<String> colors = Lists.newArrayList();
		for (ProblemGenre genre : ProblemGenre.values()) {
			colors.add(genre.getColor());
		}

		Options options = Options.create();
		options.setColors(colors.toArray(new String[0]));
		options.setCurveType("function");
		options.setWidth(600);
		options.setHeight(400);
		options.setTitle("ジャンル別正解率");
		return options;
	}
}
