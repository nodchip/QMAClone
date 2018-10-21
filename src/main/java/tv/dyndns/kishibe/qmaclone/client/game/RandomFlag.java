package tv.dyndns.kishibe.qmaclone.client.game;

import tv.dyndns.kishibe.qmaclone.client.util.HasIndex;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum RandomFlag implements IsSerializable, HasIndex {
	NotSelected(0, "指定なし"), Random1(1, "ランダム1"), Random2(2, "ランダム2"), Random3(3, "ランダム3"), Random4(
			4, "ランダム4"), Random5(5, "ランダム5");
	private final int index;
	private final String name;

	private RandomFlag(int index, String name) {
		this.index = index;
		this.name = name;
	}

	@Override
	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}
}
