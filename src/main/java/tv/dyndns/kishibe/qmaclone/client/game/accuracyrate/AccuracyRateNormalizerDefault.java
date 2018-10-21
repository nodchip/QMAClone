package tv.dyndns.kishibe.qmaclone.client.game.accuracyrate;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;

public class AccuracyRateNormalizerDefault implements AccuracyRateNormalizable {

	@Override
	public double normalize(PacketProblemMinimum problem) {
		int good = problem.good;
		int bad = problem.bad;
		if (good == 0 && bad == 0) {
			return -1;
		}
		return good / (double) (good + bad);
	}

}
