package tv.dyndns.kishibe.qmaclone.client.util;

import java.util.Arrays;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CommandRunnerTest {
	private Runnable mockRunnable1;
	private Runnable mockRunnable2;
	private Runnable mockRunnable3;
	private CommandRunner runner;

	@Before
	public void setUp() throws Exception {
		mockRunnable1 = EasyMock.createMock(Runnable.class);
		mockRunnable2 = EasyMock.createMock(Runnable.class);
		mockRunnable3 = EasyMock.createMock(Runnable.class);
		runner = new CommandRunner(Arrays.asList(mockRunnable1, mockRunnable2, mockRunnable3));
	}

	@Test
	public void testRun() {
		mockRunnable1.run();
		mockRunnable2.run();
		mockRunnable3.run();

		EasyMock.replay(mockRunnable1, mockRunnable2, mockRunnable3);

		runner.run();
		runner.run();
		runner.run();

		EasyMock.verify(mockRunnable1, mockRunnable2, mockRunnable3);
	}
}
