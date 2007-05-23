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

import java.sql.Connection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.utils.GUID;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.core.QuartzScheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.core.SchedulingContext;
import org.quartz.impl.SchedulerRepository;
import org.quartz.impl.StdScheduler;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.ThreadPool;
import org.quartz.utils.DBConnectionManager;

/**
 * Quartz-based scheduler.
 *
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class QuartzSchedulerImpl implements Scheduler {

    protected final Log __log = LogFactory.getLog(getClass());

    private static final Map<String, QuartzSchedulerImpl> __instanceMap = Collections
            .synchronizedMap(new HashMap<String, QuartzSchedulerImpl>());

    protected volatile JobProcessor _processor;

    private org.quartz.Scheduler _quartz;

    private final String _id;

    private ExecutorService _executorSvc;

    private int _threads;

    private DataSource _managedDS;

    private TransactionManager _txm;

    private boolean _isSqlServer = false;

    private boolean _isClustered = false;

    public QuartzSchedulerImpl() {
        _id = "ODE";
    }

    public void setJobProcessor(JobProcessor processor) {
        _processor = processor;
    }

    public void setExecutorService(ExecutorService es, int threads) {
        _executorSvc = es;
        _threads = threads;
    }

    public void setDataSource(DataSource managedDs) {
        _managedDS = managedDs;
    }

    public void setTransactionManager(TransactionManager txm) {
        _txm = txm;
    }

    public void setIsClustered(boolean isclustered) {
        _isClustered = isclustered;
    }

    public void init() throws ContextException {
        if (_executorSvc == null)
            throw new NullPointerException("ExecutorService not set!");
        if (_managedDS == null)
            throw new NullPointerException("DataSource name not set!");
        if (_txm == null)
            throw new NullPointerException("TransactionManager not set!");

        DBConnectionManager.getInstance().addConnectionProvider("managed", new DataSourceConnectionProvider(_managedDS));
        JobStoreJTA jobStore = new JobStoreJTA(_txm);
        jobStore.setDataSource("managed");
        jobStore.setIsClustered(_isClustered);

        checkSqlServer();

        try {
            _quartz = createScheduler(_id, new GUID().toString(), new QuartzThreadPoolExecutorServiceImpl(_executorSvc, _threads),
                    jobStore);
            _quartz.getSchedulerInstanceId();
            __instanceMap.put(_id, this);
        } catch (Exception ex) {
            String emsg = "Error intitializing scheduler.";
            __log.error(emsg,ex);
            throw new ContextException(emsg, ex);
        }
    }

    /**
     * Check to see if the database is SQL server.
     */
    private void checkSqlServer() {
        Connection conn = null;
        try {
            conn = _managedDS.getConnection();
            String dbname = conn.getMetaData().getDatabaseProductName().toLowerCase();
            _isSqlServer = dbname.contains("sqlserver") || dbname.contains(" sql server") || dbname.contains("microsoft sql");
        } catch (Exception ex) {
            throw new ContextException("Error connecting to the database.", ex);
        } finally {
            try {
                conn.close();
            } catch (Exception ex) {
                ;
            }
        }

    }

    public void start() {
        if (_quartz == null)
            throw new IllegalStateException("init() not called!");

        try {
            _quartz.start();
        } catch (SchedulerException e) {
            throw new ContextException("Error starting Quartz.", e);
        }
    }

    public void shutdown() {
        try {
            _quartz.shutdown();
            SchedulerRepository.getInstance().remove(_id);
        } catch (Exception except) {
            throw new RuntimeException(except);
        } finally {
            __instanceMap.remove(_id);
        }
    }

    public void stop() {
        try {
            _quartz.standby();
        } catch (SchedulerException e) {
            throw new ContextException("Error stopping Quartz.", e);
        }

    }

    public String schedulePersistedJob(Map<String, Object> detail, Date when) throws ContextException {
        return schedule(detail, when, false, false);
    }

    protected String schedule(Map<String, Object> detail, Date when, boolean volatil, boolean notx) {
        if (when == null)
            when = new Date();
        JobDetail jobDetail = new JobDetail(new GUID().toString(), null, JobImpl.class);
        HashMap<String, Object> mcopy = new HashMap<String, Object>(detail);
        mcopy.put("__scheduler", _id);

        JobDataMap jdm = new JobDataMap(mcopy);
        jobDetail.setJobDataMap(jdm);
        jobDetail.setDurability(false);
        jobDetail.setVolatility(volatil);
        jobDetail.setRequestsRecovery(true);
        Trigger trigger = new SimpleTrigger(jobDetail.getName() + ".trigger", org.quartz.Scheduler.DEFAULT_GROUP, when, null, 0, 0L);
        trigger.setVolatility(volatil);

        try {
            _quartz.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            String errmsg = "Quartz failure in schedulePersistentJob";
            __log.error(errmsg, e);
            throw new ContextException(errmsg, e);
        }
        return jobDetail.getName();
    }

    public String scheduleVolatileJob(final boolean transacted, final Map<String, Object> detail, Date when ) throws ContextException {       throw new RuntimeException("Not implemented");
    }

    public String scheduleVolatileJob(final boolean transacted, final Map<String, Object> detail) throws ContextException {
        registerSynchronizer(new Synchronizer() {
            public void afterCompletion(boolean success) {
                try {
                    _executorSvc.submit(new Runnable() {
                        public void run() {
                            try {
                    if (transacted) {
                                    execTransaction(new Callable() {
                            public Object call() throws Exception {
                                JobInfo ji = new JobInfo("volatileJob", detail, 0);
                                doExecute(ji);
                                return null;
                            }
                        });
                    } else {
                        JobInfo ji = new JobInfo("volatileJob", detail, 0);
                        doExecute(ji);
                    }
                            } catch (Throwable t) {
                                __log.error("Error while executing volatile job: "+detail, t);
                            }
                        }
                    });
                } catch (Throwable t) {
                    __log.error("Failure when starting a new volatile job: "+detail, t);
                }
            }
            public void beforeCompletion() { }
        });
        return null;
    }

    public void cancelJob(String jobId) throws ContextException {
        try {
            _quartz.deleteJob(jobId, jobId + ".trigger");
        } catch (SchedulerException e) {
            String errmsg = "Quartz failure in cancelJob";
            __log.error(errmsg, e);
            throw new ContextException(errmsg, e);
        }
    }

    public <T> T execTransaction(Callable<T> transaction) throws Exception, ContextException {

        try {
            if (__log.isDebugEnabled())
                __log.debug("Starting transaction.");
            begin();
        } catch (Exception ex) {
            String errmsg = "Failed to start transaction.";
            __log.error(errmsg, ex);
            throw new ContextException(errmsg, ex);
        }

        boolean success = false;
        try {
            T retval = transaction.call();
            success = true;
            return retval;
        } finally {
            if (success)
                try {
                    if (__log.isDebugEnabled())
                        __log.debug("Commiting transaction.");
                    commit();
                } catch (Exception ex) {
                    String errmsg = "Failed to commit transaction.";
                    __log.error(errmsg, ex);
                    throw new ContextException(errmsg, ex);
                }
            else
                try {
                    rollback();
                } catch (Exception ex) {
                    String errmsg = "Failed to rollback transaction.";
                    __log.error(errmsg, ex);
                    throw new ContextException(errmsg, ex);
                }
        }
    }

    public <T> Future<T> execIsolatedTransaction(final Callable<T> transaction) throws Exception, ContextException {
        return _executorSvc.submit(new Callable<T>() {
            public T call() throws Exception {
                return execTransaction(transaction);
            }
        });
    }

    public boolean isTransacted() {
        try {
            return _txm.getStatus() != Status.STATUS_NO_TRANSACTION;
        } catch (SystemException e) {
            String errmsg = "Failed to get transaction status.";
            __log.error(errmsg, e);
            throw new ContextException(errmsg, e);
        }
    }

    protected void rollback() throws Exception {
        try {
            _txm.rollback();
        } catch (Exception ex) {
            __log.error("JTA ROLLBACK FAILED", ex);
            throw ex;
        }
    }

    protected void commit() throws Exception {
        try {
            _txm.commit();
        } catch (Exception ex) {
            __log.error("JTA COMMIT FAILED", ex);
            throw ex;
        }
    }

    protected void begin() throws Exception {
        try {
            _txm.begin();
        } catch (Exception ex) {
            __log.error("JTA BEGIN FAILED", ex);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private void doExecute(JobInfo ji) throws JobExecutionException {
        JobProcessor processor = _processor;
        if (processor == null)
            throw new JobExecutionException("No processor.", null, true);
        try {
            processor.onScheduledJob(ji);
        } catch (JobProcessorException jpe) {
            throw new JobExecutionException(jpe, jpe.retry);
        } catch (RuntimeException ex) {
            __log.error("Scheduled transaction failed unexpectedly: transaction will not be retried!.", ex);
            throw new JobExecutionException(ex, true);
        } catch (Throwable t) {
            __log.fatal("Scheduled transaction failed unexpectedly: transaction will not be retried!.", t);
            throw new JobExecutionException(false);
        }
    }

    public static void execute(JobExecutionContext jobcontext) throws JobExecutionException {
        String schedulerGuid = jobcontext.getJobDetail().getJobDataMap().getString("__scheduler");
        JobInfo ji = new JobInfo(jobcontext.getJobDetail().getName(), jobcontext.getJobDetail().getJobDataMap(),
                jobcontext.getRefireCount());
        __instanceMap.get(schedulerGuid).doExecute(ji);
    }

    /**
     * Create a QUARTZ scheduler using JTA Job Shell. Unfortunately there is no "easy" way to do this using the standard scheduler
     * factory.
     */
    private org.quartz.Scheduler createScheduler(String schedulerName, String schedulerInstanceId, ThreadPool threadPool,
            JobStoreJTA jobStore) throws SchedulerException {

        jobStore.setInstanceName(schedulerName);
        jobStore.setInstanceId(schedulerInstanceId);
        if (_isSqlServer)
            jobStore.setSelectWithLockSQL("SELECT * FROM {0}LOCKS UPDLOCK WHERE LOCK_NAME = ?");

        JTAJobRunShellFactory jrsf = new JTAJobRunShellFactory(_txm);

        SchedulingContext schedCtxt = new SchedulingContext();
        schedCtxt.setInstanceId(schedulerInstanceId);

        QuartzSchedulerResources qrs = new QuartzSchedulerResources();

        qrs.setName(schedulerName);
        qrs.setInstanceId(schedulerInstanceId);
        qrs.setJobRunShellFactory(jrsf);
        qrs.setThreadPool(threadPool);
        qrs.setJobStore(jobStore);

        QuartzScheduler qs = new QuartzScheduler(qrs, schedCtxt, 0, 0);

        ClassLoadHelper cch = new CascadingClassLoadHelper();
        cch.initialize();
        jobStore.initialize(cch, qs.getSchedulerSignaler());
        StdScheduler scheduler = new StdScheduler(qs, schedCtxt);
        jrsf.initialize(scheduler, schedCtxt);
        SchedulerRepository schedRep = SchedulerRepository.getInstance();
        qs.addNoGCObject(schedRep);
        schedRep.bind(scheduler);
        return scheduler;
    }

    public void registerSynchronizer(final Synchronizer synch) throws ContextException {
        try {
            _txm.getTransaction().registerSynchronization(new Synchronization() {

                public void beforeCompletion() {
                    synch.beforeCompletion();
                }

                public void afterCompletion(int status) {
                    synch.afterCompletion(status == Status.STATUS_COMMITTED);
                }

            });
        } catch (Exception e) {
            throw new ContextException("Unable to register synchronizer.", e);
        }
    }

}
