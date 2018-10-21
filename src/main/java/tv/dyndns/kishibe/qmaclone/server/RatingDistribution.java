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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRatingDistribution;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.inject.Inject;

public class RatingDistribution {
	private static final Logger logger = Logger.getLogger(RatingDistribution.class.getName());
	private final Database database;
	private volatile PacketRatingDistribution distribution;
	private final Runnable runnableUpdate = new Runnable() {
		public void run() {
			try {
				update();
			} catch (DatabaseException e) {
				logger.log(Level.WARNING, "レーティング分布の更新に失敗しました", e);
			}
		}
	};

	@Inject
	public RatingDistribution(Database database, ThreadPool threadPool) {
		this.database = database;
		try {
			update();
		} catch (DatabaseException e) {
			logger.log(Level.WARNING, "レーティング分布の更新に失敗しました", e);
		}
		threadPool.addHourTask(runnableUpdate);
	}

	public PacketRatingDistribution get() {
		return distribution;
	}

	private void update() throws DatabaseException {
		List<Integer> ratings = database.getWholeRating();
		int minRating = Integer.MAX_VALUE;
		int maxRating = Integer.MIN_VALUE;
		for (int rating : ratings) {
			if (minRating > rating) {
				minRating = rating;
			}
			if (maxRating < rating) {
				maxRating = rating;
			}
		}
		int width = Constant.RATING_DISTRIBUTION_WIDTH;
		int[] d = new int[maxRating / width - minRating / width + 1];
		for (int rating : ratings) {
			++d[rating / width - minRating / width];
		}

		for (int i = 0; i < d.length; ++i) {
			d[i] += 1;
		}

		PacketRatingDistribution distribution = new PacketRatingDistribution();
		distribution.min = minRating;
		distribution.max = maxRating;
		distribution.distribution = d;
		this.distribution = distribution;
	}
}
