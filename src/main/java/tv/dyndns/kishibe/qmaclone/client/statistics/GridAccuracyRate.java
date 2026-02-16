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

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class GridAccuracyRate extends Grid {
	private static final String COLOR_HEATMAP_LOW = "#f2f7fc";
	private static final String COLOR_HEATMAP_HIGH = "#b5d3ec";
	private static final String COLOR_HEATMAP_TEXT = "#173a59";

	public GridAccuracyRate() {
		super(12, ProblemGenre.values().length + 1);
		addStyleName("gridFrame");
		addStyleName("gridFontNormal");
		addStyleName("statisticsTable");
		addStyleName("statisticsAccuracyGrid");

		for (int i = 0; i < 12; ++i) {
			for (int j = 0; j < ProblemGenre.values().length + 1; ++j) {
				getCellFormatter().setHorizontalAlignment(i, j, HasHorizontalAlignment.ALIGN_RIGHT);
			}
		}
		getCellFormatter().setStyleName(0, 0, "statisticsAccuracyCornerCell");
		getCellFormatter().setWordWrap(0, 0, false);

		getColumnFormatter().setWidth(0, "92px");
		for (int column = 1; column < ProblemGenre.values().length; ++column) {
			getColumnFormatter().setWidth(column, "72px");
		}
		getColumnFormatter().setWidth(ProblemGenre.values().length, "64px");

		for (int genre = 1; genre < ProblemGenre.values().length; ++genre) {
			final int column = genre;
			getCellFormatter().setStyleName(0, column, "statisticsAccuracyHeaderCell");
			getCellFormatter().setWordWrap(0, column, false);
			setHTML(0, column, formatGenreHeader(ProblemGenre.values()[column]));
		}
		setText(0, ProblemGenre.values().length, "計");
		getCellFormatter().setStyleName(0, ProblemGenre.values().length, "statisticsAccuracyHeaderCell");
		getCellFormatter().setWordWrap(0, ProblemGenre.values().length, false);

		for (int rate = 1; rate <= 10; ++rate) {
			String text = ((rate - 1) * 10) + "-" + (rate * 10) + "%";
			setText(rate, 0, text);
			getCellFormatter().setStyleName(rate, 0, "statisticsAccuracyRowHeaderCell");
			getCellFormatter().setWordWrap(rate, 0, false);
		}
		getCellFormatter().setStyleName(11, 0, "statisticsAccuracyRowHeaderCell");
		getCellFormatter().setWordWrap(11, 0, false);
	}

	public void setData(int rate[][]) {
		final int[] maxCount = new int[ProblemGenre.values().length];
		for (int genre = 0; genre < ProblemGenre.values().length; ++genre) {
			for (int i = 0; i < rate[genre].length; ++i) {
				maxCount[genre] = Math.max(maxCount[genre], rate[genre][i]);
			}
		}

		setText(11, 0, "-");
		for (int index = 0; index < 11; ++index) {
			final int row = index + 1;
			for (int genre = 0; genre < ProblemGenre.values().length; ++genre) {
				final int column = genre == 0 ? ProblemGenre.values().length : genre;

				setText(row, column, "" + rate[genre][index]);
				final Element element = getCellFormatter().getElement(row, column);
				final double colorRatio = maxCount[genre] == 0 ? 0.0d
						: (double) rate[genre][index] / (double) maxCount[genre];
				element.getStyle().setBackgroundColor(
						interpolateColor(COLOR_HEATMAP_LOW, COLOR_HEATMAP_HIGH, colorRatio));
				element.getStyle().setColor(COLOR_HEATMAP_TEXT);
			}
		}
	}

	private static String formatGenreHeader(ProblemGenre genre) {
		if (genre == ProblemGenre.Anige) {
			return "<span class='statisticsAccuracyHeaderLabel'>アニメ<br/>＆ゲーム</span>";
		}
		return "<span class='statisticsAccuracyHeaderLabel'>" + genre.toString() + "</span>";
	}

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

	private static String toHex(int value) {
		String text = Integer.toHexString(Math.max(0, Math.min(255, value)));
		return text.length() == 1 ? "0" + text : text;
	}
}
