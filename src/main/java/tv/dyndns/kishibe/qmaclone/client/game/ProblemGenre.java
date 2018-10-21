package tv.dyndns.kishibe.qmaclone.client.game;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.util.HasIndex;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.IsSerializable;

public enum ProblemGenre implements IsSerializable, HasIndex {
	Random(0, "ノンジャンル", "ノ", "gray"), Anige(1, "アニメ＆ゲーム", "ア", "blue"), Sports(2, "スポーツ", "ス",
			"red"), Geinou(3, "芸能", "芸", "green"), Zatsugaku(4, "雑学", "雑", "orange"), Gakumon(5,
			"学問", "学", "purple");

	private final int index;
	private final String name;
	private final String initial;
	private final String color;

	private ProblemGenre(int index, String name, String initial, String color) {
		this.index = index;
		this.name = name;
		this.initial = initial;
		this.color = color;
	}

	/**
	 * ジャンル名の頭文字を返す
	 * 
	 * @return　ジャンル名の頭文字
	 */
	public String getInitial() {
		return initial;
	}

	/**
	 * ジャンルのテーマカラーを返す
	 * 
	 * @return　ジャンルのテーマカラー
	 */
	public String getColor() {
		return color;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * ジャンルの集合をビットフラグへ変換する
	 * 
	 * @param genres
	 *            ジャンルの集合
	 * @return ビットフラグ
	 */
	public static int toBitFlag(Set<ProblemGenre> genres) {
		int bitFlag = 0;
		for (ProblemGenre genre : genres) {
			bitFlag |= (1 << genre.getIndex());
		}
		return bitFlag;
	}

	/**
	 * ビットフラグをジャンルの集合へ変換する
	 * 
	 * @param bitFlag
	 *            ビットフラグ
	 * @return ジャンルの集合
	 */
	public static Set<ProblemGenre> fromBitFlag(int bitFlag) {
		if (bitFlag == 0) {
			return Collections.emptySet();
		}

		List<ProblemGenre> genres = Lists.newArrayList();
		for (ProblemGenre genre : values()) {
			if ((bitFlag & (1 << genre.getIndex())) == 0) {
				continue;
			}
			genres.add(genre);
		}
		return Sets.newHashSet(genres);
	}

	/**
	 * 
	 * ジャンル名から{@link ProblemGenre}を返す
	 * 
	 * @param name
	 *            ジャンル名
	 * @return {@link ProblemGenre}
	 */
	public static ProblemGenre fromName(String name) {
		for (ProblemGenre genre : ProblemGenre.values()) {
			if (genre.name.equals(name)) {
				return genre;
			}
		}
		return null;
	}
}
