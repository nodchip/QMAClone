package tv.dyndns.kishibe.qmaclone.client.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.LetterType;
import tv.dyndns.kishibe.qmaclone.client.util.StringUtils.VoicedSoundMark;

/**
 * Test for {@link StringUtils}.
 * 
 * @author nodchip
 */
@RunWith(JUnit4.class)
public class StringUtilsTest {
	@Test
	public void testConvertToFullWidth() {
		assertEquals("０１２３４５６７８９", StringUtils.toFullWidth("0123456789"));
		assertEquals("ＱＷＥＲＴＹＵＩＯＰＡＳＤＦＧＨＪＫＬＺＸＣＶＢＮＭ",
				StringUtils.toFullWidth("QWERTYUIOPASDFGHJKLZXCVBNM"));
		assertEquals("ＱＷＥＲＴＹ", StringUtils.toFullWidth("ＱＷＥRTY"));
	}

	@Test
	public void testRemoveLast() {
		assertEquals("", StringUtils.removeLast(""));
		assertEquals("あいうえ", StringUtils.removeLast("あいうえお"));
	}

	@Test
	public void testSwitchVoicedSoundMarkOfLastLetter() {
		assertEquals("はばねろ",
				StringUtils.switchVoicedSoundMarkOfLastLetter("はばねろ", VoicedSoundMark.Full));
		assertEquals("はは",
				StringUtils.switchVoicedSoundMarkOfLastLetter("はば", VoicedSoundMark.Full));
		assertEquals("はば",
				StringUtils.switchVoicedSoundMarkOfLastLetter("はは", VoicedSoundMark.Full));
		assertEquals("ハバネロ",
				StringUtils.switchVoicedSoundMarkOfLastLetter("ハバネロ", VoicedSoundMark.Full));
		assertEquals("ハハ",
				StringUtils.switchVoicedSoundMarkOfLastLetter("ハバ", VoicedSoundMark.Full));
		assertEquals("ハバ",
				StringUtils.switchVoicedSoundMarkOfLastLetter("ハハ", VoicedSoundMark.Full));
		assertEquals("はばねろ",
				StringUtils.switchVoicedSoundMarkOfLastLetter("はばねろ", VoicedSoundMark.Half));
		assertEquals("ほほ",
				StringUtils.switchVoicedSoundMarkOfLastLetter("ほぽ", VoicedSoundMark.Half));
		assertEquals("ほぽ",
				StringUtils.switchVoicedSoundMarkOfLastLetter("ほほ", VoicedSoundMark.Half));
		assertEquals("ハバネロ",
				StringUtils.switchVoicedSoundMarkOfLastLetter("ハバネロ", VoicedSoundMark.Half));
		assertEquals("ハパ",
				StringUtils.switchVoicedSoundMarkOfLastLetter("ハバ", VoicedSoundMark.Half));
		assertEquals("ハパ",
				StringUtils.switchVoicedSoundMarkOfLastLetter("ハハ", VoicedSoundMark.Half));
		assertEquals("ハハ",
				StringUtils.switchVoicedSoundMarkOfLastLetter("ハパ", VoicedSoundMark.Half));
		assertEquals("", StringUtils.switchVoicedSoundMarkOfLastLetter("", VoicedSoundMark.Full));
		assertEquals("", StringUtils.switchVoicedSoundMarkOfLastLetter("", VoicedSoundMark.Half));
	}

