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
package tv.dyndns.kishibe.qmaclone.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.inject.Inject;

public class PrefectureRanking {
	private static final Logger logger = Logger.getLogger(PrefectureRanking.class.getName());
	private final Database database;
	private final Runnable runnableUpdate = new Runnable() {
		public void run() {
			try {
				update();
			} catch (DatabaseException e) {
				logger.log(Level.WARNING, "都道府県ランキングの更新に失敗しました", e);
			}
		}
	};
	private volatile int[][] ranking;
	private Object lock = new Object();

	@Inject
	private PrefectureRanking(Database database, ThreadPool threadPool) {
		this.database = database;
		try {
			update();
		} catch (DatabaseException e) {
			logger.log(Level.WARNING, "都道府県県ランキングの更新に失敗しました", e);
		}
		threadPool.addHourTask(runnableUpdate);
	}

	public int[][] get() {
		return ranking;
	}

	private void update() throws DatabaseException {
		Map<Integer, List<Integer>> data = database.getRatingGroupedByPrefecture();
		List<int[]> list = new ArrayList<int[]>();

		for (Entry<Integer, List<Integer>> entry : data.entrySet()) {
			int prefecture = entry.getKey();
			List<Integer> ratings = entry.getValue();
			Collections.sort(ratings, new Comparator<Integer>() {
				public int compare(Integer o1, Integer o2) {
					return o2 - o1;
				}
			});

			// http://www.topcoder.com/tc?module=Static&d1=statistics&d2=info&d3=topSchools
			int m = ratings.size();
			double r = 0.87;
			double average = 0.0;
			for (int i = 0; i < m; ++i) {
				average += (double) ratings.get(i) * Math.pow(r, i);
			}
			average *= (1.0 - r) / (1.0 - Math.pow(r, m));

			list.add(new int[] { prefecture, (int) average });
		}

		Collections.sort(list, new Comparator<int[]>() {
			public int compare(int[] o1, int[] o2) {
				return o2[1] - o1[1];
			}
		});

		synchronized (lock) {
			ranking = list.toArray(new int[0][]);
		}
	}
}
