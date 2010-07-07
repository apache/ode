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

import java.util.List;

/**
 * Database abstraction; provides all database access for the simple scheduler.
 *
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 *
 */
public interface DatabaseDelegate {
    /**
     * Save the job in the database.
     * @param job the job
     * @param nodeId node assigned to the job (or null if no node has been asssigned)
     * @param loaded whether the job has been loaded into memory (i.e. in preperation for execution)
     * @throws DatabaseException in case of error
     */
    boolean insertJob(Job job, String nodeId, boolean loaded) throws DatabaseException ;

    /**
     * Update the job in the database (only updates timestamp and retryCount)
     *
     * @param job the job
     * @throws DatabaseException in case of error
     */
    boolean updateJob(Job job) throws DatabaseException;

    /**
     * Delete a job from the database.
     * @param jobid job identifier
     * @param nodeId node identifier
     * @throws DatabaseException in case of error
     */
    boolean deleteJob(String jobid, String nodeId) throws DatabaseException;

    /**
     * Return a list of unique nodes identifiers found in the database. This is used
     * to initialize the list of known nodes when a new node starts up.
     * @return list of unique node identfiers found in the databaseuniqu
     */
    List<String> getNodeIds() throws DatabaseException;

    /**
     * "Dequeue" jobs from the database that are ready for immediate execution; this basically
     * is a select/delete operation with constraints on the nodeId and scheduled time.
     *
     * @param nodeId node identifier of the jobs
     * @param maxtime only jobs with scheduled time earlier than this will be dequeued
     * @param maxjobs maximum number of jobs to deqeue
     * @return list of jobs that met the criteria and were deleted from the database
     * @throws DatabaseException in case of error
     */
    List<Job> dequeueImmediate(String nodeId, long maxtime, int maxjobs) throws DatabaseException ;

    /**
     * Assign a particular node identifier to a fraction of jobs in the database that do not have one,
     * and are up for execution within a certain time. Only a fraction of the jobs found are assigned
     * the node identifier. This fraction is determined by the "y" parameter, while membership in the
     * group (of jobs that get the nodeId) is determined by the "x" parameter. Essentially the logic is:
     * <code>
     *  UPDATE jobs AS job
     *      WHERE job.scheduledTime before :maxtime
     *            AND job.nodeId is null
     *            AND job.scheduledTime MOD :y == :x
     *      SET job.nodeId = :nodeId
     * </code>
     *
     * @param nodeId node identifier to assign to jobs
     * @param x the result of the mod-division
     * @param y the dividend of the mod-division
     * @param maxtime only jobs with scheduled time earlier than this will be updated
     * @return number of jobs updated
     * @throws DatabaseException in case of error
     */
    int updateAssignToNode(String nodeId, int x, int y, long maxtime) throws DatabaseException;

    /**
     * Reassign jobs from one node to another.
     *
     * @param oldnode node assigning from
     * @param newnode new node asssigning to
     * @return number of rows changed
     * @throws DatabaseException
     */
    int updateReassign(String oldnode, String newnode) throws DatabaseException;
    
    public void acquireTransactionLocks();
    
    public void deleteAllJobs();
}
