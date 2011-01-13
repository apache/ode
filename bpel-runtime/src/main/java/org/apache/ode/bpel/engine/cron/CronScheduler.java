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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.engine.Contexts;
import org.apache.ode.bpel.engine.BpelServerImpl.ContextsAware;
import org.apache.ode.bpel.iapi.ClusterAware;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessConf.CronJob;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.bpel.iapi.Scheduler.MapSerializableRunnable;
import org.apache.ode.utils.CronExpression;

public class CronScheduler {
    static final Log __log = LogFactory.getLog(CronScheduler.class);

    // minimum interval of the cron job(1 second)
    private final long MIN_INTERVAL = 0;
    // if the work is schedule too late from the due time, skip it
    private final long TOLERABLE_SCHEDULE_DELAY = 0;

    private ExecutorService _scheduledTaskExec;

    private Contexts _contexts;

    private final Timer _schedulerTimer = new Timer("CronScheduler", true);

    private final Collection<TerminationListener> _systemTerminationListeners = new ArrayList<TerminationListener>();

    private final Map<QName, Collection<TerminationListener>> _terminationListenersByPid = new HashMap<QName, Collection<CronScheduler.TerminationListener>>();

    private volatile boolean _shuttingDown = false;

    public void setScheduledTaskExec(ExecutorService taskExec) {
        _scheduledTaskExec = taskExec;
    }

    public void setContexts(Contexts _contexts) {
        this._contexts = _contexts;
    }

    public void shutdown() {
        _shuttingDown = true;
        _schedulerTimer.cancel();

        for( TerminationListener listener : _systemTerminationListeners ) {
            listener.terminate();
        }
        _systemTerminationListeners.clear();

        for( Collection<TerminationListener> listeners : _terminationListenersByPid.values() ) {
            for( TerminationListener listener : listeners ) {
                listener.terminate();
            }
        }
        _terminationListenersByPid.clear();
    }

    public void cancelProcessCronJobs(QName pid, boolean undeployed) {
        assert pid != null;

        if( __log.isDebugEnabled() ) __log.debug("Cancelling PROCESS CRON jobs for: " + pid);
        Collection<TerminationListener> listenersToTerminate = new ArrayList<TerminationListener>();

        synchronized( _terminationListenersByPid ) {
            Collection<TerminationListener> listeners = _terminationListenersByPid.get(pid);
            if( listeners != null ) {
                listenersToTerminate.addAll(listeners);
                listeners.clear();
            }
            if( undeployed ) {
                _terminationListenersByPid.remove(pid);
            }
        }

        // terminate existing cron jobs if there are
        synchronized( pid ) {
            for( TerminationListener listener : listenersToTerminate ) {
                listener.terminate();
            }
        }
    }

    public void scheduleProcessCronJobs(QName pid, ProcessConf pconf) {
        if( _shuttingDown ) {
            return;
        }
        assert pid != null;

        cancelProcessCronJobs(pid, false);
        Collection<TerminationListener> newListeners = new ArrayList<TerminationListener>();

        synchronized( pid ) {
            if( __log.isDebugEnabled() ) __log.debug("Scheduling PROCESS CRON jobs for: " + pid);

            // start new cron jobs
            for( final CronJob job : pconf.getCronJobs() ) {
                if( __log.isDebugEnabled() ) __log.debug("Scheduling PROCESS CRON job: " + job.getCronExpression() + " for: " + pid);
                // for each different scheduled time
                Runnable runnable = new Runnable() {
                    public void run() {
                        if( __log.isDebugEnabled() ) __log.debug("Running cron cleanup with details list size: " + job.getRunnableDetailList().size());
                        for( JobDetails details : job.getRunnableDetailList() ) {
                            try {
                                // for each clean up for the scheduled time
                                RuntimeDataCleanupRunnable cleanup = new RuntimeDataCleanupRunnable();
                                cleanup.restoreFromDetails(details);
                                cleanup.setContexts(_contexts);
                                cleanup.run();
                                if( __log.isDebugEnabled() ) __log.debug("Finished running runtime data cleanup from a PROCESS CRON job: " + cleanup);
                            } catch(Exception re) {
                                __log.error("Error during runtime data cleanup from a PROCESS CRON: " + details + "; check your cron settings in deploy.xml.", re);
                                // don't sweat.. the rest of the system and other cron jobs still should work
                            }
                        }
                    }
                };
                newListeners.add(schedule(job.getCronExpression(), runnable, null, null));
            }
        }

        // make sure the pid does not get into the terminationListener map if no cron is setup
        if( !newListeners.isEmpty() ) {
            synchronized( _terminationListenersByPid ) {
                Collection<TerminationListener> oldListeners = _terminationListenersByPid.get(pid);
                if( oldListeners == null ) {
                    _terminationListenersByPid.put(pid, newListeners);
                } else {
                    oldListeners.addAll(newListeners);
                }
            }
        }
    }

