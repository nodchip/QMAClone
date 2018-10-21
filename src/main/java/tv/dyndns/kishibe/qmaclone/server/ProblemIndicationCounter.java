package tv.dyndns.kishibe.qmaclone.server;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class ProblemIndicationCounter implements Runnable {

	private static final int PROBLEM_INDICATOR_LIMIT_PER_HOUR = 2;
	private final Multiset<Integer> userCodes = ConcurrentHashMultiset.create();

	@Override
	public void run() {
		userCodes.clear();
	}

	public boolean isAbleToIndicate(int userCode) {
		return userCodes.count(userCode) <= PROBLEM_INDICATOR_LIMIT_PER_HOUR;
	}

	public void add(int userCode) {
		userCodes.add(userCode);
	}

}
