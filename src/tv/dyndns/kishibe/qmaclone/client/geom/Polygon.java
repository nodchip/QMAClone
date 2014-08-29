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

import java.util.ArrayList;
import java.util.List;

public class Polygon extends ArrayList<Point> implements Cloneable {
	private static final long serialVersionUID = -6022314431237885983L;

	public static Polygon fromString(String s) {
		try {
			List<Integer> values = new ArrayList<Integer>();
			for (String value : s.split(" ")) {
				values.add(Integer.valueOf(value));
			}

			Polygon polygon = new Polygon();
			for (int i = 0; i < values.size() / 2; ++i) {
				polygon.add(new Point(values.get(i * 2), values.get(i * 2 + 1)));
			}

			if (polygon.size() < 3) {
				throw new Exception("ポリゴンの頂点数が足りません");
			}

			if (polygon.hasSelfIntersecting()) {
				throw new Exception("ポリゴンが自己交差しています");
			}

			return polygon;

		} catch (Exception e) {
			// Log.info("ポリゴン情報のパースに失敗しました", e);
			return null;
		}
	}

	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		for (Point point : this) {
			if (buffer.length() != 0) {
				buffer.append(" ");
			}
			buffer.append(point);
		}
		return buffer.toString();
	}

	private double amplitude(Point a, Point b, Point c) {
		final double x0 = b.x - a.x;
		final double y0 = b.y - a.y;
		final double x1 = c.x - a.x;
		final double y1 = c.y - a.y;
		double result = Math.atan2(y1, x1) - Math.atan2(y0, x0);
		while (result < -Math.PI) {
			result += 2.0 * Math.PI;
		}
		while (result > Math.PI) {
			result -= 2.0 * Math.PI;
		}

		return result;
	}

	private static final double EPS = 1e-5;

	public boolean contains(Point point) {
		double argSum = 0;
		for (int i = 0; i < size(); ++i) {
			final Point current = get(i);
			final Point next = get((i + 1) % size());
			if (new Segment(current, next).on(point)) {
				return true;
			}
			argSum += amplitude(point, current, next);
		}

		return Math.abs(argSum) > EPS;
	}

	public boolean isCompleted() {
		return 3 <= size() && !hasSelfIntersecting();
	}

	private boolean hasSelfIntersecting() {
		int n = size();
		for (int i = 0; i < n; ++i) {
			Segment currentSegment = new Segment(get(i), get((i + 1) % n));

			for (int j = 0; j < n; ++j) {
				if (i == j || i == (j + 1) % n || (i + 1) % n == j) {
					continue;
				}

				Segment segment = new Segment(get(j), get((j + 1) % n));
				if (segment.cross(currentSegment)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public Polygon clone() {
		// PointはImmutableなので、Listのclone()を行うだけで良い
		return (Polygon) super.clone();
	}
}
