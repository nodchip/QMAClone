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

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * プレイヤー正解率の統計グリッドを表示します。
 */
public class GridUserAccuracyRate extends Grid {
	private static final String COLOR_HEATMAP_LOW = "#f2f7fc";
	private static final String COLOR_HEATMAP_HIGH = "#b5d3ec";
	private static final String COLOR_HEATMAP_TEXT = "#173a59";

	public GridUserAccuracyRate() {
		super(ProblemType.values().length + 1, ProblemGenre.values().length + 1);
		addStyleName("gridFrame");
		addStyleName("gridFontNormal");
		addStyleName("statisticsTable");
		addStyleName("statisticsUserAccuracyGrid");

		final CellFormatter formatter = getCellFormatter();
		formatter.setStyleName(0, 0, "statisticsUserAccuracyCornerCell");
		formatter.setWordWrap(0, 0, false);

		getColumnFormatter().setWidth(0, "92px");
		for (int column = 1; column < ProblemGenre.values().length; ++column) {
			getColumnFormatter().setWidth(column, "72px");
		}
		getColumnFormatter().setWidth(ProblemGenre.values().length, "64px");

		// 上項目名
		for (int genre = 1; genre < ProblemGenre.values().length; ++genre) {
			final int column = genre;
			setHTML(0, column, formatGenreHeader(ProblemGenre.values()[genre]));
			formatter.setHorizontalAlignment(0, column, HasHorizontalAlignment.ALIGN_CENTER);
			formatter.setStyleName(0, column, "statisticsUserAccuracyHeaderCell");
			formatter.setWordWrap(0, column, false);
		}
		setText(0, ProblemGenre.values().length, "計");
		formatter.setHorizontalAlignment(0, ProblemGenre.values().length,
				HasHorizontalAlignment.ALIGN_CENTER);
		formatter.setStyleName(0, ProblemGenre.values().length, "statisticsUserAccuracyHeaderCell");
		formatter.setWordWrap(0, ProblemGenre.values().length, false);

		// 左項目名
		for (ProblemType type : ProblemType.values()) {
			if (type == ProblemType.Random) {
				continue;
			}

			final int row = type.ordinal();
			setText(row, 0, type.toString());
			formatter.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
			formatter.setStyleName(row, 0, "statisticsUserAccuracyRowHeaderCell");
			formatter.setWordWrap(row, 0, false);
		}
		setText(ProblemType.values().length, 0, "計");
		formatter.setHorizontalAlignment(ProblemType.values().length, 0,
				HasHorizontalAlignment.ALIGN_CENTER);
		formatter.setStyleName(ProblemType.values().length, 0, "statisticsUserAccuracyRowHeaderCell");
		formatter.setWordWrap(ProblemType.values().length, 0, false);

	}

	/**
	 * ジャンル・形式別の正解率を集計して表示します。
	 */
	public void setData(int[][][] correctCount) {
		final CellFormatter formatter = getCellFormatter();

		for (int genre = 0; genre < ProblemGenre.values().length; ++genre) {
			final int column = (genre == 0) ? ProblemGenre.values().length : genre;

			for (int type = 0; type < ProblemType.values().length; ++type) {
				final int row = (type == 0) ? ProblemType.values().length : type;

				final int good = correctCount[genre][type][0];
				final int bad = correctCount[genre][type][1];
				final int total = good + bad;
				final String text;
				if (total == 0) {
					text = "--%";
					final Element element = formatter.getElement(row, column);
					element.getStyle().setBackgroundColor(COLOR_HEATMAP_LOW);
					element.getStyle().setColor(COLOR_HEATMAP_TEXT);
				} else {
					final int ratio = good * 100 / total;
					text = ratio + "%";
					final Element element = formatter.getElement(row, column);
					element.getStyle().setBackgroundColor(interpolateColor(COLOR_HEATMAP_LOW,
							COLOR_HEATMAP_HIGH, ((double) ratio) / 100.0d));
					element.getStyle().setColor(COLOR_HEATMAP_TEXT);
				}

				setText(row, column, text);
				formatter.setHorizontalAlignment(row, column, HasHorizontalAlignment.ALIGN_RIGHT);
			}
		}
	}

	/**
	 * ジャンル見出しの表示文字列を返します。
	 */
	private static String formatGenreHeader(ProblemGenre genre) {
		if (genre == ProblemGenre.Anige) {
			return "<span class='statisticsUserAccuracyHeaderLabel'>アニメ<br/>＆ゲーム</span>";
		}
		return "<span class='statisticsUserAccuracyHeaderLabel'>" + genre.toString() + "</span>";
	}

	/**
	 * 2色間の補間色を返します。
	 */
	private static String interpolateColor(String startColor, String endColor, double ratio) {
		double clampedRatio = Math.max(0.0d, Math.min(1.0d, ratio));
		int startRed = Integer.parseInt(startColor.substring(1, 3), 16);
		int startGreen = Integer.parseInt(startColor.substring(3, 5), 16);
		int startBlue = Integer.parseInt(startColor.substring(5, 7), 16);
		int endRed = Integer.parseInt(endColor.substring(1, 3), 16);
		int endGreen = Integer.parseInt(endColor.substring(3, 5), 16);
		int endBlue = Integer.parseInt(endColor.substring(5, 7), 16);
		int red = (int) Math.round(startRed + (endRed - startRed) * clampedRatio);
		int green = (int) Math.round(startGreen + (endGreen - startGreen) * clampedRatio);
		int blue = (int) Math.round(startBlue + (endBlue - startBlue) * clampedRatio);
		return "#" + toHex(red) + toHex(green) + toHex(blue);
	}

	/**
	 * 0-255 の値を 2 桁16進文字列へ変換します。
	 */
	private static String toHex(int value) {
		String text = Integer.toHexString(Math.max(0, Math.min(255, value)));
		return text.length() == 1 ? "0" + text : text;
	}
}
