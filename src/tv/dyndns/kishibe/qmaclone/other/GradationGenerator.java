package tv.dyndns.kishibe.qmaclone.other;

import tv.dyndns.kishibe.qmaclone.client.Utility;

public class GradationGenerator {
	public static void main(String[] args) {
		for (int ratio = 0; ratio <= 100; ++ratio) {
			final String color = Utility.createBackgroundColorString(ratio / 100.0);
			System.out.printf("tr.accuracyRate%d td {background:%s;}\n", ratio, color);
		}
		System.out.printf("tr.newProblem td {background:#FFFFFF;}\n");
	}
}
