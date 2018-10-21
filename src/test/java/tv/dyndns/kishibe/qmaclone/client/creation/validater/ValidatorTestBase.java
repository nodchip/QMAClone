package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import java.util.Arrays;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

public class ValidatorTestBase {
	protected PacketProblem problem;

	protected void setUp() throws Exception {
		problem = new PacketProblem();
		problem.id = 12345;
		problem.genre = ProblemGenre.Anige;
		problem.type = ProblemType.Senmusubi;
		problem.good = 11;
		problem.bad = 22;
		problem.randomFlag = RandomFlag.Random1;
		problem.creatorHash = "作成者".hashCode();
		problem.userCode = 12345678;
		problem.sentence = "問題文";
		problem.answers = toArray("a", "b", "c", "d");
		problem.choices = toArray("A", "B", "C", "D");
		problem.creator = "作成者";
		problem.note = "ノート";
		problem.shuffledAnswers = new String[] { "d", "c", "b", "a" };
		problem.shuffledChoices = new String[] { "D", "C", "B", "A" };
		problem.imageAnswer = false;
		problem.imageChoice = false;
		problem.voteGood = 111;
		problem.voteBad = 222;
		problem.imageUrl = "http://www.google.com/image.jpg";
		problem.movieUrl = "http://www.youtube.com/watch?v=c_VCocI423c";
	}

	protected String[] toArray(String... strings) {
		return Arrays.copyOf(strings, Constant.MAX_NUMBER_OF_ANSWERS);
	}
}
