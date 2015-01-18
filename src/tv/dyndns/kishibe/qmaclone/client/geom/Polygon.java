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

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

public class Polygon extends ArrayList<Point> implements Cloneable {

  private static final double EPS = 1e-5;

  public static Polygon fromString(String s) throws PolygonException {
    List<Integer> values = new ArrayList<Integer>();
    for (String value : s.split(" ")) {
      int number;
      try {
        number = Integer.valueOf(value);
      } catch (NumberFormatException e) {
        throw new PolygonException("数字以外の文字が入力されました: " + s, e);
      }

      values.add(number);

      if (values.size() > Constant.MAX_NUMBER_OF_POLYGON_VERTICES * 2) {
        throw new PolygonException("ポリゴンの頂点数が多すぎます: " + s);
      }
    }

    Polygon polygon = new Polygon();
    for (int i = 0; i < values.size() / 2; ++i) {
      polygon.add(new Point(values.get(i * 2), values.get(i * 2 + 1)));
    }

    if (polygon.size() < 3) {
      throw new PolygonException("ポリゴンの頂点数が足りません: " + s);
    }

    if (polygon.hasSelfIntersecting()) {
      throw new PolygonException("ポリゴンが自己交差しています: " + s);
    }

    return polygon;
  }

  public String toString() {
    StringBuilder buffer = new StringBuilder();
    for (Point point : this) {
      if (buffer.length() != 0) {
        buffer.append(" ");
      }
      buffer.append(point);
    }
    return buffer.toString();
  }

  private double amplitude(Point a, Point b, Point c) {
    double x0 = b.x - a.x;
    double y0 = b.y - a.y;
    double x1 = c.x - a.x;
    double y1 = c.y - a.y;
    double result = Math.atan2(y1, x1) - Math.atan2(y0, x0);
    while (result < -Math.PI) {
      result += 2.0 * Math.PI;
    }
    while (result > Math.PI) {
      result -= 2.0 * Math.PI;
    }

    return result;
  }

  public boolean contains(Point point) {
    double argSum = 0;
    for (int i = 0; i < size(); ++i) {
      Point current = get(i);
      Point next = get((i + 1) % size());
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
