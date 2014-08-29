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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

public class Scaler implements Serializable {
	private static final long serialVersionUID = 5160019855297325209L;
	private static final int BUFFER_SIZE = 1024;
	private double[] minValues = new double[BUFFER_SIZE];
	private double[] maxValues = new double[BUFFER_SIZE];

	public Scaler() {
		Arrays.fill(minValues, Double.POSITIVE_INFINITY);
		Arrays.fill(maxValues, Double.NEGATIVE_INFINITY);
	}

	public void setScale(Map<Character, double[][]> vectorData) {
		for (Entry<Character, double[][]> entry : vectorData.entrySet()) {
			for (double[] vector : entry.getValue()) {
				for (int index = 0; index < vector.length; ++index) {
					minValues[index] = Math.min(minValues[index], vector[index]);
					maxValues[index] = Math.max(maxValues[index], vector[index]);
				}
			}
		}
	}

	public Map<Character, double[][]> scale(Map<Character, double[][]> vectorData) {
		for (Entry<Character, double[][]> entry : vectorData.entrySet()) {
			for (double[] vector : entry.getValue()) {
				scale(vector);
			}
		}

		return vectorData;
	}

	public void scale(double[] vector) {
		for (int index = 0; index < vector.length; ++index) {
			vector[index] = scaleValue(vector[index], minValues[index], maxValues[index]);
		}
	}

	private double scaleValue(double value, double min, double max) {
		if (min == max) {
			return 0;
		}
		return 2.0 * (value - min) / (max - min) - 1.0;
	}
}
