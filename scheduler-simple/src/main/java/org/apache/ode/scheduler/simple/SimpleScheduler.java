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
package org.apache.ode.scheduler.simple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.dao.scheduler.JobDAO;
import org.apache.ode.dao.scheduler.SchedulerDAOConnection;
import org.apache.ode.dao.scheduler.SchedulerDAOConnectionFactory;

/**
 * A reliable and relatively simple scheduler that uses a database to persist information about scheduled tasks.
 *
 * The challange is to achieve high performance in a small memory footprint without loss of reliability while supporting
 * distributed/clustered configurations.
 *
 * The design is based around three time horizons: "immediate", "near future", and "everything else". Immediate jobs (i.e. jobs that
 * are about to be up) are written to the database and kept in an in-memory priority queue. When they execute, they are removed from
 * the database. Near future jobs are placed in the database and assigned to the current node, however they are not stored in
 * memory. Periodically jobs are "upgraded" from near-future to immediate status, at which point they get loaded into memory. Jobs
 * that are further out in time, are placed in the database without a node identifer; when they are ready to be "upgraded" to
 * near-future jobs they are assigned to one of the known live nodes. Recovery is rather straighforward, with stale node identifiers
 * being reassigned to known good nodes.
 *
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 *
 */
public class SimpleScheduler implements Scheduler, TaskRunner {

  private static final Log __log = LogFactory.getLog(SimpleScheduler.class);
  /**
   * Jobs scheduled with a time that is between [now, now+immediateInterval] will be assigned to the current node, and placed
   * directly on the todo queue.
   */
  long _immediateInterval = 30000;
  /**
   * Jobs sccheduled with a time that is between (now+immediateInterval,now+nearFutureInterval) will be assigned to the current
   * node, but will not be placed on the todo queue (the promoter will pick them up).
   */
  long _nearFutureInterval = 10 * 60 * 1000;
  /** 10s of no communication and you are deemed dead. */
  long _staleInterval = 10000;
  String _nodeId;
  /** Maximum number of jobs in the "near future" / todo queue. */
  int _todoLimit = 10000;
  /** The object that actually handles the jobs. */
  volatile JobProcessor _jobProcessor;
  volatile JobProcessor _polledRunnableProcessor;
  private SchedulerThread _todo;
  private SchedulerDAOConnectionFactory _dbcf;
  private TransactionManager _txm;
  /** All the nodes we know about */
  private CopyOnWriteArraySet<String> _knownNodes = new CopyOnWriteArraySet<String>();
  /** When we last heard from our nodes. */
  private ConcurrentHashMap<String, Long> _lastHeartBeat = new ConcurrentHashMap<String, Long>();
  private boolean _running;
  /** Time for next upgrade. */
  private AtomicLong _nextUpgrade = new AtomicLong();
  /** Time for next job load */
  private AtomicLong _nextScheduleImmediate = new AtomicLong();
  private Random _random = new Random();

  public SimpleScheduler(String nodeId, SchedulerDAOConnectionFactory dbcf, TransactionManager mgr, Properties conf) {
    _nodeId = nodeId;
    _dbcf = dbcf;
    _txm = mgr;
    _todoLimit = Integer.parseInt(conf.getProperty("ode.scheduler.queueLength", "10000"));
    _todo = new SchedulerThread(this);
  }

  public void setNodeId(String nodeId) {
    _nodeId = nodeId;
  }

  public void setStaleInterval(long staleInterval) {
    _staleInterval = staleInterval;
  }

  public void setImmediateInterval(long immediateInterval) {
    _immediateInterval = immediateInterval;
  }

  public void setNearFutureInterval(long nearFutureInterval) {
    _nearFutureInterval = nearFutureInterval;
  }

  public void setSchedulerDAOConnectionFactory(SchedulerDAOConnectionFactory dbcf) {
    _dbcf = dbcf;
  }

  public void setPolledRunnableProcesser(JobProcessor polledRunnableProcessor) {
    _polledRunnableProcessor = polledRunnableProcessor;
  }

