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
package tv.dyndns.kishibe.qmaclone.server.relevance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import tv.dyndns.kishibe.qmaclone.server.util.Normalizer;

import com.google.common.collect.Lists;

public class Trie {

	public interface Factory {
		Trie create();
	}

	private int[][] array;

	/**
	 * Trie木を作成する
	 * 
	 * @param words
	 *            　Trie木に挿入する単語
	 * @throws Exception
	 */
	public void build(Collection<String> words) {
		// Trie木に単語を挿入する
		List<Map<Integer, Integer>> trie = Lists.newArrayList();
		trie.add(new TreeMap<Integer, Integer>());
		int wordIndex = 0;
		for (String word : words) {
			addWord(word, trie, wordIndex++);
		}

		// Trie木を2次元配列に圧縮する
		// array[Trie木のノード番号][偶数:次の文字 奇数:次のノード番号]
		// 次の文字が0の場合は単語の終端を表す。次のノード番号には単語のインデックスが入る
		int[][] array = new int[trie.size()][];
		for (int nodeIndex = 0; nodeIndex < trie.size(); ++nodeIndex) {
			array[nodeIndex] = new int[trie.get(nodeIndex).size() * 2];
			int leafIndex = 0;
			for (Entry<Integer, Integer> entry : trie.get(nodeIndex).entrySet()) {
				array[nodeIndex][leafIndex++] = entry.getKey();
				array[nodeIndex][leafIndex++] = entry.getValue();
			}
		}

		this.array = array;
	}

	private static boolean isValidWord(String word) {
		int length = word.length();
		if (length <= 1) {
			return false;
		}

		// ○のみからなる文字列は無視
		boolean isCircles = true;
		for (int i = 0; i < length && isCircles; ++i) {
			isCircles = word.charAt(i) == '○';
		}
		if (isCircles) {
			return false;
		}

		return true;
	}

	/**
	 * Trie木に単語を挿入する
	 * 
	 * @param word
	 *            単語
	 * @param trie
	 *            Trie木
	 */
	private void addWord(String word, List<Map<Integer, Integer>> trie, int wordIndex) {
		if (!isValidWord(word)) {
			return;
		}

		word = Normalizer.normalize(word);

		int currentNode = 0;
		for (char c : word.toCharArray()) {
			if (trie.get(currentNode).containsKey(c & 0xffff)) {
				currentNode = trie.get(currentNode).get(c & 0xffff);
			} else {
				trie.get(currentNode).put(c & 0xffff, trie.size());
				currentNode = trie.size();
				trie.add(new TreeMap<Integer, Integer>());
			}
		}
		trie.get(currentNode).put(0, wordIndex);
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
		string = Normalizer.normalize(string);

		char[] s = string.toCharArray();
		int n = s.length;
		int[] memo = new int[n + 1];
		int[] edge = new int[n + 1];
		int[] wordIndexes = new int[n + 1];

		// 動的計画法
		for (int startIndex = 0; startIndex < n; ++startIndex) {
			// 一致する文字が無い場合は何もせずに1マス進む
			if (memo[startIndex + 1] < memo[startIndex]) {
				memo[startIndex + 1] = memo[startIndex];
				edge[startIndex + 1] = 0;
			}

			int nodeIndex = 0;
			for (int currentIndex = startIndex; currentIndex < n; ++currentIndex) {
				// 葉が無かったら
				if (array[nodeIndex].length == 0) {
					break;
				}

				// 終端文字が見つかったら
				if (array[nodeIndex][0] == 0 && currentIndex - startIndex != 1) {
					int cost = memo[startIndex] + (currentIndex - startIndex)
							* (currentIndex - startIndex);
					if (memo[currentIndex] < cost) {
						memo[currentIndex] = cost;
						edge[currentIndex] = currentIndex - startIndex;
						// 単語インデックスを記録する
						wordIndexes[currentIndex] = array[nodeIndex][1];
					}
				}

				// 次の葉に移動する
				int c = s[currentIndex] & 0xffff;
				int l = 0;
				int r = array[nodeIndex].length / 2;
				while (l + 1 < r) {
					int m = (l + r) >> 1;
					// lowerBound
					if (array[nodeIndex][m * 2] <= c) {
						l = m;
					} else {
						r = m;
					}
				}

				if (l == array[nodeIndex].length / 2 || array[nodeIndex][l * 2] != c) {
					break;
				}

				nodeIndex = array[nodeIndex][l * 2 + 1];
			}
		}

		// 動的計画法のトラックバック
		int currentIndex = n;
		while (currentIndex > 0) {
			if (edge[currentIndex] == 0) {
				currentIndex--;
			} else {
				if (words != null) {
					words.add(wordIndexes[currentIndex]);
				}
				if (lengths != null) {
					lengths.add(edge[currentIndex]);
				}
				currentIndex -= edge[currentIndex];
				if (offsets != null) {
					offsets.add(currentIndex);
				}
			}
		}
		if (words != null) {
			Collections.reverse(words);
		}
		if (offsets != null) {
			Collections.reverse(offsets);
		}
		if (lengths != null) {
			Collections.reverse(lengths);
		}
	}

	public void save(File file) throws FileNotFoundException, IOException {
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new DeflaterOutputStream(
				new BufferedOutputStream(new FileOutputStream(file))))) {
			outputStream.writeObject(array);
		}
	}

	public void load(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
		try (ObjectInputStream inputStream = new ObjectInputStream(new InflaterInputStream(
				new BufferedInputStream(new FileInputStream(file))))) {
			array = (int[][]) inputStream.readObject();
		}
	}
}
