package tv.dyndns.kishibe.qmaclone.client.util;

public class Random {
	private static final Random INSTANCE = new Random();

	public static Random get() {
		return INSTANCE;
	}

	private long x = 123456789;
	private long y = 362436069;
	private long z = 521288629;
	private long w = System.currentTimeMillis();

	public int nextInt() {
		long t;
		t = 0xffffffff & (x ^ (x << 11));
		x = 0xffffffff & y;
		y = 0xffffffff & z;
		z = 0xffffffff & w;
		w = 0xffffffff & ((w ^ (w >> 19)) ^ (t ^ (t >> 8)));
		return (int) w;
	}

	public int nextInt(int n) {
		return Math.abs(nextInt() % n);
	}

	public boolean nextBoolean() {
		return nextInt(2) == 0;
	}

	public double nextDouble() {
		return nextInt(Integer.MAX_VALUE) / (double) Integer.MAX_VALUE;
	}

	public int[] makePermutationArray(int n) {
		if (n <= 0) {
			return new int[0];
		}
		int[] array = new int[n];
		for (int i = 0; i < n; ++i) {
			array[i] = i;
		}
		for (int i = 0; i < n; ++i) {
			int j = nextInt(n);
			int temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
		return array;
	}
}
