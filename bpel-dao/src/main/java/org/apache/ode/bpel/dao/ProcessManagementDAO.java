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