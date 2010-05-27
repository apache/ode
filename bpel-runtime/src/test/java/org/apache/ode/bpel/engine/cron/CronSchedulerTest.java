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
package org.apache.ode.bpel.engine.cron;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.xml.namespace.QName;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.ode.bpel.engine.Contexts;
import org.apache.ode.bpel.engine.cron.CronScheduler;
import org.apache.ode.bpel.engine.cron.RuntimeDataCleanupRunnable;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.bpel.iapi.ProcessConf.CleanupInfo;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.daohib.bpel.BpelDAOConnectionImpl;
import org.apache.ode.utils.CronExpression;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class CronSchedulerTest extends MockObjectTestCase {
    
    private Contexts contexts;
    private Mock scheduler;
    private CronScheduler cronScheduler;
    private ExecutorService execService;

    static {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.DEBUG);
        LogManager.getLogger(RuntimeDataCleanupRunnable.class).setLevel(Level.DEBUG);
        LogManager.getLogger(BpelDAOConnectionImpl.class).setLevel(Level.DEBUG);
        LogManager.getLogger("org.apache.ode").setLevel(Level.DEBUG);
    }
    
    protected void setUp() throws Exception {
        contexts = new Contexts();
        scheduler = mock(Scheduler.class);
        contexts.scheduler = (Scheduler)scheduler.proxy();
        
        cronScheduler = new CronScheduler();
        cronScheduler.setContexts(contexts);
        execService = Executors.newCachedThreadPool(new ThreadFactory() {
            int threadNumber = 0;
            public Thread newThread(Runnable r) {
                threadNumber += 1;
                Thread t = new Thread(r, "LongRunning-"+threadNumber);
                t.setDaemon(true);
                return t;
            }
        });
        cronScheduler.setScheduledTaskExec(execService);
    }
    
    private class NotifyingTerminationListener implements CronScheduler.TerminationListener {
        boolean finished = false;
        
        public synchronized void terminate() {
            finished = true;
            notify();
        }
    }
    
    public void testNull() throws Exception {}
    
    public void _testCleanup() throws Exception {
        CronExpression cronExpr = new CronExpression("* * * * * ?");
        RuntimeDataCleanupRunnable runnable = new RuntimeDataCleanupRunnable();
        
        JobDetails details = new JobDetails();
        details.setProcessId(new QName("test"));
        details.getDetailsExt().put("transactionSize", 10);
        CleanupInfo cleanupInfo = new CleanupInfo();
        cleanupInfo.getFilters().add("a=b");
        cleanupInfo.getCategories().add(CLEANUP_CATEGORY.CORRELATIONS);
        details.getDetailsExt().put("cleanupInfo", cleanupInfo);
        runnable.restoreFromDetails(details);
        runnable.setContexts(contexts);
        
        NotifyingTerminationListener listener = new NotifyingTerminationListener();
        cronScheduler.schedule(cronExpr, runnable, null, listener);
        while( !listener.finished ) {
            synchronized(listener) {
                listener.wait();
            }
        }
    }
}
