//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.lucene.util.NamedThreadFactory;

public class ThreadPool implements ScheduledExecutorService {
  private final ScheduledExecutorService scheduledExecutorService = Executors
      .newScheduledThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1),
          new NamedThreadFactory("ThreadPool-scheduled"));
  private final ExecutorService cachedExecutorService = Executors
      .newCachedThreadPool(new NamedThreadFactory("ThreadPool-cached"));

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return scheduledExecutorService.awaitTermination(timeout, unit)
        && cachedExecutorService.awaitTermination(timeout, unit);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    return cachedExecutorService.invokeAll(tasks);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
      TimeUnit unit) throws InterruptedException {
    return cachedExecutorService.invokeAll(tasks, timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    return cachedExecutorService.invokeAny(tasks);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return cachedExecutorService.invokeAny(tasks, timeout, unit);
  }

  @Override
  public boolean isShutdown() {
    return scheduledExecutorService.isShutdown() && cachedExecutorService.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return scheduledExecutorService.isTerminated() && cachedExecutorService.isTerminated();
  }

  @Override
  public void shutdown() {
    scheduledExecutorService.shutdown();
    cachedExecutorService.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    final List<Runnable> runnables = new ArrayList<Runnable>();
    runnables.addAll(scheduledExecutorService.shutdownNow());
    runnables.addAll(cachedExecutorService.shutdownNow());
    return runnables;
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return cachedExecutorService.submit(task);
  }

  @Override
  public Future<?> submit(Runnable task) {
    return cachedExecutorService.submit(task);
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return cachedExecutorService.submit(task, result);
  }

  @Override
  public void execute(Runnable command) {
    cachedExecutorService.execute(command);
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    return scheduledExecutorService.schedule(command, delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    return scheduledExecutorService.schedule(callable, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period,
      TimeUnit unit) {
    return scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
      TimeUnit unit) {
    return scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
  }

  public void addMinuteTasks(Runnable command) {
    scheduledExecutorService.scheduleWithFixedDelay(command, 1, 1, TimeUnit.MINUTES);
  }

  public void addHourTask(Runnable command) {
    scheduledExecutorService.scheduleWithFixedDelay(command, 1, 1, TimeUnit.HOURS);
  }

  public void addDailyTask(Runnable command) {
    scheduledExecutorService.scheduleWithFixedDelay(command, 1, 1, TimeUnit.DAYS);
  }

  public void addWeeklyTask(Runnable command) {
    scheduledExecutorService.scheduleWithFixedDelay(command, 7, 7, TimeUnit.DAYS);
  }
}
