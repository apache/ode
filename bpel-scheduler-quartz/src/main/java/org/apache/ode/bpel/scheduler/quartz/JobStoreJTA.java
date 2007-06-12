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
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.LoggingConnectionWrapper;
import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.core.SchedulingContext;
import org.quartz.impl.jdbcjobstore.Constants;
import org.quartz.impl.jdbcjobstore.JobStoreSupport;
import org.quartz.impl.jdbcjobstore.Semaphore;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredBundle;

/**
 * A server-level devloper friendly implementation of {@link JobStore}. This is
 * very similar to {@link org.quartz.impl.jdbcjobstore.JobStoreCMT} except that
 * we don't bother with the "non-managed" data source BS and go to the
 * transaction manager to get the job done.
 */
public class JobStoreJTA extends JobStoreSupport implements JobStore {

    private static final Log __log = LogFactory.getLog(JobStoreJTA.class);

    private TransactionManager _txm;

    // Quartz in-mem semaphore has a bug, ours is identical but fixes it
    private Semaphore _lockHandler = null;

    protected boolean setTxIsolationLevelReadCommitted = true;

    /** Thread-local for holding the transaction that was suspended if any */
    private ThreadLocal<Transaction> _suspenededTx = new ThreadLocal<Transaction>();

    public JobStoreJTA(TransactionManager txm) {
        _txm = txm;
    }

    public boolean isTxIsolationLevelReadCommitted() {
        return setTxIsolationLevelReadCommitted;
    }

    /**
     * Set the transaction isolation level of DB connections to sequential.
     *
     * @param b
     */
    public void setTxIsolationLevelReadCommitted(boolean b) {
        setTxIsolationLevelReadCommitted = b;
    }