    public void cancelJob(final String jobId) throws ContextException {
        SchedulerDAOConnection conn = _dbcf.getConnection();
        _todo.dequeue(new JobDAOTask(jobId));
        if (!conn.deleteJob(jobId, _nodeId)) {
            __log.debug("Job removal failed.");
            throw new ContextException("Job removal failed.");
        }
    }

  public String schedulePersistedJob(final JobDetails jobDetail, Date when) throws ContextException {
    long ctime = System.currentTimeMillis();
    if (when == null) {
      when = new Date(ctime);
    }

    if (__log.isDebugEnabled()) {
      __log.debug("scheduling " + jobDetail + " for " + when);
    }

    return schedulePersistedJob(jobDetail, true, when, ctime);
  }

  public String scheduleMapSerializableRunnable(MapSerializableRunnable runnable, Date when) throws ContextException {
    long ctime = System.currentTimeMillis();
    if (when == null) {
      when = new Date(ctime);
    }

    JobDetails jobDetails = new JobDetails();
    jobDetails.getDetailsExt().put("runnable", runnable);
    runnable.storeToDetails(jobDetails);

    if (__log.isDebugEnabled()) {
      __log.debug("scheduling " + jobDetails + " for " + when);
    }

    return schedulePersistedJob(jobDetails, true, when, ctime);
  }

    private JobDAO insertJob(final boolean transacted, final JobDetails jobDetails, final long scheduledDate, final String nodeID, final boolean loaded, final boolean enqueue) throws ContextException {
        SchedulerDAOConnection conn = _dbcf.getConnection();
        final JobDAO job = conn.createJob(transacted, jobDetails, true, scheduledDate);
        if (!conn.insertJob(job, nodeID, loaded)) {
            String msg = String.format("Database insert failed. jobId %s nodeId %s", job.getJobId(), nodeID);
            __log.error(msg);
            throw new ContextException(msg);
        }
        if (enqueue) {
            addTodoOnCommit(new JobDAOTask(job));
        }
        return job;
    }

  public String schedulePersistedJob(JobDetails jobDetails, boolean transacted, Date when, long ctime) throws ContextException {
    boolean immediate = when.getTime() <= ctime + _immediateInterval;
    boolean nearfuture = !immediate && when.getTime() <= ctime + _nearFutureInterval;
    JobDAO job;

    if (immediate) {
      // If we have too many jobs in the queue, we don't allow any new ones
      if (_todo.size() > _todoLimit) {
        __log.error("The execution queue is backed up, the engine can't keep up with the load. Either "
                + "increase the queue size or regulate the flow.");
        return null;
      }
      job = insertJob(transacted, jobDetails, when.getTime(), _nodeId, true, true);
      __log.debug("scheduled immediate job: " + job.getJobId());
    } else if (nearfuture) {
      // Near future, assign the job to ourselves (why? -- this makes it very unlikely that we
      // would get two nodes trying to process the same instance, which causes unsightly rollbacks).
      job = insertJob(transacted, jobDetails, when.getTime(), _nodeId, false, false);
      __log.debug("scheduled near-future job: " + job.getJobId());
    } else /* far future */ {
      // Not the near future, we don't assign a node-id, we'll assign it later.
      job = insertJob(transacted, jobDetails, when.getTime(), null, false, false);
      __log.debug("scheduled far-future job: " + job.getJobId());
    }
    return job.getJobId();
  }

    public String scheduleVolatileJob(final boolean transacted, final JobDetails jobDetail) throws ContextException {
        SchedulerDAOConnection conn = _dbcf.getConnection();
        final JobDAO job = conn.createJob(transacted, jobDetail, false, System.currentTimeMillis());
        addTodoOnCommit(new JobDAOTask(job));
        return job.getJobId();
    }

  public void setJobProcessor(JobProcessor processor) throws ContextException {
    _jobProcessor = processor;
  }

  public void shutdown() {
    stop();
    _jobProcessor = null;
    _todo = null;
  }

