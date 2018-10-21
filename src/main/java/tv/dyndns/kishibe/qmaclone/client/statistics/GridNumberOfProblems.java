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

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class GridNumberOfProblems extends Grid {
	public GridNumberOfProblems() {
		super(ProblemType.values().length + 1, ProblemGenre.values().length + 1);
		addStyleName("gridFrame");
		addStyleName("gridFontNormal");

		final int sizeOfColumn = ProblemGenre.values().length + 1;
		final int sizeOfRow = ProblemType.values().length + 1;

		final CellFormatter formatter = getCellFormatter();
		for (int row = 0; row < sizeOfRow; ++row) {
			for (int column = 0; column < sizeOfColumn; ++column) {
				formatter.setHorizontalAlignment(row, column, HasHorizontalAlignment.ALIGN_RIGHT);
			}
		}

		for (int column = 1; column < sizeOfColumn - 1; ++column) {
			setText(0, column, ProblemGenre.values()[column].toString());
		}
		setText(0, sizeOfColumn - 1, "計");

		for (int row = 1; row < sizeOfRow - 1; ++row) {
			setText(row, 0, ProblemType.values()[row].toString());
		}
		setText(sizeOfRow - 1, 0, "計");
	}

	public void setData(int[][] count) {
		final int sizeOfColumn = ProblemGenre.values().length + 1;
		final int sizeOfRow = ProblemType.values().length + 1;

		// 縦横を逆にする
		final int[][] matrix = new int[sizeOfRow][sizeOfColumn];
		for (int row = 0; row < ProblemType.values().length; ++row) {
			for (int column = 0; column < ProblemGenre.values().length; ++column) {
				matrix[row == 0 ? sizeOfRow - 1 : row][column == 0 ? sizeOfColumn - 1 : column] = count[column][row];
			}
		}

		for (int row = 1; row < sizeOfRow; ++row) {
			for (int column = 1; column < sizeOfColumn; ++column) {
				setText(row, column, "" + matrix[row][column]);
			}
		}

		setBackgroundColor(1, ProblemType.numberOfTypesWithoutRandom, 1,
				ProblemGenre.values().length, matrix);
		setBackgroundColor(ProblemType.numberOfTypesWithoutRandom, ProblemType.values().length, 1,
				ProblemGenre.values().length, matrix);
		setBackgroundColor(ProblemType.values().length, ProblemType.values().length + 1, 1,
				ProblemGenre.values().length, matrix);
		setBackgroundColor(1, ProblemType.numberOfTypesWithoutRandom, ProblemGenre.values().length,
				ProblemGenre.values().length + 1, matrix);
		setBackgroundColor(ProblemType.numberOfTypesWithoutRandom, ProblemType.values().length,
				ProblemGenre.values().length, ProblemGenre.values().length + 1, matrix);
	}

	private void setBackgroundColor(int rowStart, int rowEnd, int columnStart, int columnEnd,
			int[][] matrix) {
		int maxValue = Integer.MIN_VALUE;
		for (int row = rowStart; row < rowEnd; ++row) {
			for (int column = columnStart; column < columnEnd; ++column) {
				maxValue = Math.max(maxValue, matrix[row][column]);
			}
		}

		final CellFormatter formatter = getCellFormatter();
		for (int row = rowStart; row < rowEnd; ++row) {
			for (int column = columnStart; column < columnEnd; ++column) {
				final Element elementCell = formatter.getElement(row, column);
				final double colorRatio = (double) matrix[row][column] / (double) maxValue;
				Utility.setBackgroundColor(elementCell, colorRatio);
			}
		}
	}
}
