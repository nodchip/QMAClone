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

import tv.dyndns.kishibe.qmaclone.client.Utility;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class GridUserAccuracyRate extends Grid {
	public GridUserAccuracyRate() {
		super(ProblemType.values().length + 1, ProblemGenre.values().length + 1);
		addStyleName("gridFrame");
		addStyleName("gridFontNormal");

		final CellFormatter formatter = getCellFormatter();

		// 上項目名
		for (int genre = 1; genre < ProblemGenre.values().length; ++genre) {
			final int column = genre;
			setText(0, column, ProblemGenre.values()[genre].toString());
			formatter.setHorizontalAlignment(0, column, HasHorizontalAlignment.ALIGN_CENTER);
		}
		setText(0, ProblemGenre.values().length, "計");
		formatter.setHorizontalAlignment(0, ProblemGenre.values().length,
				HasHorizontalAlignment.ALIGN_CENTER);

		// 左項目名
		for (ProblemType type : ProblemType.values()) {
			if (type == ProblemType.Random) {
				continue;
			}

			final int row = type.ordinal();
			setText(row, 0, type.toString());
			formatter.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
		}
		setText(ProblemType.values().length, 0, "計");
		formatter.setHorizontalAlignment(ProblemType.values().length, 0,
				HasHorizontalAlignment.ALIGN_CENTER);

	}

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
				} else {
					final int ratio = good * 100 / total;
					text = ratio + "%";
					Utility.setBackgroundColor(formatter.getElement(row, column),
							((double) ratio) / 100.0);
				}

				setText(row, column, text);
				formatter.setHorizontalAlignment(row, column, HasHorizontalAlignment.ALIGN_RIGHT);
			}
		}
	}
}
