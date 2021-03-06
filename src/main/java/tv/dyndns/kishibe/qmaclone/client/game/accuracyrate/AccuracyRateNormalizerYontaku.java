package tv.dyndns.kishibe.qmaclone.client.game.accuracyrate;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;

public class AccuracyRateNormalizerYontaku implements AccuracyRateNormalizable {

	@Override
	public double normalize(PacketProblemMinimum problem) {
		int good = problem.good;
		int bad = problem.bad;
		if (good == 0 && bad == 0) {
			return -1;
		}
		double rate = good / (double) (good + bad);
		// 25% -> 0%
		rate -= 0.25;
		rate *= 1.0 / 0.75;
		rate = Math.max(0.0, rate);
		rate = Math.min(1.0, rate);
		return rate;
	}

}
