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

package org.apache.ode.bpel.dao;

import java.util.Collection;

/**
 * This DAO handles any process and instance management related database operations. The idea is to separate out
 * the operational side of database tasks from core engine.
 * 
 * @author sean
 *
 */
public interface ProcessManagementDAO {
	/**
	 * Finds process instances that have failures on a given process id, and, returns the number of failed instances
	 * and the last failed date in an object array.
	 * 
	 * @param conn BpelDAOConnection
	 * @param status the status string, e.g. "active"
	 * @param processId the string representation of the QName of the process
	 * @return an array containing the number of failed instances and the last failed date
	 */
	public Object[] findFailedCountAndLastFailedDateForProcessId(BpelDAOConnection conn, String status, String processId);
	
	/**
	 * Prefetches the counts of activity failures for the given instances and sets the values to the _activityFailureCount
	 * member variable of the ProcesInstanceDAOImpl.
	 * 
	 * @param instances a collection of process instances
	 */
	public void prefetchActivityFailureCounts(Collection<ProcessInstanceDAO> instances);
}