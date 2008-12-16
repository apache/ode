package org.apache.ode.bpel.memdao;

import java.util.Collection;
import java.util.Date;

import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessManagementDAO;

public class ProcessManagementDaoImpl extends DaoBaseImpl implements ProcessManagementDAO {
	public Object[] findFailedCountAndLastFailedDateForProcessId(BpelDAOConnection conn, String status, String processId) {
        Date lastFailureDt = null;
        int failureInstances = 0;

        InstanceFilter instanceFilter = new InstanceFilter("status=" + status + " pid="+ processId);
        for (ProcessInstanceDAO instance : conn.instanceQuery(instanceFilter)) {
            int count = instance.getActivityFailureCount();
            if (count > 0) {
                ++failureInstances;
                Date failureDt = instance.getActivityFailureDateTime();
                if (lastFailureDt == null || lastFailureDt.before(failureDt))
                    lastFailureDt = failureDt;
            }
        }

        return new Object[] {failureInstances, lastFailureDt};
	}
	
	public void prefetchActivityFailureCounts(Collection<ProcessInstanceDAO> instances) {
		// do nothing 
	}
}