	@Test
	public void testConvertLastAlphabetToKana() {
		assertEquals("はばねろ", StringUtils.convertLastAlphabetToKana("はばねＲＯ", LetterType.Hiragana));
		assertEquals("ハバネロ", StringUtils.convertLastAlphabetToKana("ハバネＲＯ", LetterType.Katakana));
		assertEquals("っっ", StringUtils.convertLastAlphabetToKana("っＬＴＳＵ", LetterType.Hiragana));
		assertEquals("ッッ", StringUtils.convertLastAlphabetToKana("ッＬＴＳＵ", LetterType.Katakana));
		assertEquals("あちょ", StringUtils.convertLastAlphabetToKana("あＴＹＯ", LetterType.Hiragana));
		assertEquals("アチョ", StringUtils.convertLastAlphabetToKana("アＴＹＯ", LetterType.Katakana));
		assertEquals("ほげ", StringUtils.convertLastAlphabetToKana("ほＧＥ", LetterType.Hiragana));
		assertEquals("ホゲ", StringUtils.convertLastAlphabetToKana("ホＧＥ", LetterType.Katakana));
		assertEquals("あい", StringUtils.convertLastAlphabetToKana("あＩ", LetterType.Hiragana));
		assertEquals("アイ", StringUtils.convertLastAlphabetToKana("アＩ", LetterType.Katakana));
		assertEquals("へー", StringUtils.convertLastAlphabetToKana("へ-", LetterType.Hiragana));
		assertEquals("ヘー", StringUtils.convertLastAlphabetToKana("ヘ-", LetterType.Katakana));
		assertEquals("たっＴ", StringUtils.convertLastAlphabetToKana("たＴＴ", LetterType.Hiragana));
		assertEquals("タッＴ", StringUtils.convertLastAlphabetToKana("タＴＴ", LetterType.Katakana));
		assertEquals("ふぁんＴ", StringUtils.convertLastAlphabetToKana("ふぁＮＴ", LetterType.Hiragana));
		assertEquals("ファンＴ", StringUtils.convertLastAlphabetToKana("ファＮＴ", LetterType.Katakana));
		assertEquals("かんＰ", StringUtils.convertLastAlphabetToKana("かＮＰ", LetterType.Hiragana));
		assertEquals("カンＰ", StringUtils.convertLastAlphabetToKana("カＮＰ", LetterType.Katakana));
		assertEquals("っ", StringUtils.convertLastAlphabetToKana("ＸＴＵ", LetterType.Hiragana));
		assertEquals("ッ", StringUtils.convertLastAlphabetToKana("ＸＴＵ", LetterType.Katakana));
		assertEquals("っ", StringUtils.convertLastAlphabetToKana("ＬＴＵ", LetterType.Hiragana));
		assertEquals("ッ", StringUtils.convertLastAlphabetToKana("ＬＴＵ", LetterType.Katakana));
		assertEquals("うぃ", StringUtils.convertLastAlphabetToKana("ＷＨＩ", LetterType.Hiragana));
		assertEquals("ウィ", StringUtils.convertLastAlphabetToKana("ＷＨＩ", LetterType.Katakana));
		assertEquals("うぇ", StringUtils.convertLastAlphabetToKana("ＷＨＥ", LetterType.Hiragana));
		assertEquals("ウェ", StringUtils.convertLastAlphabetToKana("ＷＨＥ", LetterType.Katakana));
		assertEquals("とぅ", StringUtils.convertLastAlphabetToKana("ＴＷＵ", LetterType.Hiragana));
		assertEquals("トゥ", StringUtils.convertLastAlphabetToKana("ＴＷＵ", LetterType.Katakana));
		assertEquals("どぅ", StringUtils.convertLastAlphabetToKana("ＤＷＵ", LetterType.Hiragana));
		assertEquals("ドゥ", StringUtils.convertLastAlphabetToKana("ＤＷＵ", LetterType.Katakana));
		assertEquals("くぉ", StringUtils.convertLastAlphabetToKana("ＱＯ", LetterType.Hiragana));
		assertEquals("クォ", StringUtils.convertLastAlphabetToKana("ＱＯ", LetterType.Katakana));
	}

	@Test
	public void convertLastAlphabetToKanaBeforeSendAnswerShouldWork() {
		assertEquals("おればなな",
				StringUtils.convertLastAlphabetToKanaBeforeSendAnswer("おればなな", LetterType.Hiragana));
		assertEquals("しんかん",
				StringUtils.convertLastAlphabetToKanaBeforeSendAnswer("しんかＮ", LetterType.Hiragana));
		assertEquals("シンカン",
				StringUtils.convertLastAlphabetToKanaBeforeSendAnswer("シンカＮ", LetterType.Katakana));
	}

	@Test
	public void toFullWidthShouldWork() {
		assertEquals("ＡＢＣ１２３", StringUtils.toFullWidth("ABC123"));
	}

	@Test
	public void toHalfWidthShouldWork() {
		assertEquals("ABC123", StringUtils.toHalfWidth("ＡＢＣ１２３"));
	}
}
