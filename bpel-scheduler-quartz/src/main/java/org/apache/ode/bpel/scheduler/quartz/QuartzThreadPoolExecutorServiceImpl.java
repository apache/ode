package org.apache.ode.bpel.scheduler.quartz;

import java.util.concurrent.ExecutorService;

import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

public class QuartzThreadPoolExecutorServiceImpl implements ThreadPool {
  private ExecutorService _executorService;
  private int _numThreads;

  QuartzThreadPoolExecutorServiceImpl(ExecutorService executor, int numThreads) {
    _executorService = executor;
    _numThreads = numThreads;
  }
  
  public boolean runInThread(Runnable runnable) {
    _executorService.execute(runnable);
    return true;
  }

  public void initialize() throws SchedulerConfigException {
   
  }

  public void shutdown(boolean waitForJobsToComplete) {
    if (waitForJobsToComplete) _executorService.shutdown();
    else _executorService.shutdownNow();
  }

  public int getPoolSize() {
    return _numThreads;
  }
  

}
