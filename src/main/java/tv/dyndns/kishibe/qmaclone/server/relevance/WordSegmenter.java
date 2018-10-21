package tv.dyndns.kishibe.qmaclone.server.relevance;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class WordSegmenter {

	private final TrieCache trieCache;

	@Inject
	public WordSegmenter(TrieCache trieCache) {
		this.trieCache = Preconditions.checkNotNull(trieCache);
	}

	/**
	 * 文字列をパースし、含まれている単語を抽出する。vitabiアルゴリズムのようなもの
	 * 
	 * @param string
	 *            文字列
	 * @param words
	 *            含まれている単語のインデックス
	 */
	public void parse(String string, List<Integer> words, List<Integer> offsets,
			List<Integer> lengths) {
		trieCache.get().parse(string, words, offsets, lengths);
	}

}
