package tv.dyndns.kishibe.qmaclone.client.util;

public class Rand {

	private int x;

	public Rand(int seed) {
		this.x = seed;
	}

	public int get(int upper) {
		return Math.abs((x = ((x * 1103515245 + 12345) & 0x7fffffff)) >> 1) % upper;
	}

}
