package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.ProblemProcessable;
import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.common.base.Strings;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

@RunWith(JUnit4.class)
public class ValidatorStressTest {

	@Rule
	public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
	@Inject
	private Database database;

	@Ignore
	@Test
	public void test() throws Exception {
		database.processProblems(new ProblemProcessable() {

			@Override
			public void process(PacketProblem problem) throws Exception {
				if (Strings.isNullOrEmpty(problem.creator)) {
					problem.creator = "作成者";
				}
				assertEquals(problem.toString(), Collections.emptyList(), problem.validate());
			}
		});
	}
}
