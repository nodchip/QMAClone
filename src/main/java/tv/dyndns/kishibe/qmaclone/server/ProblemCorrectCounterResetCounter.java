package tv.dyndns.kishibe.qmaclone.server;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class ProblemCorrectCounterResetCounter implements Runnable {

	private static final int PROBLEM_CORRECT_COUNTER_LIMIT_PER_HOUR = 2;
	private final Multiset<Integer> userCodes = ConcurrentHashMultiset.create();

	@Override
	public void run() {
		userCodes.clear();
	}

	public boolean isAbleToReset(int userCode) {
		return userCodes.count(userCode) <= PROBLEM_CORRECT_COUNTER_LIMIT_PER_HOUR;
	}

	public void add(int userCode) {
		userCodes.add(userCode);
	}

}
