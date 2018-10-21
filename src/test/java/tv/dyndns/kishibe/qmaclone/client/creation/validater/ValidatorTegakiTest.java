package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import java.util.Arrays;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.user.client.Timer;

public class ValidatorTegakiTest extends QMACloneGWTTestCaseBase {
	private ValidatorTegaki validator;
	private PacketProblem problem;

	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		validator = new ValidatorTegaki();
		problem = new PacketProblem();
		problem.type = ProblemType.Tegaki;
		problem.answers = new String[] { "春", null, null, null, null, null, null, null };
		problem.sentence = "a";
		problem.creator = "a";

		ValidatorTegaki.AvailableCharacters.get();
	}

	@Test
	public void testCheckShouldReturnTrueIfValid() {
		new Timer() {
			@Override
			public void run() {
				assertEquals(Arrays.asList(), validator.check(problem).warn);
				finishTest();
			}
		}.schedule(100);
		delayTestFinish(500);
	}

	@Test
	public void testCheckShouldReturnFalseIfNoAnswer() {
		new Timer() {
			@Override
			public void run() {
				problem.answers = new String[] { null, null, null, null, null, null, null, null };
				assertEquals(Arrays.asList("解答を入力してください"), validator.check(problem).warn);
				finishTest();
			}
		}.schedule(100);
		delayTestFinish(500);
	}

	@Test
	public void testCheckShouldReturnFalseIfNotAvalidableCharacter() {
		new Timer() {
			@Override
			public void run() {
				problem.answers = new String[] { "A", null, null, null, null, null, null, null };
				assertEquals(Arrays.asList("1番目の解答1文字目「A」は使用できません。"), validator.check(problem).warn);
				finishTest();
			}
		}.schedule(100);
		delayTestFinish(500);
	}
}