    public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler)
            throws SchedulerConfigException {

        if (!getUseDBLocks() && !isClustered())
            _lockHandler = new NotSoSimpleSemaphore();
        super.initialize(loadHelper, signaler);
    }

    /**
     * <p>
     * Recover any failed or misfired jobs and clean up the data store as
     * appropriate.
     * </p>
     *
     * @throws JobPersistenceException
     *           if jobs could not be recovered
     */
    protected void recoverJobs() throws JobPersistenceException {

        boolean transOwner = false;

        Connection conn = getNonManagedTXConnection();
        try {

            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            recoverJobs(conn);
            commitConnection(conn);
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } catch (Exception e) {
            rollbackConnection(conn);
            throw new JobPersistenceException("Error recovering jobs: "
                    + e.getMessage(), e);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);
            closeConnection(conn);
        }
    }

    protected void cleanVolatileTriggerAndJobs() throws JobPersistenceException {

        boolean transOwner = false;

        Connection conn = getNonManagedTXConnection();
        try {

            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            cleanVolatileTriggerAndJobs(conn);

            commitConnection(conn);
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } catch (Exception e) {
            rollbackConnection(conn);
            throw new JobPersistenceException("Error cleaning volatile data: "
                    + e.getMessage(), e);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);
            closeConnection(conn);
        }
    }

    // ---------------------------------------------------------------------------
    // job / trigger storage methods
    // ---------------------------------------------------------------------------

    /**
     * <p>
     * Store the given <code>{@link org.quartz.JobDetail}</code> and
     * <code>{@link org.quartz.Trigger}</code>.
     * </p>
     *
     * @param newJob
     *          The <code>JobDetail</code> to be stored.
     * @param newTrigger
     *          The <code>Trigger</code> to be stored.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Job</code> with the same name/group already exists.
     */
    public void storeJobAndTrigger(SchedulingContext ctxt, JobDetail newJob,
                                   Trigger newTrigger) throws ObjectAlreadyExistsException,
            JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            if (isLockOnInsert()) {
                getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
                transOwner = true;
                getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);
            }

            if (newJob.isVolatile() && !newTrigger.isVolatile()) {
                JobPersistenceException jpe = new JobPersistenceException(
                        "Cannot associate non-volatile " + "trigger with a volatile job!");
                jpe.setErrorCode(SchedulerException.ERR_CLIENT_ERROR);
                throw jpe;
            }

            storeJob(conn, ctxt, newJob, false);
            storeTrigger(conn, ctxt, newTrigger, newJob, false,
                    Constants.STATE_WAITING, false, false);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Store the given <code>{@link org.quartz.JobDetail}</code>.
     * </p>
     *
     * @param newJob
     *          The <code>JobDetail</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Job</code> existing in the
     *          <code>JobStore</code> with the same name & group should be
     *          over-written.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Job</code> with the same name/group already exists,
     *           and replaceExisting is set to false.
     */
    public void storeJob(SchedulingContext ctxt, JobDetail newJob,
                         boolean replaceExisting) throws ObjectAlreadyExistsException,
            JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            if (isLockOnInsert() || replaceExisting) {
                getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
                transOwner = true;
                getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);
            }

            storeJob(conn, ctxt, newJob, replaceExisting);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Remove (delete) the <code>{@link org.quartz.Job}</code> with the given
     * name, and any <code>{@link org.quartz.Trigger}</code> s that reference
     * it.
     * </p>
     *
     * <p>
     * If removal of the <code>Job</code> results in an empty group, the group
     * should be removed from the <code>JobStore</code>'s list of known group
     * names.
     * </p>
     *
     * @param jobName
     *          The name of the <code>Job</code> to be removed.
     * @param groupName
     *          The group name of the <code>Job</code> to be removed.
     * @return <code>true</code> if a <code>Job</code> with the given name &
     *         group was found and removed from the store.
     */
    public boolean removeJob(SchedulingContext ctxt, String jobName,
                             String groupName) throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            return removeJob(conn, ctxt, jobName, groupName, true);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Retrieve the <code>{@link org.quartz.JobDetail}</code> for the given
     * <code>{@link org.quartz.Job}</code>.
     * </p>
     *
     * @param jobName
     *          The name of the <code>Job</code> to be retrieved.
     * @param groupName
     *          The group name of the <code>Job</code> to be retrieved.
     * @return The desired <code>Job</code>, or null if there is no match.
     */
    public JobDetail retrieveJob(SchedulingContext ctxt, String jobName,
                                 String groupName) throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return retrieveJob(conn, ctxt, jobName, groupName);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Store the given <code>{@link org.quartz.Trigger}</code>.
     * </p>
     *
     * @param newTrigger
     *          The <code>Trigger</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Trigger</code> existing in
     *          the <code>JobStore</code> with the same name & group should be
     *          over-written.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Trigger</code> with the same name/group already
     *           exists, and replaceExisting is set to false.
     */
    public void storeTrigger(SchedulingContext ctxt, Trigger newTrigger,
                             boolean replaceExisting) throws ObjectAlreadyExistsException,
            JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            if (isLockOnInsert() || replaceExisting) {
                getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
                transOwner = true;
            }

            storeTrigger(conn, ctxt, newTrigger, null, replaceExisting,
                    STATE_WAITING, false, false);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Remove (delete) the <code>{@link org.quartz.Trigger}</code> with the
     * given name.
     * </p>
     *
     * <p>
     * If removal of the <code>Trigger</code> results in an empty group, the
     * group should be removed from the <code>JobStore</code>'s list of known
     * group names.
     * </p>
     *
     * <p>
     * If removal of the <code>Trigger</code> results in an 'orphaned'
     * <code>Job</code> that is not 'durable', then the <code>Job</code>
     * should be deleted also.
     * </p>
     *
     * @param triggerName
     *          The name of the <code>Trigger</code> to be removed.
     * @param groupName
     *          The group name of the <code>Trigger</code> to be removed.
     * @return <code>true</code> if a <code>Trigger</code> with the given name &
     *         group was found and removed from the store.
     */
    public boolean removeTrigger(SchedulingContext ctxt, String triggerName,
                                 String groupName) throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;

            return removeTrigger(conn, ctxt, triggerName, groupName);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * @see org.quartz.spi.JobStore#replaceTrigger(org.quartz.core.SchedulingContext,
     *      java.lang.String, java.lang.String, org.quartz.Trigger)
     */
    public boolean replaceTrigger(SchedulingContext ctxt, String triggerName,
                                  String groupName, Trigger newTrigger) throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;

            return replaceTrigger(conn, ctxt, triggerName, groupName, newTrigger);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Retrieve the given <code>{@link org.quartz.Trigger}</code>.
     * </p>
     *
     * @param triggerName
     *          The name of the <code>Trigger</code> to be retrieved.
     * @param groupName
     *          The group name of the <code>Trigger</code> to be retrieved.
     * @return The desired <code>Trigger</code>, or null if there is no match.
     */
    public Trigger retrieveTrigger(SchedulingContext ctxt, String triggerName,
                                   String groupName) throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return retrieveTrigger(conn, ctxt, triggerName, groupName);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Store the given <code>{@link org.quartz.Calendar}</code>.
     * </p>
     *
     * @param calName
     *          The name of the calendar.
     * @param calendar
     *          The <code>Calendar</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Calendar</code> existing in
     *          the <code>JobStore</code> with the same name & group should be
     *          over-written.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Calendar</code> with the same name already exists,
     *           and replaceExisting is set to false.
     */
    public void storeCalendar(SchedulingContext ctxt, String calName,
                              Calendar calendar, boolean replaceExisting, boolean updateTriggers)
            throws ObjectAlreadyExistsException, JobPersistenceException {
        Connection conn = getConnection();
        boolean lockOwner = false;
        try {
            if (isLockOnInsert() || updateTriggers) {
                getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
                lockOwner = true;
            }

            storeCalendar(conn, ctxt, calName, calendar, replaceExisting,
                    updateTriggers);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, lockOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Remove (delete) the <code>{@link org.quartz.Calendar}</code> with the
     * given name.
     * </p>
     *
     * <p>
     * If removal of the <code>Calendar</code> would result in <code.Trigger</code>s
     * pointing to non-existent calendars, then a <code>JobPersistenceException</code>
     * will be thrown.
     * </p> *
     *
     * @param calName
     *          The name of the <code>Calendar</code> to be removed.
     * @return <code>true</code> if a <code>Calendar</code> with the given
     *         name was found and removed from the store.
     */
    public boolean removeCalendar(SchedulingContext ctxt, String calName)
            throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            getLockHandler().obtainLock(conn, LOCK_CALENDAR_ACCESS);

            return removeCalendar(conn, ctxt, calName);
        } finally {
            releaseLock(conn, LOCK_CALENDAR_ACCESS, true);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Retrieve the given <code>{@link org.quartz.Trigger}</code>.
     * </p>
     *
     * @param calName
     *          The name of the <code>Calendar</code> to be retrieved.
     * @return The desired <code>Calendar</code>, or null if there is no match.
     */
    public Calendar retrieveCalendar(SchedulingContext ctxt, String calName)
            throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return retrieveCalendar(conn, ctxt, calName);
        } finally {
            closeConnection(conn);
        }
    }

    // ---------------------------------------------------------------------------
    // informational methods
    // ---------------------------------------------------------------------------

    /**
     * <p>
     * Get the number of <code>{@link org.quartz.Job}</code> s that are stored
     * in the <code>JobStore</code>.
     * </p>
     */
    public int getNumberOfJobs(SchedulingContext ctxt)
            throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return getNumberOfJobs(conn, ctxt);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Get the number of <code>{@link org.quartz.Trigger}</code> s that are
     * stored in the <code>JobsStore</code>.
     * </p>
     */
    public int getNumberOfTriggers(SchedulingContext ctxt)
            throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return getNumberOfTriggers(conn, ctxt);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Get the number of <code>{@link org.quartz.Calendar}</code> s that are
     * stored in the <code>JobsStore</code>.
     * </p>
     */
    public int getNumberOfCalendars(SchedulingContext ctxt)
            throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return getNumberOfCalendars(conn, ctxt);
        } finally {
            closeConnection(conn);
        }
    }

    public Set getPausedTriggerGroups(SchedulingContext ctxt)
            throws JobPersistenceException {

        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            Set groups = getPausedTriggerGroups(conn, ctxt);
            return groups;
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Job}</code> s that
     * have the given group name.
     * </p>
     *
     * <p>
     * If there are no jobs in the given group name, the result should be a
     * zero-length array (not <code>null</code>).
     * </p>
     */
    public String[] getJobNames(SchedulingContext ctxt, String groupName)
            throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return getJobNames(conn, ctxt, groupName);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Trigger}</code> s
     * that have the given group name.
     * </p>
     *
     * <p>
     * If there are no triggers in the given group name, the result should be a
     * zero-length array (not <code>null</code>).
     * </p>
     */
    public String[] getTriggerNames(SchedulingContext ctxt, String groupName)
            throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return getTriggerNames(conn, ctxt, groupName);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Job}</code> groups.
     * </p>
     *
     * <p>
     * If there are no known group names, the result should be a zero-length array
     * (not <code>null</code>).
     * </p>
     */
    public String[] getJobGroupNames(SchedulingContext ctxt)
            throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return getJobGroupNames(conn, ctxt);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Trigger}</code>
     * groups.
     * </p>
     *
     * <p>
     * If there are no known group names, the result should be a zero-length array
     * (not <code>null</code>).
     * </p>
     */
    public String[] getTriggerGroupNames(SchedulingContext ctxt)
            throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return getTriggerGroupNames(conn, ctxt);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Calendar}</code> s in
     * the <code>JobStore</code>.
     * </p>
     *
     * <p>
     * If there are no Calendars in the given group name, the result should be a
     * zero-length array (not <code>null</code>).
     * </p>
     */
    public String[] getCalendarNames(SchedulingContext ctxt)
            throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return getCalendarNames(conn, ctxt);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Get all of the Triggers that are associated to the given Job.
     * </p>
     *
     * <p>
     * If there are no matches, a zero-length array should be returned.
     * </p>
     */
    public Trigger[] getTriggersForJob(SchedulingContext ctxt, String jobName,
                                       String groupName) throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return getTriggersForJob(conn, ctxt, jobName, groupName);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Get the current state of the identified <code>{@link Trigger}</code>.
     * </p>
     *
     * @see Trigger#STATE_NORMAL
     * @see Trigger#STATE_PAUSED
     * @see Trigger#STATE_COMPLETE
     * @see Trigger#STATE_ERROR
     * @see Trigger#STATE_NONE
     */
    public int getTriggerState(SchedulingContext ctxt, String triggerName,
                               String groupName) throws JobPersistenceException {
        Connection conn = getConnection();
        try {
            // no locks necessary for read...
            return getTriggerState(conn, ctxt, triggerName, groupName);
        } finally {
            closeConnection(conn);
        }
    }

    // ---------------------------------------------------------------------------
    // trigger state manipulation methods
    // ---------------------------------------------------------------------------

    /**
     * <p>
     * Pause the <code>{@link org.quartz.Trigger}</code> with the given name.
     * </p>
     *
     * @see #resumeTrigger(SchedulingContext, String, String)
     */
    public void pauseTrigger(SchedulingContext ctxt, String triggerName,
                             String groupName) throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            pauseTrigger(conn, ctxt, triggerName, groupName);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Pause all of the <code>{@link org.quartz.Trigger}s</code> in the given
     * group.
     * </p>
     *
     * @see #resumeTriggerGroup(SchedulingContext, String)
     */
    public void pauseTriggerGroup(SchedulingContext ctxt, String groupName)
            throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            pauseTriggerGroup(conn, ctxt, groupName);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Pause the <code>{@link org.quartz.Job}</code> with the given name - by
     * pausing all of its current <code>Trigger</code>s.
     * </p>
     *
     * @see #resumeJob(SchedulingContext, String, String)
     */
    public void pauseJob(SchedulingContext ctxt, String jobName, String groupName)
            throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            Trigger[] triggers = getTriggersForJob(conn, ctxt, jobName, groupName);
            for (int j = 0; j < triggers.length; j++) {
                pauseTrigger(conn, ctxt, triggers[j].getName(), triggers[j].getGroup());
            }
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Pause all of the <code>{@link org.quartz.Job}s</code> in the given group -
     * by pausing all of their <code>Trigger</code>s.
     * </p>
     *
     * @see #resumeJobGroup(SchedulingContext, String)
     */
    public void pauseJobGroup(SchedulingContext ctxt, String groupName)
            throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            String[] jobNames = getJobNames(conn, ctxt, groupName);

            for (int i = 0; i < jobNames.length; i++) {
                Trigger[] triggers = getTriggersForJob(conn, ctxt, jobNames[i],
                        groupName);
                for (int j = 0; j < triggers.length; j++) {
                    pauseTrigger(conn, ctxt, triggers[j].getName(), triggers[j]
                            .getGroup());
                }
            }
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Resume (un-pause) the <code>{@link org.quartz.Trigger}</code> with the
     * given name.
     * </p>
     *
     * <p>
     * If the <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     *
     * @see #pauseTrigger(SchedulingContext, String, String)
     */
    public void resumeTrigger(SchedulingContext ctxt, String triggerName,
                              String groupName) throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            resumeTrigger(conn, ctxt, triggerName, groupName);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Resume (un-pause) all of the <code>{@link org.quartz.Trigger}s</code> in
     * the given group.
     * </p>
     *
     * <p>
     * If any <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     *
     * @see #pauseTriggerGroup(SchedulingContext, String)
     */
    public void resumeTriggerGroup(SchedulingContext ctxt, String groupName)
            throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            resumeTriggerGroup(conn, ctxt, groupName);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Resume (un-pause) the <code>{@link org.quartz.Job}</code> with the given
     * name.
     * </p>
     *
     * <p>
     * If any of the <code>Job</code>'s<code>Trigger</code> s missed one or
     * more fire-times, then the <code>Trigger</code>'s misfire instruction
     * will be applied.
     * </p>
     *
     * @see #pauseJob(SchedulingContext, String, String)
     */
    public void resumeJob(SchedulingContext ctxt, String jobName, String groupName)
            throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            Trigger[] triggers = getTriggersForJob(conn, ctxt, jobName, groupName);
            for (int j = 0; j < triggers.length; j++) {
                resumeTrigger(conn, ctxt, triggers[j].getName(), triggers[j].getGroup());
            }
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Resume (un-pause) all of the <code>{@link org.quartz.Job}s</code> in the
     * given group.
     * </p>
     *
     * <p>
     * If any of the <code>Job</code> s had <code>Trigger</code> s that missed
     * one or more fire-times, then the <code>Trigger</code>'s misfire
     * instruction will be applied.
     * </p>
     *
     * @see #pauseJobGroup(SchedulingContext, String)
     */
    public void resumeJobGroup(SchedulingContext ctxt, String groupName)
            throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            String[] jobNames = getJobNames(conn, ctxt, groupName);

            for (int i = 0; i < jobNames.length; i++) {
                Trigger[] triggers = getTriggersForJob(conn, ctxt, jobNames[i],
                        groupName);
                for (int j = 0; j < triggers.length; j++) {
                    resumeTrigger(conn, ctxt, triggers[j].getName(), triggers[j]
                            .getGroup());
                }
            }
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Pause all triggers - equivalent of calling
     * <code>pauseTriggerGroup(group)</code> on every group.
     * </p>
     *
     * <p>
     * When <code>resumeAll()</code> is called (to un-pause), trigger misfire
     * instructions WILL be applied.
     * </p>
     *
     * @see #resumeAll(SchedulingContext)
     * @see #pauseTriggerGroup(SchedulingContext, String)
     */
    public void pauseAll(SchedulingContext ctxt) throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            pauseAll(conn, ctxt);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Resume (un-pause) all triggers - equivalent of calling
     * <code>resumeTriggerGroup(group)</code> on every group.
     * </p>
     *
     * <p>
     * If any <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     *
     * @see #pauseAll(SchedulingContext)
     */
    public void resumeAll(SchedulingContext ctxt) throws JobPersistenceException {
        Connection conn = getConnection();
        boolean transOwner = false;
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            resumeAll(conn, ctxt);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    // ---------------------------------------------------------------------------
    // trigger firing methods
    // ---------------------------------------------------------------------------

    /**
     * <p>
     * Get a handle to the next trigger to be fired, and mark it as 'reserved' by
     * the calling scheduler.
     * </p>
     *
     * @see #releaseAcquiredTrigger(SchedulingContext, Trigger)
     */
    public Trigger acquireNextTrigger(SchedulingContext ctxt, long noLaterThan)
            throws JobPersistenceException {
        boolean transOwner = false;

        Connection conn = getNonManagedTXConnection();
        try {

            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            Trigger trigger = acquireNextTrigger(conn, ctxt, noLaterThan);

            commitConnection(conn);
            return trigger;
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } catch (Exception e) {
            rollbackConnection(conn);
            throw new JobPersistenceException(
                    "Error acquiring next firable trigger: " + e.getMessage(), e);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Inform the <code>JobStore</code> that the scheduler no longer plans to
     * fire the given <code>Trigger</code>, that it had previously acquired
     * (reserved).
     * </p>
     */
    public void releaseAcquiredTrigger(SchedulingContext ctxt, Trigger trigger)
            throws JobPersistenceException {
        if (__log.isDebugEnabled())
            __log.debug("releaseAcquiredTrigger: " + trigger);
        
        boolean transOwner = false;
        Connection conn = getNonManagedTXConnection();
        ;
        try {

            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            releaseAcquiredTrigger(conn, ctxt, trigger);
            commitConnection(conn);
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } catch (Exception e) {
            rollbackConnection(conn);
            throw new JobPersistenceException("Error releasing acquired trigger: "
                    + e.getMessage(), e);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Inform the <code>JobStore</code> that the scheduler is now firing the
     * given <code>Trigger</code> (executing its associated <code>Job</code>),
     * that it had previously acquired (reserved).
     * </p>
     *
     * @return null if the trigger or it's job or calendar no longer exist, or if
     *         the trigger was not successfully put into the 'executing' state.
     */
    public TriggerFiredBundle triggerFired(SchedulingContext ctxt, Trigger trigger)
            throws JobPersistenceException {

        if (__log.isDebugEnabled())
            __log.debug("triggerFired: " + trigger);
        
        boolean transOwner = false;
        Connection conn = getNonManagedTXConnection();

        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            TriggerFiredBundle tfb = null;
            JobPersistenceException err = null;
            try {
                tfb = triggerFired(conn, ctxt, trigger);
            } catch (JobPersistenceException jpe) {
                if (jpe.getErrorCode() != SchedulerException.ERR_PERSISTENCE_JOB_DOES_NOT_EXIST)
                    throw jpe;
                err = jpe;
            }

            if (err != null)
                throw err;

            commitConnection(conn);
            return tfb;
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } catch (Exception e) {
            rollbackConnection(conn);
            throw new JobPersistenceException("TX failure: " + e.getMessage(), e);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);
            closeConnection(conn);
        }
    }

    /**
     * <p>
     * Inform the <code>JobStore</code> that the scheduler has completed the
     * firing of the given <code>Trigger</code> (and the execution its
     * associated <code>Job</code>), and that the
     * <code>{@link org.quartz.JobDataMap}</code> in the given
     * <code>JobDetail</code> should be updated if the <code>Job</code> is
     * stateful.
     * </p>
     */
    public void triggeredJobComplete(SchedulingContext ctxt, Trigger trigger,
                                     JobDetail jobDetail, int triggerInstCode) throws JobPersistenceException {
        if (__log.isDebugEnabled())
            __log.debug("triggeredJobComplete: trigger=" + trigger + ", jobName=" + jobDetail.getFullName() + ", triggerInstCode="+ triggerInstCode);

        boolean transOwner = false;

        Connection conn = getNonManagedTXConnection();
        try {
            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;
            getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);

            triggeredJobComplete(conn, ctxt, trigger, jobDetail, triggerInstCode);

            commitConnection(conn);
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } catch (Exception e) {
            rollbackConnection(conn);
            throw new JobPersistenceException("TX failure: " + e.getMessage(), e);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            releaseLock(conn, LOCK_JOB_ACCESS, transOwner);

            closeConnection(conn);
        }
    }

    protected boolean doRecoverMisfires() throws JobPersistenceException {
        __log.debug("doRecoverMisfires() callled");
        boolean transOwner = false;
        boolean moreToDo = false;

        Connection conn = getNonManagedTXConnection();
        try {

            getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
            transOwner = true;

            try {
                moreToDo = recoverMisfiredJobs(conn, false);
            } catch (Exception e) {
                throw new JobPersistenceException(e.getMessage(), e);
            }

            commitConnection(conn);

            __log.debug("doRecoverMisfires() returned moreToDo = " + moreToDo);
            return moreToDo;
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } catch (Exception e) {
            rollbackConnection(conn);
            throw new JobPersistenceException("TX failure: " + e.getMessage(), e);
        } finally {
            releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            closeConnection(conn);
        }

    }

    protected synchronized boolean doCheckin() throws JobPersistenceException {
        __log.debug("doCheckin() called, firstCheckIn=" + firstCheckIn);
        boolean transStateOwner = false;
        boolean recovered = false;

        Connection conn = getNonManagedTXConnection();
        try {
            List failedRecords = (firstCheckIn) ? null : clusterCheckIn(conn);

            
            if (__log.isDebugEnabled())
                __log.debug("doCheckin: firstCheckIn=" + firstCheckIn + ", failedRecords=" + failedRecords);

            getLockHandler().obtainLock(conn, LOCK_STATE_ACCESS);
            transStateOwner = true;

            if (firstCheckIn || failedRecords.size() > 0) {
                failedRecords = (firstCheckIn) ? clusterCheckIn(conn) : findFailedInstances(conn);

                if (!failedRecords.isEmpty()) {
                    if (__log.isDebugEnabled())
                        __log.debug("doChecking: recovering " + failedRecords);
                    
                    getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
                    getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);
                    clusterRecover(conn, failedRecords);
                }
                recovered = true;
            }

            commitConnection(conn);
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } catch (Exception e) {
            rollbackConnection(conn);
            throw new JobPersistenceException("TX failure: " + e.getMessage(), e);
        } finally {
            releaseLock(conn, LOCK_STATE_ACCESS, transStateOwner);
            closeConnection(conn);
        }

        firstCheckIn = false;  

        return recovered;
    }

    
    protected Connection getNonManagedTXConnection()
            throws JobPersistenceException {

        __log.debug("getNonManagedTXConnection()");

        if (_suspenededTx.get() != null) {
            __log.fatal("Internal Error: found suspended transaction: " + _suspenededTx.get());
            throw new IllegalStateException("Found suspended transaction: " + _suspenededTx.get());
        }
        
        try {
            if (_txm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                _suspenededTx.set(_txm.suspend());
            }
        } catch (Exception ex) {
            __log.error("Unable to suspend JTA transaction.", ex);
            _suspenededTx.set(null);
            throw new JobPersistenceException("Unable to suspend!", ex);
        }

        // at this point we are suspended, if we failt to obtain a connection
        // we have to resume!
        boolean resume = true;
        boolean rollback = false;
        try {
            _txm.begin();
            rollback = true;

            Connection conn = getConnection();
            if (isTxIsolationLevelReadCommitted())
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            rollback = false;
            resume = false;
            return new LoggingConnectionWrapper(conn, __log);
        } catch (SQLException sqle) {
            throw new JobPersistenceException(
                    "Failed to obtain DB connection from data source '" + getDataSource()
                            + "': " + sqle.toString(), sqle);
        } catch (Exception e) {
            throw new JobPersistenceException(
                    "Failed to obtain DB connection from data source '" + getDataSource()
                            + "': " + e.toString(), e,
                    JobPersistenceException.ERR_PERSISTENCE_CRITICAL_FAILURE);
        } finally {
            if (rollback) rollbackConnection(null);
            if (resume)
                resume();
        }
    }

    @Override
    protected void commitConnection(Connection conn)
            throws JobPersistenceException {
        __log.debug("COMMIT: "+ conn);
        try {
            _txm.commit();
        } catch (Exception ex) {
            throw new JobPersistenceException("Couldn't commit jdbc connection. "
                    + ex.getMessage(), ex);
        }
    }

    @Override
    protected void rollbackConnection(Connection conn) {
        __log.debug("ROLLBACK: "+ conn);
        try {
            if (_txm.getStatus() != Status.STATUS_NO_TRANSACTION) {
            _txm.rollback();
            }
        } catch (Exception e) {
            __log.error("Exception while trying to rollback transaction", e);
        }
    }

    private void resume() {
        Transaction suspended = _suspenededTx.get();
        _suspenededTx.set(null);
        if (suspended != null)
            try {
                _txm.resume(suspended);
            } catch (Exception ex) {
                __log.error("Error resuming transaction.", ex);
            }
    }

    @Override
    protected void closeConnection(Connection conn)
            throws JobPersistenceException {
        try {
            conn.close();
        } catch (Exception ex) {
            __log.error("Error closing connection",ex);
        }

        try {
            if (_suspenededTx.get() != null && _txm.getStatus() == Status.STATUS_ACTIVE) {
                __log.error("Unexpected: transaction still active", new Exception());
                rollbackConnection(conn);
            }
        } catch (Exception ex) {
            __log.error("Error getting transaction status",ex);
        }
        resume();
    }

    public boolean getUseDBLocks() {
        return false;
    }
    
    public boolean isLockOnInsert() {
        return false;
    }

    protected Semaphore getLockHandler() {
        return _lockHandler == null ? super.getLockHandler() : _lockHandler;
    }
}
