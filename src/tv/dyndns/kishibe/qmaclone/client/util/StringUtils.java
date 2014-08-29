package tv.dyndns.kishibe.qmaclone.client.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.dyndns.kishibe.qmaclone.client.game.LetterType;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class StringUtils {
	private static final String HALF_FULL_LETTER[] = { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
			"ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ０１２３４５６７８９" };
	private static final String[][] VOICED_SOUND_MARK = {
			{
					"かきくけこさしすせそたちつてとはひふへほがぎぐげござじずぜぞだぢづでどばびぶべぼっカキクケコサシスセソタチツテトハヒフヘホガギグゲゴザジズゼゾダヂヅデドバビブベボッっぱぴぷぺぽパピプペポウヴ",
					"がぎぐげござじずぜぞだぢづでどばびぶべぼかきくけこさしすせそたちつてとはひふへほづガギグゲゴザジズゼゾダヂヅデドバビブベボカキクケコサシスセソタチツテトハヒフヘホヅづばびぶべぼバビブベボヴウ" },
			{ "はひふへほぱぴぷぺぽハヒフヘホパピプペポばびぶべぼバビブベボ", "ぱぴぷぺぽはひふへほパピプペポハヒフヘホぱぴぷぺぽパピプペポ" } };
	private static final String[][] ALPHABET_TO_KANA = { { "Ａ", "あ", "ア" }, { "Ｉ", "い", "イ" },
			{ "Ｕ", "う", "ウ" }, { "Ｅ", "え", "エ" }, { "Ｏ", "お", "オ" }, { "LA", "ぁ", "ァ" },
			{ "LI", "ぃ", "ィ" }, { "LU", "ぅ", "ゥ" }, { "LE", "ぇ", "ェ" }, { "LO", "ぉ", "ォ" },
			{ "XA", "ぁ", "ァ" }, { "XI", "ぃ", "ィ" }, { "XU", "ぅ", "ゥ" }, { "XE", "ぇ", "ェ" },
			{ "XO", "ぉ", "ォ" }, { "KA", "か", "カ" }, { "KI", "き", "キ" }, { "KU", "く", "ク" },
			{ "KE", "け", "ケ" }, { "KO", "こ", "コ" }, { "SA", "さ", "サ" }, { "SI", "し", "シ" },
			{ "SU", "す", "ス" }, { "SE", "せ", "セ" }, { "SO", "そ", "ソ" }, { "TA", "た", "タ" },
			{ "TI", "ち", "チ" }, { "TU", "つ", "ツ" }, { "TE", "て", "テ" }, { "TO", "と", "ト" },
			{ "NA", "な", "ナ" }, { "NI", "に", "ニ" }, { "NU", "ぬ", "ヌ" }, { "NE", "ね", "ネ" },
			{ "NO", "の", "ノ" }, { "HA", "は", "ハ" }, { "HI", "ひ", "ヒ" }, { "HU", "ふ", "フ" },
			{ "HE", "へ", "ヘ" }, { "HO", "ほ", "ホ" }, { "MA", "ま", "マ" }, { "MI", "み", "ミ" },
			{ "MU", "む", "ム" }, { "ME", "め", "メ" }, { "MO", "も", "モ" }, { "YA", "や", "ヤ" },
			{ "YI", "い", "イ" }, { "YU", "ゆ", "ユ" }, { "YE", "いぇ", "イェ" }, { "YO", "よ", "ヨ" },
			{ "RA", "ら", "ラ" }, { "RI", "り", "リ" }, { "RU", "る", "ル" }, { "RE", "れ", "レ" },
			{ "RO", "ろ", "ロ" }, { "WA", "わ", "ワ" }, { "WI", "うぃ", "ウィ" }, { "WU", "う", "ウ" },
			{ "WE", "うぇ", "ウェ" }, { "WO", "を", "ヲ" }, { "VA", "ヴぁ", "ヴァ" }, { "VI", "ヴぃ", "ヴィ" },
			{ "VU", "ヴ", "ヴ" }, { "VE", "ヴぇ", "ヴェ" }, { "VO", "ヴぉ", "ヴォ" }, { "GA", "が", "ガ" },
			{ "GI", "ぎ", "ギ" }, { "GU", "ぐ", "グ" }, { "GE", "げ", "ゲ" }, { "GO", "ご", "ゴ" },
			{ "ZA", "ざ", "ザ" }, { "ZI", "じ", "ジ" }, { "ZU", "ず", "ズ" }, { "ZE", "ぜ", "ゼ" },
			{ "ZO", "ぞ", "ゾ" }, { "JA", "じゃ", "ジャ" }, { "JI", "じ", "ジ" }, { "JU", "じゅ", "ジュ" },
			{ "JE", "じぇ", "ジェ" }, { "JO", "じょ", "ジョ" }, { "DA", "だ", "ダ" }, { "DI", "ぢ", "ヂ" },
			{ "DU", "づ", "ヅ" }, { "DE", "で", "デ" }, { "DO", "ど", "ド" }, { "BA", "ば", "バ" },
			{ "BI", "び", "ビ" }, { "BU", "ぶ", "ブ" }, { "BE", "べ", "ベ" }, { "BO", "ぼ", "ボ" },
			{ "PA", "ぱ", "パ" }, { "PI", "ぴ", "ピ" }, { "PU", "ぷ", "プ" }, { "PE", "ぺ", "ペ" },
			{ "PO", "ぽ", "ポ" }, { "FA", "ふぁ", "ファ" }, { "FI", "ふぃ", "フィ" }, { "FU", "ふ", "フ" },
			{ "FE", "ふぇ", "フェ" }, { "FO", "ふぉ", "フォ" }, { "LYA", "ゃ", "ャ" }, { "LYI", "ぃ", "ィ" },
			{ "LYU", "ゅ", "ュ" }, { "LYE", "ぇ", "ェ" }, { "LYO", "ょ", "ョ" }, { "XYA", "ゃ", "ャ" },
			{ "XYI", "ぃ", "ィ" }, { "XYU", "ゅ", "ュ" }, { "XYE", "ぇ", "ェ" }, { "XYO", "ょ", "ョ" },
			{ "KYA", "きゃ", "キャ" }, { "KYI", "きぃ", "キィ" }, { "KYU", "きゅ", "キュ" },
			{ "KYE", "きぇ", "キェ" }, { "KYO", "きょ", "キョ" }, { "SYA", "しゃ", "シャ" },
			{ "SYI", "しぃ", "シィ" }, { "SYU", "しゅ", "シュ" }, { "SYE", "しぇ", "シェ" },
			{ "SYO", "しょ", "ショ" }, { "TYA", "ちゃ", "チャ" }, { "TYI", "ちぃ", "チィ" },
			{ "TYU", "ちゅ", "チュ" }, { "TYE", "ちぇ", "チェ" }, { "TYO", "ちょ", "チョ" },
			{ "CYA", "ちゃ", "チャ" }, { "CYI", "ちぃ", "チィ" }, { "CYU", "ちゅ", "チュ" },
			{ "CYE", "ちぇ", "チェ" }, { "CYO", "ちょ", "チョ" }, { "NYA", "にゃ", "ニャ" },
			{ "NYI", "にぃ", "ニィ" }, { "NYU", "にゅ", "ニュ" }, { "NYE", "にぇ", "ニェ" },
			{ "NYO", "にょ", "ニョ" }, { "HYA", "ひゃ", "ヒャ" }, { "HYI", "ひぃ", "ヒィ" },
			{ "HYU", "ひゅ", "ヒュ" }, { "HYE", "ひぇ", "ヒェ" }, { "HYO", "ひょ", "ヒョ" },
			{ "FYA", "ふゃ", "フャ" }, { "FYI", "ふぃ", "フィ" }, { "FYU", "ふゅ", "フュ" },
			{ "FYE", "ふぇ", "フェ" }, { "FYO", "ふょ", "フョ" }, { "MYA", "みゃ", "ミャ" },
			{ "MYI", "みぃ", "ミィ" }, { "MYU", "みゅ", "ミュ" }, { "MYE", "みぇ", "ミェ" },
			{ "MYO", "みょ", "ミョ" }, { "RYA", "りゃ", "リャ" }, { "RYI", "りぃ", "リィ" },
			{ "RYU", "りゅ", "リュ" }, { "RYE", "りぇ", "リェ" }, { "RYO", "りょ", "リョ" },
			{ "GYA", "ぎゃ", "ギャ" }, { "GYI", "ぎぃ", "ギィ" }, { "GYU", "ぎゅ", "ギュ" },
			{ "GYE", "ぎぇ", "ギェ" }, { "GYO", "ぎょ", "ギョ" }, { "ZYA", "じゃ", "ジャ" },
			{ "ZYI", "じぃ", "ジィ" }, { "ZYU", "じゅ", "ジュ" }, { "ZYE", "じぇ", "ジェ" },
			{ "ZYO", "じょ", "ジョ" }, { "JYA", "じゃ", "ジャ" }, { "JYI", "じぃ", "ジィ" },
			{ "JYU", "じゅ", "ジュ" }, { "JYE", "じぇ", "ジェ" }, { "JYO", "じょ", "ジョ" },
			{ "DYA", "ぢゃ", "ヂャ" }, { "DYI", "ぢぃ", "ヂィ" }, { "DYU", "ぢゅ", "ヂュ" },
			{ "DYE", "ぢぇ", "ヂェ" }, { "DYO", "ぢょ", "ヂョ" }, { "BYA", "びゃ", "ビャ" },
			{ "BYI", "びぃ", "ビィ" }, { "BYU", "びゅ", "ビュ" }, { "BYE", "びぇ", "ビェ" },
			{ "BYO", "びょ", "ビョ" }, { "PYA", "ぴゃ", "ピャ" }, { "PYI", "ぴぃ", "ピィ" },
			{ "PYU", "ぴゅ", "ピュ" }, { "PYE", "ぴぇ", "ピェ" }, { "PYO", "ぴょ", "ピョ" },
			{ "SHA", "しゃ", "シャ" }, { "SHI", "し", "シ" }, { "SHU", "しゅ", "シュ" },
			{ "SHE", "しぇ", "シェ" }, { "SHO", "しょ", "ショ" }, { "CHA", "ちゃ", "チャ" },
			{ "CHI", "ち", "チ" }, { "CHU", "ちゅ", "チュ" }, { "CHE", "ちぇ", "チェ" },
			{ "CHO", "ちょ", "チョ" }, { "THA", "てゃ", "テャ" }, { "THI", "てぃ", "ティ" },
			{ "THU", "てゅ", "テュ" }, { "THE", "てぇ", "テェ" }, { "THO", "てょ", "テョ" },
			{ "DHA", "でゃ", "デャ" }, { "DHI", "でぃ", "ディ" }, { "DHU", "でゅ", "デュ" },
			{ "DHE", "でぇ", "デェ" }, { "DHO", "でょ", "デョ" }, { "NN", "ん", "ン" },
			{ "TSA", "つぁ", "ツァ" }, { "TSI", "つぃ", "ツィ" }, { "TSU", "つ", "ツ" },
			{ "TSE", "つぇ", "ツェ" }, { "TSO", "つぉ", "ツォ" }, { "XTU", "っ", "ッ" },
			{ "LTSU", "っ", "ッ" }, { "LTU", "っ", "ッ" }, { "-", "ー", "ー" }, { "QQ", "っQ", "ッQ" },
			{ "RR", "っR", "ッR" }, { "TT", "っT", "ッT" }, { "YY", "っY", "ッY" }, { "PP", "っP", "ッP" },
			{ "SS", "っS", "ッS" }, { "DD", "っD", "ッD" }, { "FF", "っF", "ッF" }, { "GG", "っG", "ッG" },
			{ "HH", "っH", "ッH" }, { "JJ", "っJ", "ッJ" }, { "KK", "っK", "ッK" }, { "LL", "っL", "ッL" },
			{ "ZZ", "っZ", "ッZ" }, { "XX", "っX", "ッX" }, { "CC", "っC", "ッC" }, { "VV", "っV", "ッV" },
			{ "BB", "っB", "ッB" }, { "MM", "っM", "ッM" }, { "NQ", "んQ", "ンQ" }, { "NW", "んW", "ンW" },
			{ "NR", "んR", "ンR" }, { "NT", "んT", "ンT" }, { "NS", "んS", "ンS" }, { "ND", "んD", "ンD" },
			{ "NF", "んF", "ンF" }, { "NG", "んG", "ンG" }, { "NH", "んH", "ンH" }, { "NJ", "んJ", "ンJ" },
			{ "NK", "んK", "ンK" }, { "NL", "んL", "ンL" }, { "NZ", "んZ", "ンZ" }, { "NX", "んX", "ンX" },
			{ "NC", "んC", "ンC" }, { "NV", "んV", "ンV" }, { "NB", "んB", "ンB" }, { "NM", "んM", "ンM" },
			{ "NP", "んP", "ンP" }, { "WHI", "うぃ", "ウィ" }, { "WHE", "うぇ", "ウェ" },
			{ "TWU", "とぅ", "トゥ" }, { "DWU", "どぅ", "ドゥ" }, { "WHA", "うぁ", "ウァ" },
			{ "WHI", "うぃ", "ウィ" }, { "WHU", "う", "ウ" }, { "WHE", "うぇ", "ウェ" },
			{ "WHO", "うぉ", "ウォ" }, { "QA", "くぁ", "クァ" }, { "QI", "くぃ", "クィ" }, { "QU", "く", "ク" },
			{ "QE", "くぇ", "クェ" }, { "QO", "くぉ", "クォ" }, };

	private static final Map<Character, Character> HALF_TO_FULL = createHalfToFull();
	private static final Map<Character, Character> FULL_TO_HALF = createFullToHalf();
	private static final List<Map<String, String>> VOICED_SOUND_MARK_DICTIONARY = createVoiceSoundMarkDictionary();
	private static final List<Map<String, String>> ALPHABET_TO_KANA_DICTIONARY = createAlphabetToKanaDictionary();

	public enum VoicedSoundMark {
		Full, Half
	}

	private static Map<Character, Character> createHalfToFull() {
		Map<Character, Character> halfToFull = Maps.newHashMap();
		for (int i = 0; i < HALF_FULL_LETTER[0].length(); ++i) {
			halfToFull.put(HALF_FULL_LETTER[0].charAt(i), HALF_FULL_LETTER[1].charAt(i));
		}
		return halfToFull;
	}

	private static Map<Character, Character> createFullToHalf() {
		Map<Character, Character> fullToHalf = Maps.newHashMap();
		for (int i = 0; i < HALF_FULL_LETTER[0].length(); ++i) {
			fullToHalf.put(HALF_FULL_LETTER[1].charAt(i), HALF_FULL_LETTER[0].charAt(i));
		}
		return fullToHalf;
	}

	private static List<Map<String, String>> createVoiceSoundMarkDictionary() {
		List<Map<String, String>> dictionary = Lists.newArrayList();
		for (String[] rule : VOICED_SOUND_MARK) {
			String from = rule[0];
			String to = rule[1];
			Preconditions.checkState(from.length() == to.length());

			int numberOfLetters = rule[0].length();
			Map<String, String> m = Maps.newHashMap();
			for (int i = 0; i < numberOfLetters; ++i) {
				m.put(from.substring(i, i + 1), to.substring(i, i + 1));
			}
			dictionary.add(m);
		}

		return dictionary;
	}

	private static List<Map<String, String>> createAlphabetToKanaDictionary() {
		List<Map<String, String>> dictionary = Lists.newArrayList();
		dictionary.add(new HashMap<String, String>());
		dictionary.add(new HashMap<String, String>());
		for (String[] rule : ALPHABET_TO_KANA) {
			String from = toFullWidth(rule[0]);
			String hiragana = toFullWidth(rule[1]);
			String katakana = toFullWidth(rule[2]);
			dictionary.get(LetterType.Hiragana.ordinal()).put(from, hiragana);
			dictionary.get(LetterType.Katakana.ordinal()).put(from, katakana);
		}
		return dictionary;
	}

	private static String converWidth(String s, Map<Character, Character> dictionary) {
		if (Strings.isNullOrEmpty(s)) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (char ch : s.toCharArray()) {
			if (dictionary.containsKey(ch)) {
				ch = dictionary.get(ch);
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	/**
	 * 全角文字に変換する
	 * 
	 * @param s
	 *            変換対象の文字列
	 * @return 変換後の文字列
	 */
	public static String toFullWidth(String s) {
		return converWidth(s, HALF_TO_FULL);
	}

	/**
	 * 半角文字に変換する
	 * 
	 * @param s
	 *            変換対象の文字列
	 * @return 変換後の文字列
	 */
	public static String toHalfWidth(String s) {
		return converWidth(s, FULL_TO_HALF);
	}

	public static String removeLast(String s) {
		return s.isEmpty() ? "" : s.substring(0, s.length() - 1);
	}

	public static String switchVoicedSoundMarkOfLastLetter(String answer, VoicedSoundMark type) {
		if (answer.isEmpty()) {
			return "";
		}

		String lastLetter = answer.substring(answer.length() - 1);
		lastLetter = VOICED_SOUND_MARK_DICTIONARY.get(type.ordinal()).get(lastLetter);
		if (lastLetter != null) {
			answer = answer.substring(0, answer.length() - 1) + lastLetter;
		}
		return answer;
	}

	public static String convertLastAlphabetToKana(String answer, LetterType type) {
		// 最長一致
		Map<String, String> dictionary = ALPHABET_TO_KANA_DICTIONARY.get(type.ordinal());
		for (int length = Math.min(4, answer.length()); length > 0; --length) {
			String from = answer.substring(answer.length() - length, answer.length());
			if (!dictionary.containsKey(from)) {
				continue;
			}
			String to = dictionary.get(from);
			return answer.subSequence(0, answer.length() - length) + to;
		}
		return answer;
	}

	public static String convertLastAlphabetToKanaBeforeSendAnswer(String answer, LetterType type) {
		if (answer.endsWith("Ｎ")) {
			answer = answer.substring(0, answer.length() - 1);
			if (type == LetterType.Hiragana) {
				answer += "ん";
			} else {
				answer += "ン";
			}
		}
		return answer;
	}
}
