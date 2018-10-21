package tv.dyndns.kishibe.qmaclone.server.util;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class IntArray {
	private static final int INITIAL_SIZE = 16;
	private int[] data = new int[INITIAL_SIZE];
	private int last = 0;

	public synchronized void add(int element) {
		if (data.length == last) {
			data = Arrays.copyOf(data, data.length * 2);
		}
		data[last++] = element;
	}

	public synchronized int get(int index) {
		return data[index];
	}

	public synchronized void set(int index, int element) {
		data[index] = element;
	}

	public synchronized int size() {
		return last;
	}

	public synchronized int back() {
		return data[last - 1];
	}

	public synchronized void removeElementAndFillWithLastElement(int element) {
		for (int i = 0; i < last; ++i) {
			if (data[i] != element) {
				continue;
			}

			data[i] = back();
			--last;
		}
	}

	public synchronized int[] data() {
		return Arrays.copyOf(data, last);
	}

	public synchronized boolean isEmpty() {
		return last == 0;
	}

	public List<Integer> asList() {
		List<Integer> list = Lists.newArrayList();
		for (int element : data()) {
			list.add(element);
		}
		return list;
	}
}