    public void refreshSystemCronJobs(SystemSchedulesConfig systemSchedulesConf) {
        if( _shuttingDown ) {
            return;
        }

        synchronized( _systemTerminationListeners) {
            if( __log.isDebugEnabled() ) __log.debug("Refreshing SYSTEM CRON jobs.");

            try {
                // if error thrown on reading the schedules.xml, do not cancel existing cron jobs
                List<CronJob> systemCronJobs = systemSchedulesConf.getSystemCronJobs();

                // cancel cron jobs
                for( TerminationListener listener : _systemTerminationListeners ) {
                    listener.terminate();
                }
                _systemTerminationListeners.clear();

                // start new cron jobs
                for( final CronJob job : systemCronJobs ) {
                    if( __log.isDebugEnabled() ) __log.debug("Scheduling SYSTEM CRON job:" + job);
                    // for each different scheduled time
                    Runnable runnable = new Runnable() {
                        public void run() {
                            for( JobDetails details : job.getRunnableDetailList() ) {
                                try {
                                    // for now, we have only runtime data cleanup cron job defined
                                    // for each clean up for the scheduled time
                                    RuntimeDataCleanupRunnable cleanup = new RuntimeDataCleanupRunnable();
                                    synchronized( _terminationListenersByPid ) {
                                        if( !_terminationListenersByPid.isEmpty() ) {
                                            details.getDetailsExt().put("pidsToExclude", _terminationListenersByPid.keySet());
                                        }
                                    }
                                    cleanup.restoreFromDetails(details);
                                    cleanup.setContexts(_contexts);
                                    cleanup.run();
                                    if( __log.isDebugEnabled() ) __log.debug("Finished running runtime data cleanup from a SYSTEM CRON job:" + cleanup);
                                } catch( Exception e ) {
                                    __log.error("Error running a runtime data cleanup from a SYSTEM CRON job: " + details + "; check your system cron setup.", e);
                                }
                            }
                        }
                    };
                    _systemTerminationListeners.add(schedule(job.getCronExpression(), runnable, null, null));
                }
            } catch( Exception e ) {
                __log.error("Error during refreshing SYSTEM CRON schedules: ", e);
            }
        }
    }

    public TerminationListener schedule(final CronExpression cronExpression,
            final Runnable runnable, final JobDetails runnableDetails,
            TerminationListener terminationListener) {
        if( _shuttingDown ) {
            __log.info("CRON Scheduler is being shut down. This new scheduling request is ignored.");
            return new TerminationListener() {
                public void terminate() {
                    // do nothing
                }
            };
        }

        assert cronExpression != null;
        assert runnable != null;

        final Date nextScheduleTime = cronExpression.getNextValidTimeAfter(new Date(
                System.currentTimeMillis() + MIN_INTERVAL));
        final CronScheduledJob job = new CronScheduledJob(nextScheduleTime, runnable, runnableDetails, cronExpression, terminationListener);
        if( __log.isDebugEnabled() ) __log.debug("CRON will run in " + (nextScheduleTime.getTime() - System.currentTimeMillis()) + "ms.");

        try {
            _schedulerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (__log.isDebugEnabled()) {
                        __log.debug("Cron scheduling timer kicked in: " + cronExpression);
                    }
                    // run only if the current node is the coordinator,
                    // with the SimpleScheduler, the node is always the coordinator
                    if( !(_contexts.scheduler instanceof ClusterAware)
                            || ((ClusterAware)_contexts.scheduler).amICoordinator() ) {
                        // do not hold the timer thread too long, submit the work to an executorService
                        _scheduledTaskExec.submit(job);
                        if (__log.isDebugEnabled()) {
                            __log.debug("CRON job scheduled " + runnable);
                        }
                    }
                }
            }, nextScheduleTime);
        } catch( IllegalStateException ise ) {
            if( _shuttingDown ) {
                __log.info("CRON Scheduler is being shut down. This new scheduling request is ignored.");
            } else {
                throw ise;
            }
        }

        return job.terminationListener;
    }

    public interface TerminationListener {
        void terminate();
    }

    private class CronScheduledJob implements Callable<TerminationListener> {
        private volatile boolean terminated = false;
        private Date nextScheduleTime;
        private Runnable runnable;
        private JobDetails runnableDetails;
        private CronExpression cronExpression;
        private TerminationListener terminationListener;

        public CronScheduledJob(Date nextScheduleTime,
                Runnable runnable, JobDetails runnableDetails,
                CronExpression cronExpression, TerminationListener terminationListener) {
            this.nextScheduleTime = nextScheduleTime;
            this.runnable = runnable;
            this.runnableDetails = runnableDetails;
            this.cronExpression = cronExpression;
            if( terminationListener == null ) {
                terminationListener = new TerminationListener() {
                    public void terminate() {
                        terminated = true;
                    }
                };
            }
            this.terminationListener = terminationListener;
        }

        public TerminationListener call() throws Exception {
            try {
                if( TOLERABLE_SCHEDULE_DELAY == 0 ||
                    nextScheduleTime.getTime() < System.currentTimeMillis() + TOLERABLE_SCHEDULE_DELAY) {
                    if( runnableDetails != null &&
                            runnable instanceof MapSerializableRunnable ) {
                        ((MapSerializableRunnable)runnable).restoreFromDetails(runnableDetails);
                    }
                    if (runnable instanceof ContextsAware) {
                        ((ContextsAware) runnable).setContexts(_contexts);
                    }
                    if( !_shuttingDown && !terminated ) {
                        if (__log.isDebugEnabled()) {
                            __log.debug("Running CRON job: " + runnable + " for " + nextScheduleTime.getTime());
                        }
                        runnable.run();
                    }
                } else {
                    // ignore the scheduling.. it will be scheduled later
                }
            } catch( Exception e ) {
                if( _shuttingDown ) {
                    __log.info("A cron job threw an Exception during ODE shutdown: " + e.getMessage() + ", you can ignore the error.");
                } else if( e instanceof RuntimeException ) {
                    throw e;
                } else {
                    throw new RuntimeException("Exception during running cron scheduled job: " + runnable, e);
                }
            } finally {
                if( !_shuttingDown && !terminated ) {
                    schedule(cronExpression, runnable, runnableDetails, terminationListener);
                }
            }

            return terminationListener;
        }
    }
}
