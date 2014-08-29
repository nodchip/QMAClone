package tv.dyndns.kishibe.qmaclone.server;

import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.util.IntArray;

public abstract class ProblemManager {
	private static final Logger logger = Logger.getLogger(ProblemManager.class.getName());
	protected static final int MAX_FIND_LOOP = 100;
	protected final Random random = new Random();
	private final Database database;

	protected ProblemManager(Database database) {
		this.database = database;
	}

	protected PacketProblemMinimum selectProblemFromList(IntArray problemIds,
			Set<Integer> selectedProblemIds, int classLevel, NewAndOldProblems newAndOldProblems,
			boolean tegaki, Set<Integer> userCodes, Set<Integer> createrHashes, boolean useNormalizedAccuracy) {
		if (problemIds == null || problemIds.isEmpty()) {
			return null;
		}

		// 更新により配列外アクセスとなる可能性があるため、問題選択できるまでループする
		int problemId = 0;
		do {
			try {
				problemId = problemIds.get(random.nextInt(problemIds.size()));
			} catch (IndexOutOfBoundsException e) {
			}
		} while (problemId == 0 && !problemIds.isEmpty());

		if (problemId == 0) {
			return null;
		}

		// 同じ問題が出題されないようにする
		if (selectedProblemIds.contains(problemId)) {
			return null;
		}

		PacketProblemMinimum problem;
		try {
			problem = database.getProblemMinimum(problemId);
		} catch (DatabaseException e) {
			logger.log(Level.WARNING, "問題の読み込みに失敗しました", e);
			return null;
		}

		// BugTrack-QMAClone/581 - QMAClone wiki
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F581
		// 指摘されている問題を出題しない
		if (problem.indication != null) {
			return null;
		}

		// 新問・集計中問題の取り扱い
		if (problem.isNew()) {
			if (classLevel < Constant.NUMBER_OF_CLASSES / 10) {
				// 初心者クラスでは新問は出題しない
				return null;
			}

			if (classLevel == Constant.CLASS_LEVEL_EASY) {
				// 難易度:易では出題しない
				return null;
			}

			if (newAndOldProblems == NewAndOldProblems.OnlyOld) {
				// 旧問のみの場合には出題しない
				return null;
			}
		} else {
			// 通常の問題の取り扱い
			double rate = useNormalizedAccuracy?problem.getNormalizedAccuracyRate():(problem.getAccuracyRate() * 0.01);
			double lowerBound = Constant.getAccuracyRateLowerBound(classLevel);
			double upperBound = Constant.getAccuracyRateUpperBound(classLevel);
			if (rate < lowerBound || upperBound < rate) {
				return null;
			}

			if (newAndOldProblems == NewAndOldProblems.OnlyNew) {
				// 新問のみの場合には出題しない
				return null;
			}
		}

		if (!tegaki && problem.type == ProblemType.Tegaki) {
			return null;
		}

		// すでに同じユーザーコードの作者による問題が出題されている場合は出題しない
		if (userCodes.contains(problem.userCode)) {
			return null;
		}

		// すでに同じ作者名の作者による問題が出題されている場合は出題しない
		if (createrHashes.contains(problem.creatorHash)) {
			return null;
		}

		return problem;
	}
}
