package tv.dyndns.kishibe.qmaclone.client.game.judge;

import java.util.Arrays;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

public class JudgeTestBase {
	protected PacketProblem problem;

	protected void setUp() throws Exception {
		problem = new PacketProblem();
	}

	protected String[] toArray(String... strings) {
		return Arrays.copyOf(strings, Constant.MAX_NUMBER_OF_ANSWERS);
	}
}
