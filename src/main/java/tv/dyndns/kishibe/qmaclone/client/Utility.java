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
package tv.dyndns.kishibe.qmaclone.client;

import static com.google.common.base.Strings.padStart;
import static java.lang.String.valueOf;

import java.util.Date;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

public class Utility {
	private static final double[] TEST_RATIO = { 0.01, 0.24, 0.26, 0.49, 0.51, 0.74, 0.76, 0.99 };
	private static final int MIN_COLOR = 208;
	private static final int MAX_COLOR = 255;
	private static final String HEX_LETTER = "0123456789ABCDEF";
	private static final int PRIME_NUMBER_BIG = 1182079739;
	private static final String TRIP_LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private Utility() {
	}

	public void testBackgroundColor() {
		for (int i = 0; i < TEST_RATIO.length; ++i) {
			final String string = createBackgroundColorString(TEST_RATIO[i]);
			System.out.println(string);
		}
	}

	public static void setBackgroundColor(Element element, double colorRatio) {
		DOM.setElementProperty(element, "bgColor", createBackgroundColorString(colorRatio));
	}

	public static void setBackgroundColor(Element element, String color) {
		DOM.setElementProperty(element, "bgColor", color);
	}

	public static String createBackgroundColorString(double ratio) {
		final int r, g, b;
		ratio = Math.min(1.0, Math.max(0, ratio));

		if (ratio < 1.0 / 4.0) {
			ratio = ratio * 4.0;
			r = MAX_COLOR;
			g = (int) (ratio * (MAX_COLOR - MIN_COLOR)) + MIN_COLOR;
			b = MIN_COLOR;

		} else if (ratio < 2.0 / 4.0) {
			ratio = 1.0 - (ratio - 1.0 / 4.0) * 4.0;
			r = (int) (ratio * (MAX_COLOR - MIN_COLOR)) + MIN_COLOR;
			g = MAX_COLOR;
			b = MIN_COLOR;

		} else if (ratio < 3.0 / 4.0) {
			ratio = (ratio - 2.0 / 4.0) * 4.0;
			r = MIN_COLOR;
			g = MAX_COLOR;
			b = (int) (ratio * (MAX_COLOR - MIN_COLOR)) + MIN_COLOR;

		} else {
			ratio = 1.0 - (ratio - 3.0 / 4.0) * 4.0;
			r = MIN_COLOR;
			g = (int) (ratio * (MAX_COLOR - MIN_COLOR)) + MIN_COLOR;
			b = MAX_COLOR;
		}

		return createBackgroundColorString(r, g, b);
	}

	private static String createBackgroundColorString(int r, int g, int b) {
		return "#" + toHexString(r, 2) + toHexString(g, 2) + toHexString(b, 2);
	}

	private static String toHexString(int number, int digit) {
		String result = "";
		while (digit-- > 0) {
			int n = number % 16;
			number /= 16;
			result = HEX_LETTER.substring(n, n + 1) + result;
		}
		return result;
	}

	public static String makeTrip(long l) {
		l = Math.abs(l);

		final StringBuilder sb = new StringBuilder();
		l *= PRIME_NUMBER_BIG;
		for (int i = 0; i < 8; ++i) {
			final int index = (int) (l % TRIP_LETTERS.length());
			l /= TRIP_LETTERS.length();
			sb.append(TRIP_LETTERS.substring(index, index + 1));
		}
		return sb.toString();
	}

	public static String makeTrip(String string) {
		return makeTrip(string.hashCode());
	}

	public static String makeTrip(long userCode, String machineIp) {
		if (SharedData.get().isAdministoratorMode()) {
			return "◆" + userCode + "@" + machineIp;
		} else {
			return "◆" + Utility.makeTrip(userCode) + "@" + Utility.makeTrip(machineIp);
		}
	}

	@SuppressWarnings("deprecation")
	public static String toDateFormat(Date date) {
		// BugTrack-QMAClone/392 - QMAClone wiki
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F392
		StringBuilder sb = new StringBuilder();
		sb.append(padStart(valueOf(date.getYear() + 1900), 4, '0'));
		sb.append('/');
		sb.append(padStart(valueOf(date.getMonth() + 1), 2, '0'));
		sb.append('/');
		sb.append(padStart(valueOf(date.getDate()), 2, '0'));
		sb.append(' ');
		sb.append(padStart(valueOf(date.getHours()), 2, '0'));
		sb.append(':');
		sb.append(padStart(valueOf(date.getMinutes()), 2, '0'));
		sb.append(':');
		sb.append(padStart(valueOf(date.getSeconds()), 2, '0'));
		return sb.toString();
	}

	public static int countBits(int x) {
		x = (x & 0x55555555) + ((x >> 1) & 0x55555555);
		x = (x & 0x33333333) + ((x >> 2) & 0x33333333);
		x = (x & 0x0f0f0f0f) + ((x >> 4) & 0x0f0f0f0f);
		x = (x & 0x00ff00ff) + ((x >> 8) & 0x00ff00ff);
		x = (x & 0x0000ffff) + ((x >> 16) & 0x0000ffff);
		return x;
	}

	public static int numberOfTrainingZero(int x) {
		return 32 - numberOfLeadingZero(x);
	}

	public static int numberOfLeadingZero(int x) {
		int y;
		int n = 32;
		y = x >> 16;
		if (y != 0) {
			n = n - 16;
			x = y;
		}
		y = x >> 8;
		if (y != 0) {
			n = n - 8;
			x = y;
		}
		y = x >> 4;
		if (y != 0) {
			n = n - 4;
			x = y;
		}
		y = x >> 2;
		if (y != 0) {
			n = n - 2;
			x = y;
		}
		y = x >> 1;
		if (y != 0) {
			return n - 2;
		}
		return n - x;
	}
}