  public synchronized void start() {
    if (_running) {
      return;
    }

    _todo.clearTasks(UpgradeJobsTask.class);
    _todo.clearTasks(LoadImmediateTask.class);
    _todo.clearTasks(CheckStaleNodes.class);

    _knownNodes.clear();

      exec(new Callable<Boolean>() {

          public Boolean call(SchedulerDAOConnection conn) throws ContextException {
              List<String> ids = conn.getNodeIds();
              if (ids == null) {
                  __log.error("Error retrieving node list.");
                  throw new ContextException("Error retrieving node list.");
              }
              _knownNodes.addAll(ids);
              return null;
          }
      });

      // Pretend we got a heartbeat...
      for (String s : _knownNodes) {
      _lastHeartBeat.put(s, System.currentTimeMillis());
    }

    // schedule immediate job loading for now!
    _todo.enqueue(new LoadImmediateTask(System.currentTimeMillis()));

    // schedule check for stale nodes, make it random so that the nodes don't overlap.
    _todo.enqueue(new CheckStaleNodes(System.currentTimeMillis() + (long) (_random.nextDouble() * _staleInterval)));

    // do the upgrade sometime (random) in the immediate interval.
    _todo.enqueue(new UpgradeJobsTask(System.currentTimeMillis() + (long) (_random.nextDouble() * _immediateInterval)));

    _todo.start();
    _running = true;
  }

  public synchronized void stop() {
    if (!_running) {
      return;
    }

    _todo.stop();
    _todo.clearTasks(UpgradeJobsTask.class);
    _todo.clearTasks(LoadImmediateTask.class);
    _todo.clearTasks(CheckStaleNodes.class);
    _running = false;
  }

    public void jobCompleted(final String jobId) {
        SchedulerDAOConnection conn = _dbcf.getConnection();
        boolean deleted = conn.deleteJob(jobId, _nodeId);
        if (!deleted) {
            throw new ContextException("Job no longer in database: jobId=" + jobId);
        }
    }

  /**
   * Run a job in the current thread.
   *
   * @param job
   *            job to run.
   */
  protected void runJob(final JobDAO job) {
    final Scheduler.JobInfo jobInfo = new Scheduler.JobInfo(job.getJobId(), job.getDetails(), job.getDetails().getRetryCount());

    try {
      try {
        _jobProcessor.onScheduledJob(jobInfo);
      } catch (JobProcessorException jpe) {
        if (jpe.retry) {
          __log.error("Error while processing transaction, retrying in " + doRetry(job) + "s");
        } else {
          __log.error("Error while processing transaction, no retry.", jpe);
        }
      }
    } catch (Exception ex) {
      __log.error("Error in scheduler processor.", ex);
    }

  }

    private void addTodoOnCommit(final JobDAOTask job) {

        Transaction tx;
        try {
            tx = _txm.getTransaction();
        } catch (Exception ex) {
            String errmsg = "Transaction manager error; unable to obtain transaction.";
            __log.error(errmsg, ex);
            throw new ContextException(errmsg, ex);
        }

        if (tx == null) {
            _todo.enqueue(job);
        } else {
            try {
                tx.registerSynchronization(new Synchronization() {

                    public void afterCompletion(int status) {
                        if (status == Status.STATUS_COMMITTED) {
                            _todo.enqueue(job);
                        }
                    }

                    public void beforeCompletion() {
                    }
                });
            } catch (Exception e) {
                String errmsg = "Unable to registrer synchronizer. ";
                __log.error(errmsg, e);
                throw new ContextException(errmsg, e);
            }
        }
    }

  public void runTask(Task task) {
    if (task instanceof JobDAOTask) {
      runJob(((JobDAOTask) task).dao);
    }
    if (task instanceof SchedulerTask) {
      ((SchedulerTask) task).run();
    }
  }

  public void updateHeartBeat(String nodeId) {
    if (nodeId == null) {
      return;
    }

    if (_nodeId.equals(nodeId)) {
      return;
    }

    _lastHeartBeat.put(nodeId, System.currentTimeMillis());
    _knownNodes.add(nodeId);
  }

