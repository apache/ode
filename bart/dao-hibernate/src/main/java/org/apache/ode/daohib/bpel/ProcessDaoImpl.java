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
package org.apache.ode.daohib.bpel;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HCorrelationSet;
import org.apache.ode.daohib.bpel.hobj.HCorrelator;
import org.apache.ode.daohib.bpel.hobj.HProcess;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Hibernate-based {@link ProcessDAO} implementation.
 */
class ProcessDaoImpl extends HibernateDao implements ProcessDAO {
    private static final String QRY_CORRELATOR = "where this.correlatorId = ?";

    private HProcess _process;

    public ProcessDaoImpl(SessionManager sm, HProcess process) {
        super(sm,process);
        _process = process;
    }

    public QName getProcessId() {
        return QName.valueOf(_process.getProcessId());
    }

    public ProcessInstanceDAO getInstance(Long iid) {
        ProcessInstanceDAO instance = BpelDAOConnectionImpl._getInstance(_sm, getSession(), iid);
        if (instance == null || !instance.getProcess().getProcessId().equals(getProcessId()))
            return null;
        return instance;
    }


    public CorrelatorDAO getCorrelator(String  corrId) {
        Iterator results;
        Query q = getSession().createFilter(_process.getCorrelators(),
                QRY_CORRELATOR);
        results = q.setString(0, corrId).iterate();

        if(!results.hasNext()){
            String msg = "no such correlator: corrId = " + corrId;
            throw new IllegalArgumentException(msg);
        }
        try {
            return new CorrelatorDaoImpl(_sm, (HCorrelator)results.next());
        } finally {
            Hibernate.close(results);
        }
    }

    public void removeRoutes(String routeId, ProcessInstanceDAO target) {
        for (HCorrelator hCorrelator : _process.getCorrelators()) {
            new CorrelatorDaoImpl(_sm, hCorrelator).removeRoutes(routeId, target);
        }
    }

    public ProcessInstanceDAO createInstance(CorrelatorDAO correlator) {
        HProcessInstance instance = new HProcessInstance();
        instance.setInstantiatingCorrelator((HCorrelator)((CorrelatorDaoImpl)correlator).getHibernateObj());
        instance.setProcess(_process);
        instance.setCreated(new Date());
        getSession().save(instance);
        _process.getInstances().add(instance);

        return new ProcessInstanceDaoImpl(_sm,instance);
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessDAO#findInstance(CorrelationKey)
     */
    @SuppressWarnings("unchecked")
    public Collection<ProcessInstanceDAO> findInstance(CorrelationKey ckeyValue) {

        Criteria criteria = getSession().createCriteria(HCorrelationSet.class);
        criteria.add(Expression.eq("scope.instance.process.id",_process.getId()));
        criteria.add(Expression.eq("value", ckeyValue.toCanonicalString()));
        criteria.addOrder(Order.desc("scope.instance.created"));
        return criteria.list();

    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessDAO#instanceCompleted(ProcessInstanceDAO)
     */
    public void instanceCompleted(ProcessInstanceDAO instance) {
        // nothing to do here (yet?)
    }

    public void delete() {
        getSession().delete(_process);
    }

    public QName getType() {
        return new QName(_process.getTypeNamespace(), _process.getTypeName());
    }

    public long getVersion() {
        return _process.getVersion();
    }

    public CorrelatorDAO addCorrelator(String corrid) {
        HCorrelator correlator = new HCorrelator();
        correlator.setCorrelatorId(corrid);
        correlator.setProcess(_process);
        correlator.setCreated(new Date());
        _process.getCorrelators().add(correlator);
        getSession().save(correlator);
        getSession().saveOrUpdate(_process);
        return new CorrelatorDaoImpl(_sm, correlator);
    }

	public int getNumInstances() {
		// TODO: Is this efficient? 
		return _process.getInstances().size();
	}

    public String getGuid() {
        return _process.getGuid();
    }

}
