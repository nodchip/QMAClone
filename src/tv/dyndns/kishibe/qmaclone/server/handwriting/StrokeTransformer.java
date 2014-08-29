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
package tv.dyndns.kishibe.qmaclone.server.handwriting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StrokeTransformer {
	private static final double EPS = 1e-15;

	public final Map<Character, double[][]> transform(Map<Character, double[][][][]> handwritingData) {
		final Map<Character, double[][]> result = new HashMap<Character, double[][]>();

		for (Entry<Character, double[][][][]> entry : handwritingData.entrySet()) {
			final List<double[]> vectors = new ArrayList<double[]>();

			for (double[][][] strokes : entry.getValue()) {
				scale(strokes);
				vectors.add(transform(strokes));
			}

			result.put(entry.getKey(), vectors.toArray(new double[0][]));
		}

		return result;
	}

	public static void scale(double[][][] strokes) {
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (double[][] stroke : strokes) {
			for (double[] point : stroke) {
				minX = Math.min(minX, point[0]);
				maxX = Math.max(maxX, point[0]);
				minY = Math.min(minY, point[1]);
				maxY = Math.max(maxY, point[1]);
			}
		}

		if (minX == maxX) {
			minX = 0.0;
			maxX = 1.0;
		}
		if (minY == maxY) {
			minY = 0.0;
			maxY = 1.0;
		}

		for (double[][] stroke : strokes) {
			for (double[] point : stroke) {
				point[0] = (point[0] - minX) / (maxX - minX);
				point[1] = (point[1] - minY) / (maxY - minY);
			}
		}
	}

	public double[] transform(double[][][] strokes) {
		final double[] vector = new double[strokes.length * 5];
		int offset = 0;

		// 各画を登録
		for (double[][] stroke : strokes) {
			final double[] values = transform(stroke);
			for (int i = 0; i < values.length; ++i) {
				vector[offset++] = values[i];
			}
		}

		return vector;
	}

	// 三好義昭,"手書き文字評価のための特徴抽出"
	private double[] transform(double[][] stroke) {
		double[] result = new double[5];

		// 長さ
		final double lengthSum = calculateLength(stroke);
		result[0] = lengthSum;

		// 中心位置
		final double[] center = calculateCenter(stroke, lengthSum);
		result[1] = center[0];
		result[2] = center[1];

		// 方向
		final double direction = calculateDirection(stroke);
		result[3] = direction;

		// 曲直
		final double curvature = calculateCurvature(stroke, center);
		result[4] = curvature;

		return result;
	}

	private double calculateCurvature(double[][] stroke, final double[] center) {
		// 最小二乗法近似
		// http://atsugi5761455.fc2web.com/calking16.html
		final int n = stroke.length;
		double sumX = 0;
		double sumX2 = 0;
		double sumX3 = 0;
		double sumX4 = 0;
		double sumY = 0;
		double sumXY = 0;
		double sumX2Y = 0;
		for (double[] point : stroke) {
			final double x = point[0];
			final double y = point[1];
			sumX += x;
			sumX2 += x * x;
			sumX3 += x * x * x;
			sumX4 += x * x * x * x;
			sumY += y;
			sumXY += x * y;
			sumX2Y += x * x * y;
		}

		// Miyazaki's Technique 逆行列
		// http://www.cvl.iis.u-tokyo.ac.jp/~miyazaki/tech/tech23.html
		final double a11 = n;
		final double a12 = sumX;
		final double a13 = sumX2;
		final double a21 = sumX;
		final double a22 = sumX2;
		final double a23 = sumX3;
		final double a31 = sumX2;
		final double a32 = sumX3;
		final double a33 = sumX4;

		// 行列式の0チェックは(ry
		// (ry しちゃダメだったorz
		final double detA = a11 * a22 * a33 + a21 * a32 * a13 + a31 * a12 * a23 - a11 * a32 * a23 - a31 * a22 * a13 - a21 * a12 * a33;
		if (Math.abs(detA) < EPS) {
			return 0;
		}

		// final double b11 = (a22 * a33 - a23 * a32) / detA;
		// final double b12 = (a13 * a32 - a12 * a33) / detA;
		// final double b13 = (a12 * a23 - a13 * a22) / detA;
		final double b21 = (a23 * a31 - a21 * a33) / detA;
		final double b22 = (a11 * a33 - a13 * a31) / detA;
		final double b23 = (a13 * a21 - a11 * a23) / detA;
		final double b31 = (a21 * a32 - a22 * a31) / detA;
		final double b32 = (a12 * a31 - a11 * a32) / detA;
		final double b33 = (a11 * a22 - a12 * a21) / detA;

		final double c1 = sumY;
		final double c2 = sumXY;
		final double c3 = sumX2Y;

		// final double d1 = b11 * c1 + b12 * c2 * b13 * c3;
		final double d2 = b21 * c1 + b22 * c2 * b23 * c3;
		final double d3 = b31 * c1 + b32 * c2 * b33 * c3;

		final double centerX = center[0];
		double r = 2 * d3 * centerX + d2;
		final double curvature = 2 * d3 / Math.pow(1 + r * r, 1.5);
		// System.out.printf("%30.20f\n", curvature);
		return curvature;
	}

	private double calculateDirection(double[][] stroke) {
		final int n = stroke.length;
		double sumXY = 0;
		double sumX = 0;
		double sumY = 0;
		double sumX2 = 0;
		for (double[] point : stroke) {
			final double x = point[0];
			final double y = point[1];
			sumXY += x * y;
			sumX += x;
			sumY += y;
			sumX2 += x * x;
		}

		final double arg = Math.atan2(n * sumXY - sumX * sumY, n * sumX2 - sumX * sumX);
		return arg;
	}

	private double[] calculateCenter(double[][] stroke, double lengthSum) {
		final double[] center = new double[2];
		double length = 0;
		for (int i = 0; i < stroke.length - 1; ++i) {
			final double x0 = stroke[i][0];
			final double y0 = stroke[i][1];
			final double x1 = stroke[i + 1][0];
			final double y1 = stroke[i + 1][1];
			final double dx = x1 - x0;
			final double dy = y1 - y0;
			final double distance = Math.sqrt(dx * dx + dy * dy);
			final double nextLength = length + distance;

			if (nextLength > lengthSum / 2) {
				final double r = (lengthSum / 2 - length) / distance;
				center[0] = (1 - r) * x0 + r * x1;
				center[1] = (1 - r) * y0 + r * y1;
				break;
			}

			length = nextLength;
		}
		return center;
	}

	private double calculateLength(double[][] stroke) {
		double lengthSum = 0;
		for (int i = 0; i < stroke.length - 1; ++i) {
			final double x0 = stroke[i][0];
			final double y0 = stroke[i][1];
			final double x1 = stroke[i + 1][0];
			final double y1 = stroke[i + 1][1];
			final double dx = x1 - x0;
			final double dy = y1 - y0;
			lengthSum += Math.sqrt(dx * dx + dy * dy);
		}
		return lengthSum;
	}
}
