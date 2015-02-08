package tv.dyndns.kishibe.qmaclone.client.util;

import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public class CommandRunnerTest {

  @Rule
  public final MockitoRule mocks = MockitoJUnit.rule();

  @Mock
  private Runnable mockRunnable1;
  @Mock
  private Runnable mockRunnable2;
  @Mock
  private Runnable mockRunnable3;

  private CommandRunner runner;

  @Before
  public void setUp() throws Exception {
    runner = new CommandRunner(Arrays.asList(mockRunnable1, mockRunnable2, mockRunnable3));
  }

  @Test
  public void testRun() {
    runner.run();
    runner.run();
    runner.run();

    verify(mockRunnable1).run();
    verify(mockRunnable2).run();
    verify(mockRunnable3).run();
  }
}
