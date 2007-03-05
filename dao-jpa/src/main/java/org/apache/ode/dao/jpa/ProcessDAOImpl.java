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

package org.apache.ode.dao.jpa;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name="ODE_PROCESS")
@NamedQueries({
    @NamedQuery(name="InstanceByCKey", query="SELECT cs._scope._instance FROM CorrelationSetDAOImpl as cs WHERE cs._correlationKey = :ckey"),
    @NamedQuery(name="CorrelatorByKey", query="SELECT c FROM CorrelatorDAOImpl as c WHERE c._correlatorKey = :ckey")
})
public class ProcessDAOImpl implements ProcessDAO {

    @PersistenceContext
    private EntityManager _em;

    @Id @Column(name="ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long _id;

    @Basic @Column(name="PROCESS_ID")
    private String _processId;
	@Basic @Column(name="NUMBER_OF_INSTANCES")
    private int _numInstances;
	@Basic @Column(name="PROCESS_TYPE")
    private String _processType;
	@Basic @Column(name="GUID")
    private String _guid;
	@Basic @Column(name="VERSION")
    private long _version;

	@OneToMany(targetEntity=CorrelatorDAOImpl.class,mappedBy="_process",fetch=FetchType.LAZY,cascade={CascadeType.ALL})
    private Collection<CorrelatorDAOImpl> _correlators = new ArrayList<CorrelatorDAOImpl>();

    @Transient
    transient private BPELDAOConnectionImpl _connection;

	public ProcessDAOImpl() {}
	public ProcessDAOImpl(QName pid, QName type, String guid, BPELDAOConnectionImpl connection, long version) {
        _processId = pid.toString();
		_processType = type.toString();
		_connection = connection;
		_guid = guid;
        _version = version;
    }
	
	public void addCorrelator(String correlator) {
		CorrelatorDAOImpl corr = new CorrelatorDAOImpl(correlator);
		_correlators.add(corr);
	}

    public CorrelatorDAO getCorrelator(String correlatorId) {
        Query qry = _connection.getEntityManager().createNamedQuery("CorrelatorByKey");
        qry.setParameter("ckey", correlatorId);
        List res = qry.getResultList();
        if (res.size() == 0) return null;
        return (CorrelatorDAO) res.get(0);
    }

    public ProcessInstanceDAO createInstance(
			CorrelatorDAO instantiatingCorrelator) {
		ProcessInstanceDAOImpl inst = new ProcessInstanceDAOImpl((CorrelatorDAOImpl)instantiatingCorrelator, this,_connection);
		_connection.getEntityManager().persist(inst);
		_numInstances++;
		return inst;
	}

	@SuppressWarnings("unchecked")
    public Collection<ProcessInstanceDAO> findInstance(CorrelationKey ckey) {
		Query qry = _connection.getEntityManager().createNamedQuery("InstanceByCKey");
        qry.setParameter("ckey", ckey.toCanonicalString());
        return qry.getResultList();
	}

	public ProcessInstanceDAO getInstance(Long iid) {
		return _connection.getInstance(iid);
	}

	public QName getProcessId() {
		return QName.valueOf(_processId);
	}

	public QName getType() {
		return QName.valueOf(_processType);
	}

    public void delete() {
        _connection.removeProcess(this);
    }

    public int getNumInstances() {
        return _numInstances;
    }

    public long getVersion() {
        return _version;
    }

    public void instanceCompleted(ProcessInstanceDAO instance) {
        // nothing to do here (yet?)
    }

    public void removeRoutes(String routeId, ProcessInstanceDAO target) {
        for (CorrelatorDAO c : _correlators) {
            ((CorrelatorDAOImpl)c).removeLocalRoutes(routeId, target);
        }
    }

    public String getGuid() {
        return _guid;
    }

    void setConnection(BPELDAOConnectionImpl connection) {
        _connection = connection;
    }
}
