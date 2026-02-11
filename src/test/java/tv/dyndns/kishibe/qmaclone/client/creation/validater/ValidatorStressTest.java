package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.ProblemProcessable;
import tv.dyndns.kishibe.qmaclone.server.testing.GuiceInjectionExtension;

import com.google.common.base.Strings;
import com.google.inject.Inject;

@ExtendWith(GuiceInjectionExtension.class)
public class ValidatorStressTest {
	@Inject
	private Database database;

	@Disabled
	@Test
	public void test() throws Exception {
		database.processProblems(new ProblemProcessable() {

			@Override
			public void process(PacketProblem problem) throws Exception {
				if (Strings.isNullOrEmpty(problem.creator)) {
					problem.creator = "作成者";
				}
				assertEquals(Collections.emptyList(), problem.validate(), problem.toString());
			}
		});
	}
}

