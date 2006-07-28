/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
