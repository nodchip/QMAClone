package tv.dyndns.kishibe.qmaclone.server.util;

import java.text.Normalizer.Form;

public class Normalizer {
	public static String normalize(String s) {
		s = java.text.Normalizer.normalize(s, Form.NFKC);
		final char[] charArray = s.toCharArray();
		for (int i = 0; i < charArray.length; ++i) {
			int c = charArray[i];
			if (65281 <= c && c <= 65374) {
				c -= 65248;
			}
			charArray[i] = (char) c;
		}
		s = new String(charArray);
		s = s.toLowerCase();
		return s;
	}
}
