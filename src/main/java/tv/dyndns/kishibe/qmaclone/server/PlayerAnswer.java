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
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class PlayerAnswer {
	private static final Logger logger = Logger.getLogger(PlayerAnswer.class.getName());
	private final Map<Integer, List<PacketWrongAnswer>> playerAnswers = Maps.newHashMap();
	private final Random random = new Random();

	public static interface Factory {
		PlayerAnswer create(List<Integer> problemIds);
	}

	@Inject
	public PlayerAnswer(Database database, @Assisted List<Integer> problemIds) {
		for (int problemID : problemIds) {
			try {
				playerAnswers.put(problemID, database.getPlayerAnswers(problemID));
			} catch (DatabaseException e) {
				logger.log(Level.WARNING, "プレイヤー解答の読み込みに失敗しました", e);
			}
		}
	}

	public synchronized String get(int problemID) {
		if (!playerAnswers.containsKey(problemID)) {
			return "これバグですから残念";
		}

		List<PacketWrongAnswer> wrongAnswers = playerAnswers.get(problemID);
		if (wrongAnswers.isEmpty()) {
			return null;
		}
		return wrongAnswers.get(random.nextInt(wrongAnswers.size())).answer;
	}
}

// CREATE TABLE player_answer (PROBLEM_ID INT NOT NULL REFERENCES PROBLEM(ID),
// ANSWER VARCHAR(255) NOT NULL, COUNT INT NOT NULL DEFAULT 1, PRIMARY KEY
// (PROBLEM_ID, ANSWER) ) CHARSET utf8;
