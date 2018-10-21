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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.Utility;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

public class VoteManager {
	private static class Key {
		private final int userCode;
		private final int problemId;

		public Key(int userCode, int problemId) {
			this.userCode = userCode;
			this.problemId = problemId;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(userCode, problemId);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Key)) {
				return false;
			}

			final Key rh = (Key) obj;
			return userCode == rh.userCode && problemId == rh.problemId;
		}
	}

	private final Database database;
	private final RestrictedUserUtils restrictedUserUtils;
	private final Set<Key> userCodes = new HashSet<Key>();

	@Inject
	public VoteManager(Database database, ThreadPool threadPool,
			RestrictedUserUtils restrictedUserUtils) {
		this.database = Preconditions.checkNotNull(database);
		this.restrictedUserUtils = Preconditions.checkNotNull(restrictedUserUtils);
		threadPool.addHourTask(new Runnable() {
			public void run() {
				updateUserCodes();
			}
		});
	}

	public void vote(int userCode, int problemId, boolean good, String feedback, String playerName,
			String remoteAddress) throws DatabaseException {
		if (restrictedUserUtils.checkAndUpdateRestrictedUser(userCode, remoteAddress,
				RestrictionType.VOTE)) {
			return;
		}

		synchronized (userCodes) {
			Key key = new Key(userCode, problemId);
			if (userCodes.contains(key)) {
				return;
			}
			userCodes.add(key);

			// [良] 意見 (名前◆トリップ)
			String opinion = good ? "良" : "悪";
			String sentence = String.format("[%s] %s (%s◆%s)", opinion, feedback, playerName,
					Utility.makeTrip(userCode));

			database.voteToProblem(problemId, good, sentence);
		}
	}

	public void reset(int problemId) throws DatabaseException {
		List<PacketProblem> problems = database.getProblem(ImmutableList.of(problemId));
		PacketProblem problem = problems.get(0);
		problem.voteGood = problem.voteBad = 0;
		database.updateProblem(problem);
	}

	private void updateUserCodes() {
		synchronized (userCodes) {
			userCodes.clear();
		}
	}
}
