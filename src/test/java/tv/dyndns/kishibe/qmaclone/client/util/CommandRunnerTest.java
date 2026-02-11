package tv.dyndns.kishibe.qmaclone.client.util;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandRunnerTest {
  private final AtomicInteger count1 = new AtomicInteger();
  private final AtomicInteger count2 = new AtomicInteger();
  private final AtomicInteger count3 = new AtomicInteger();
  private CommandRunner runner;

  @BeforeEach
  public void setUp() {
    runner = new CommandRunner(Arrays.asList(count1::incrementAndGet, count2::incrementAndGet, count3::incrementAndGet));
  }

  @AfterEach
  public void tearDown() {
    count1.set(0);
    count2.set(0);
    count3.set(0);
  }

  @Test
  public void testRun() {
    runner.run();
    runner.run();
    runner.run();

    org.junit.jupiter.api.Assertions.assertEquals(1, count1.get());
    org.junit.jupiter.api.Assertions.assertEquals(1, count2.get());
    org.junit.jupiter.api.Assertions.assertEquals(1, count3.get());
  }
}
