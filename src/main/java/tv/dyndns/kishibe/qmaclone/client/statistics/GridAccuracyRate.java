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

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class GridAccuracyRate extends Grid {
	public GridAccuracyRate() {
		super(12, ProblemGenre.values().length + 1);
		addStyleName("gridFrame");
		addStyleName("gridFontNormal");

		for (int i = 0; i < 12; ++i) {
			for (int j = 0; j < ProblemGenre.values().length + 1; ++j) {
				getCellFormatter().setHorizontalAlignment(i, j, HasHorizontalAlignment.ALIGN_RIGHT);
			}
		}

		for (int genre = 1; genre < ProblemGenre.values().length; ++genre) {
			final int column = genre;
			setText(0, column, ProblemGenre.values()[column].toString());
		}
		setText(0, ProblemGenre.values().length, "計");

		for (int rate = 1; rate <= 10; ++rate) {
			String text = ((rate - 1) * 10) + "-" + (rate * 10) + "%";
			setText(rate, 0, text);
		}
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
				final double colorRatio = (double) rate[genre][index] / (double) maxCount[genre];
				Utility.setBackgroundColor(element, colorRatio);
			}
		}
	}
}