    boolean doLoadImmediate() {
        __log.debug("LOAD IMMEDIATE started");

        List<JobDAO> jobs;
        do {
            jobs = exec(new Callable<List<JobDAO>>() {
                public List<JobDAO> call(SchedulerDAOConnection conn) throws ContextException {
                    return conn.dequeueImmediate(_nodeId, System.currentTimeMillis() + _immediateInterval, 10);
                }
            });
            if (jobs == null) {
                __log.error("Error loading immediate jobs from database.");
                return false;
            }
            for (JobDAO j : jobs) {
                if (__log.isDebugEnabled()) {
                    __log.debug("todo.enqueue job from db: " + j.getJobId() + " for " + j.getScheduledDate());
                }
                _todo.enqueue(new JobDAOTask(j));
            }

        } while (jobs.size() == 10);
        __log.debug("LOAD IMMEDIATE complete");
        return true;
    }

    boolean doUpgrade() {
        __log.debug("UPGRADE started");
        final ArrayList<String> knownNodes = new ArrayList<String>(_knownNodes);
        // Don't forget about self.
        knownNodes.add(_nodeId);
        Collections.sort(knownNodes);

        // We're going to try to upgrade near future jobs using the db only.
        // We assume that the distribution of the trailing digits in the
        // scheduled time are uniformly distributed, and use modular division
        // of the time by the number of nodes to create the node assignment.
        // This can be done in a single update statement.
        final long maxtime = System.currentTimeMillis() + _nearFutureInterval;
        return exec(new Callable<Boolean>() {
            public Boolean call(SchedulerDAOConnection conn) throws ContextException {
                int numNodes = knownNodes.size();
                if (numNodes == -1) {
                    __log.error("Database error upgrading jobs.");
                    return false;
                }
                for (int i = 0; i < numNodes; ++i) {
                    String node = knownNodes.get(i);
                    if (conn.updateAssignToNode(node, i, numNodes, maxtime) == -1) {
                        __log.error("updateAssignToNode failed");
                        return false;
                    }
                }
                __log.debug("UPGRADE complete");
                return true;
            }
        });
    }

  /**
   * Re-assign stale node's jobs to self.
   *
   * @param nodeId
   */
    void recoverStaleNode(final String nodeId) {
        __log.debug("recovering stale node " + nodeId);
        int numrows = exec(new Callable<Integer>() {

            public Integer call(SchedulerDAOConnection conn) throws ContextException {
                int numrows = conn.updateReassign(nodeId, _nodeId);
                if (numrows == -1) {
                    __log.error("Database error reassigning node.");
                    throw new ContextException("Database error reassigning node.");
                }
                __log.debug("reassigned " + numrows + " jobs to self. ");
                return numrows;

            }
        });

        // We can now forget about this node, if we see it again, it will be
        // "new to us"
        _knownNodes.remove(nodeId);
        _lastHeartBeat.remove(nodeId);

        // Force a load-immediate to catch anything new from the recovered node.
        doLoadImmediate();
        __log.debug("node recovery complete");
    }

    private long doRetry(final JobDAO job) {
        final int retry = job.getDetails().getRetryCount() + 1;
        final long delay = (long) (Math.pow(5, retry - 1));

        /*even though the JobDetails property is marked with Transient Hibernate
        still treats any changes to it as a modification and will attempt to persist it.
        for the new Job we will clone the existing details.*/

        Scheduler.JobDetails newJobDetails;
        try {
            newJobDetails = job.getDetails().clone();
        } catch (CloneNotSupportedException ce) {
            __log.error("Unable to clone job details", ce);
            return -1L;
        }
        newJobDetails.setRetryCount(newJobDetails.getRetryCount() + 1);
        SchedulerDAOConnection conn = _dbcf.getConnection();
        JobDAO jobRetry = conn.createJob(job.isTransacted(), newJobDetails, job.isPersisted(), System.currentTimeMillis() + delay * 1000);
        if (!conn.insertJob(jobRetry, _nodeId, false)) {
            __log.debug("retry failed for Job " + jobRetry.getJobId());
            return -1L;
        }
        __log.debug("added retry for Job " + job.getJobId() + " at " + job.getScheduledDate() + " new Job: " + jobRetry.getJobId() + " at " + jobRetry.getScheduledDate());
        return delay;
    }

