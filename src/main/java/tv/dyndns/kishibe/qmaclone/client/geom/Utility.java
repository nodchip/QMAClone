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

public class Utility {
	public static int cross(Point a, Point b) {
		return a.x * b.y - b.x * a.y;
	}

	public static int dot(Point a, Point b) {
		return a.x * b.x + a.y * b.y;
	}

	public static boolean onSegment(Point p1, Point p2, Point p3) {
		final Point p12 = p2.minus(p1);
		final Point p13 = p3.minus(p1);
		final Point p21 = p1.minus(p2);
		final Point p23 = p3.minus(p2);
		return cross(p12, p13) == 0 && dot(p12, p13) >= 0 && dot(p21, p23) >= 0;
	}

	public static boolean segmentCross(Point p1, Point p2, Point p3, Point p4) {
		if (p1.equals(p3) || p1.equals(p4) || p2.equals(p3) || p2.equals(p4) || onSegment(p1, p2, p3) || onSegment(p1, p2, p4) || onSegment(p3, p4, p1) || onSegment(p3, p4, p2)) {
			return true;
		}

		// http://piza2.2ch.net/tech/kako/996/996157997.html >>65
		//
		// これをsとtについて解くことにする。
		// D=-(x2-x1)(y4-y3)+(x4-x3)(y2-y1)
		// とすると、D≠0のとき線分を通る直線が交差し、
		// s=(-(x3-x1)(y4-y3)+(x4-x3)(y3-y1))/D
		// t=((x2-x1)(y3-y1)-(x3-x1)(y2-y1))/D
		// となる。
		//
		// このsとtがともに0以上1以下の場合に線分が交差する。

		final long x1 = p1.x;
		final long y1 = p1.y;
		final long x2 = p2.x;
		final long y2 = p2.y;
		final long x3 = p3.x;
		final long y3 = p3.y;
		final long x4 = p4.x;
		final long y4 = p4.y;
		long d = (x4 - x3) * (y2 - y1) - (x2 - x1) * (y4 - y3);
		if (d == 0) {
			return false;
		}
		long s = (x4 - x3) * (y3 - y1) - (x3 - x1) * (y4 - y3);
		long t = (x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1);
		if (d < 0) {
			d *= -1;
			s *= -1;
			t *= -1;
		}
		return 0 <= s && s <= d && 0 <= t && t <= d;
	}
}
