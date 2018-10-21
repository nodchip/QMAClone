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
package tv.dyndns.kishibe.qmaclone.client.geom;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class Point implements Cloneable {
	public static final int INVALID = -1;
	public final int x;
	public final int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 文字列を座標として解釈する。無効な書式の文字列が与えられた場合はnullを返す。
	 * 
	 * @param s
	 *            文字列
	 * @return 座標。無効な文字列が与えられたときは{@code null}。
	 */
	public static Point fromString(String s) {
		if (Strings.isNullOrEmpty(s)) {
			return null;
		}

		List<Double> values = Lists.newArrayList();
		for (String value : s.split(" ")) {
			try {
				values.add(Double.valueOf(value));
			} catch (NumberFormatException e) {
				return null;
			}
		}

		if (values.size() != 2) {
			return null;
		}

		int xx = (int) Math.rint(values.get(0));
		int yy = (int) Math.rint(values.get(1));
		return new Point(xx, yy);
	}

	@Override
	public String toString() {
		return x + " " + y;
	}

	public Point minus(Point rh) {
		return new Point(x - rh.x, y - rh.y);
	}

	public int norm() {
		return x * x + y * y;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Point)) {
			return false;
		}
		Point rh = (Point) obj;
		return x == rh.x && y == rh.y;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(x, y);
	}

	public boolean isValid() {
		return !(x == INVALID || y == INVALID);
	}
}
