package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

public class ShufflerTestBase {
	protected PacketProblem problem;

	protected void setUp() {
		problem = new PacketProblem();
	}

	protected String[] toArray(String... strings) {
		return strings;
	}

	protected int[] toArray(int... is) {
		return is;
	}
}
