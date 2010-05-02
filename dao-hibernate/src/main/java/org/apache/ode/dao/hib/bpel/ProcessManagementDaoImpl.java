package org.apache.ode.dao.hib.bpel;

import java.util.Collection;
import java.util.List;

import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.dao.bpel.BpelDAOConnection;
import org.apache.ode.dao.bpel.ProcessInstanceDAO;
import org.apache.ode.dao.bpel.ProcessManagementDAO;
import org.apache.ode.dao.hib.SessionManager;
import org.apache.ode.dao.hib.bpel.hobj.HProcessInstance;
import org.hibernate.Query;
import org.hibernate.transform.ResultTransformer;

public class ProcessManagementDaoImpl extends HibernateDao implements ProcessManagementDAO {
	protected ProcessManagementDaoImpl(SessionManager sessionManager) {
		super(sessionManager, null);
	}

	@SuppressWarnings("unchecked")
	public Object[] findFailedCountAndLastFailedDateForProcessId(BpelDAOConnection conn, String status, String processId) {
		Object[] results = new Object[] {0, null};
		
        Query query = getSession().getNamedQuery(HProcessInstance.COUNT_FAILED_INSTANCES_BY_PROCESS_IDS_AND_STATES);
        query.setParameterList("states", new InstanceFilter("status=" + status).convertFilterState());
        query.setParameterList("processIds", new String[] {processId});
		query.setResultTransformer(new ResultTransformer() {
			private static final long serialVersionUID = 8034301512569916379L;

			public List transformList(List collection) {
				return collection;
			}
			public Object transformTuple(Object[] tuple, String[] aliases) {
				return tuple;
			}
		});
		
		List result = query.list();
		if( !result.isEmpty() ) {
			results = (Object[])result.iterator().next();
		}
		
		return results;
	}
	
	public void prefetchActivityFailureCounts(Collection<ProcessInstanceDAO> instances) {
		// do nothing; activity failures counts are already in a column 
	}
}
