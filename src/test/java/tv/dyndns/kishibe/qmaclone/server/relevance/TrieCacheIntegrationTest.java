package tv.dyndns.kishibe.qmaclone.server.relevance;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.common.collect.Lists;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

public class TrieCacheIntegrationTest {

	private static final String TEST_DATA = "少年時代から無鉄砲な江戸っ子の坊っちゃんと、肉親から疎んじられる彼に無償の愛を注ぐ女中である清の描写から『坊っちゃん』の物語は幕を開く。"
			+ "坊っちゃんは両親と死別後、清とも離れ、四国の旧制中学校に数学の教師として赴任する。"
			+ "着任早々、校長には狸、教頭には赤シャツ、画学の教師には野だいこ、英語の教師にはうらなり、数学の主任教師には山嵐と、それぞれにあだ名を付けた。"
			+ "坊っちゃんは授業の時に生徒達から、てんぷらそばを四杯食べた件等の私事について執拗に冷やかされる。"
			+ "また初めての宿直の夜には、寄宿生達から蒲団の中に大量のバッタ（厳密にはイナゴ）を入れられる等の嫌がらせを受け、激怒して、何としても犯人を突き止めようとしたため、大事になってしまう。"
			+ "坊っちゃんは赤シャツとその腰巾着である野だいこから、生徒による嫌がらせは山嵐の扇動によるものであると婉曲的に吹き込まれ、一時は真に受けてしまう。"
			+ "しかし、後日の職員会議において、先の寄宿生の不祥事に坊っちゃんが毅然とした措置を主張したところ、狸をはじめとする事なかれ主義の職員達は取り合ってくれなかったのに対し、山嵐だけが坊っちゃんを支持してくれた。"
			+ "お互いに対する誤解は解けていき、坊っちゃんと山嵐とは、かえって強い友情で結ばれるようになる。"
			+ "うらなりには、マドンナとあだ名される婚約者がいたが、赤シャツがマドンナへの横恋慕から、お人好しのうらなりを体良く延岡に左遷したという事実を知り、坊っちゃんは義憤にかられる。"
			+ "実は山嵐も、赤シャツの横恋慕を糾弾したため、逆恨みされていたのであった。"
			+ "日露戦争の祝勝会の日に、坊っちゃんと山嵐は赤シャツの謀略により、中学校と師範学校の生徒同士の乱闘騒ぎに巻き込まれた上、いわれ無き生徒扇動の罪を着せられ、山嵐が辞職に追い込まれる。"
			+ "卑劣な仕打ちに憤激した坊っちゃんと山嵐は、赤シャツと野だいこの不祥事を暴くための監視を始め、ついに芸者遊び帰りの赤シャツと野だいこを取り押さえる。"
			+ "そして芸者遊びについて詰問するも、しらを切られたため、業を煮やし、激しく暴行を加えた。"
			+ "即刻辞職した坊っちゃんは、帰郷後、街鉄（現在の都電）の技手となって、再び、清と同居生活を始めるが、清が亡くなり、遺言通り小日向の養源寺に葬った事を記して、『坊っちゃん』の物語は幕を閉じる。";
	@Rule
	public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
	@Inject
	private TrieCache trieCache;

	@Test
	public void testParse() {
		List<Integer> extractedWordIndexes = Lists.newArrayList();
		trieCache.get().parse(TEST_DATA, extractedWordIndexes, null, null);
		assertThat(extractedWordIndexes, not(empty()));
	}

}
