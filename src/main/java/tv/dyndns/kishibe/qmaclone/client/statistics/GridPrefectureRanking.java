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

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * 県別平均トップレーティングの一覧グリッドを表示します。
 */
public class GridPrefectureRanking extends Grid {
	public GridPrefectureRanking() {
		addStyleName("gridFrame");
		addStyleName("gridFontNormal");
		addStyleName("statisticsTable");
		addStyleName("statisticsPrefectureGrid");
	}

	/**
	 * 都道府県ごとの順位と平均トップレーティングを表示します。
	 */
	public void setData(int[][] data) {
		resize(data.length + 1, 3);

		CellFormatter formatter = getCellFormatter();
		formatter.setStyleName(0, 0, "statisticsPrefectureCornerCell");
		formatter.setWordWrap(0, 0, false);

		setHTML(0, 0, "順位");
		setHTML(0, 1, "都道府県");
		setHTML(0, 2, "<span class='statisticsPrefectureHeaderLabel'>平均トップ<br/>レーティング</span>");
		formatter.setHorizontalAlignment(0, 0, HorizontalPanel.ALIGN_CENTER);
		formatter.setHorizontalAlignment(0, 1, HorizontalPanel.ALIGN_CENTER);
		formatter.setHorizontalAlignment(0, 2, HorizontalPanel.ALIGN_CENTER);
		formatter.setStyleName(0, 1, "statisticsPrefectureHeaderCell");
		formatter.setStyleName(0, 2, "statisticsPrefectureHeaderCell");
		formatter.setWordWrap(0, 1, false);
		formatter.setWordWrap(0, 2, false);

		for (int i = 0; i < data.length; ++i) {
			final int prefecture = data[i][0];
			final int rating = data[i][1];
			setText(i + 1, 0, "" + (i + 1));
			setText(i + 1, 1, Constant.PREFECTURE_NAMES[prefecture]);
			setText(i + 1, 2, "" + rating);
			formatter.setHorizontalAlignment(i + 1, 0, HorizontalPanel.ALIGN_RIGHT);
			formatter.setHorizontalAlignment(i + 1, 1, HorizontalPanel.ALIGN_CENTER);
			formatter.setHorizontalAlignment(i + 1, 2, HorizontalPanel.ALIGN_RIGHT);
			formatter.setStyleName(i + 1, 0, "statisticsPrefectureRankCell");
			formatter.setStyleName(i + 1, 1, "statisticsPrefecturePrefectureCell");
			formatter.setStyleName(i + 1, 2, "statisticsPrefectureRatingCell");
			if ((i % 2) == 1) {
				formatter.addStyleName(i + 1, 0, "statisticsPrefectureBodyCellEven");
				formatter.addStyleName(i + 1, 1, "statisticsPrefectureBodyCellEven");
				formatter.addStyleName(i + 1, 2, "statisticsPrefectureBodyCellEven");
			}
			if (i < 3) {
				formatter.addStyleName(i + 1, 2, "statisticsPrefectureTop3Cell");
			}
		}
	}
}