  <T> T exec(Callable<T> callable) throws ContextException {
        //Execute the work on the current thread
      return callable.call();
    }

    /**
     * Wrapper for database transactions.
     *
     * @author Maciej Szefler
     *
     * @param <V>
     *            return type
     */
    abstract class Callable<V> implements java.util.concurrent.Callable<V> {

        public V call() throws ContextException {
            boolean success = false;
            SchedulerDAOConnection conn = _dbcf.getConnection();
            try {
                if (_txm != null) {
                    _txm.begin();
                }
                V r = call(conn);
                if (_txm != null) {
                    _txm.commit();
                }
                success = true;
                return r;
            } catch (ContextException ce) {
                throw ce;
            } catch (Exception e) {
                throw new ContextException("TxError", e);
            } finally {

                try {
                    if (!success && _txm != null ) {
                        _txm.rollback();
                    }
                } catch (Exception ex) {
                    __log.error("TxError", ex);
                }
                conn.close();
            }

        }

        abstract V call(SchedulerDAOConnection conn) throws ContextException;
    }

  private abstract class SchedulerTask extends Task implements Runnable {

    SchedulerTask(long schedDate) {
      super(schedDate);
    }
  }

  private class LoadImmediateTask extends SchedulerTask {

    LoadImmediateTask(long schedDate) {
      super(schedDate);
    }

    public void run() {
      boolean success = false;
      try {
        success = doLoadImmediate();
      } finally {
        if (success) {
          _todo.enqueue(new LoadImmediateTask(System.currentTimeMillis() + (long) (_immediateInterval * .75)));
        } else {
          _todo.enqueue(new LoadImmediateTask(System.currentTimeMillis() + 100));
        }
      }
    }
  }

  /**
   * Upgrade jobs from far future to immediate future (basically, assign them to a node).
   *
   * @author mszefler
   *
   */
  private class UpgradeJobsTask extends SchedulerTask {

    UpgradeJobsTask(long schedDate) {
      super(schedDate);
    }

    public void run() {
      long ctime = System.currentTimeMillis();
      long ntime = _nextUpgrade.get();
      __log.debug("UPGRADE task for " + schedDate + " fired at " + ctime);

      // We could be too early, this can happen if upgrade gets delayed due to another
      // node
      if (_nextUpgrade.get() > System.currentTimeMillis()) {
        __log.debug("UPGRADE skipped -- wait another " + (ntime - ctime) + "ms");
        _todo.enqueue(new UpgradeJobsTask(ntime));
        return;
      }

      boolean success = false;
      try {
        success = doUpgrade();
      } finally {
        long future = System.currentTimeMillis() + (success ? (long) (_nearFutureInterval * .50) : 100);
        _nextUpgrade.set(future);
        _todo.enqueue(new UpgradeJobsTask(future));
        __log.debug("UPGRADE completed, success = " + success + "; next time in " + (future - ctime) + "ms");
      }
    }
  }

  /**
   * Check if any of the nodes in our cluster are stale.
   */
  private class CheckStaleNodes extends SchedulerTask {

    CheckStaleNodes(long schedDate) {
      super(schedDate);
    }

    public void run() {
      _todo.enqueue(new CheckStaleNodes(System.currentTimeMillis() + _staleInterval));
      __log.debug("CHECK STALE NODES started");
      for (String nodeId : _knownNodes) {
        Long lastSeen = _lastHeartBeat.get(nodeId);
        if (lastSeen == null || (System.currentTimeMillis() - lastSeen) > _staleInterval) {
          recoverStaleNode(nodeId);
        }
      }
    }
  }
}
