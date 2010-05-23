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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;

import javax.persistence.*;
import javax.xml.namespace.QName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
@Entity
@Table(name="ODE_PROCESS")
@NamedQueries({
    @NamedQuery(name="ActiveInstances", query="select i from ProcessInstanceDAOImpl as i where i._process = :process and i._state = :state"),
    @NamedQuery(name="InstanceByCKey", query="select cs._scope._processInstance from CorrelationSetDAOImpl as cs where cs._correlationKey = :ckey"),
    @NamedQuery(name="CorrelatorByKey", query="select c from CorrelatorDAOImpl as c where c._correlatorKey = :ckey and c._process = :process")
})
public class ProcessDAOImpl extends OpenJPADAO implements ProcessDAO {
    private static final Log __log = LogFactory.getLog(ProcessDAOImpl.class);

    @Id @Column(name="ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long _id;

    @Basic @Column(name="PROCESS_ID")
    private String _processId;
    @Basic @Column(name="PROCESS_TYPE")
    private String _processType;
    @Basic @Column(name="GUID")
    private String _guid;
    @Basic @Column(name="VERSION")
    private long _version;

    @OneToMany(targetEntity=CorrelatorDAOImpl.class,mappedBy="_process",fetch=FetchType.LAZY,cascade={CascadeType.ALL})
    private Collection<CorrelatorDAOImpl> _correlators = new ArrayList<CorrelatorDAOImpl>();

    public ProcessDAOImpl() {}
    public ProcessDAOImpl(QName pid, QName type, String guid, long version) {
        _processId = pid.toString();
        _processType = type.toString();
        _guid = guid;
        _version = version;
    }

    public Serializable getId() {
        return _id; 
    }
    
    public void setId(Long id) {
        _id = id;
    }

    public CorrelatorDAO addCorrelator(String correlator) {
        CorrelatorDAOImpl corr = new CorrelatorDAOImpl(correlator, this);
        _correlators.add(corr);
        return corr;
    }

    @SuppressWarnings("unchecked")
    public CorrelatorDAO getCorrelator(String correlatorId) {
        Query qry = getEM().createNamedQuery("CorrelatorByKey");
        qry.setParameter("ckey", correlatorId);
        qry.setParameter("process", this);
        List res = qry.getResultList();
        if (res.size() == 0) return null;
        return (CorrelatorDAO) res.get(0);
    }

    public ProcessInstanceDAO createInstance(CorrelatorDAO instantiatingCorrelator) {
        ProcessInstanceDAOImpl inst = new ProcessInstanceDAOImpl((CorrelatorDAOImpl)instantiatingCorrelator, this);
        getEM().persist(inst);
        return inst;
    }

    public ProcessInstanceDAO createInstance(CorrelatorDAO instantiatingCorrelator, MessageExchangeDAO mex) {
        ProcessInstanceDAOImpl inst = new ProcessInstanceDAOImpl((CorrelatorDAOImpl)instantiatingCorrelator, this);
        getEM().persist(inst);
        return inst;
    }

    @SuppressWarnings("unchecked")
    public Collection<ProcessInstanceDAO> findInstance(CorrelationKey ckey) {
        Query qry = getEM().createNamedQuery("InstanceByCKey");
        qry.setParameter("ckey", ckey.toCanonicalString());
        return qry.getResultList();
    }

    public ProcessInstanceDAO getInstance(Long iid) {
        return getEM().find(ProcessInstanceDAOImpl.class, iid);
    }

    public QName getProcessId() {
        return QName.valueOf(_processId);
    }

    public QName getType() {
        return QName.valueOf(_processType);
    }

    @SuppressWarnings("unchecked")
    public void deleteProcessAndRoutes() {
        // delete routes
        Collection instanceIds = getEM().createNamedQuery(ProcessInstanceDAOImpl.SELECT_INSTANCE_IDS_BY_PROCESS).setParameter("process", this).getResultList();
        batchUpdateByIds(instanceIds.iterator(), getEM().createNamedQuery(MessageRouteDAOImpl.DELETE_MESSAGE_ROUTES_BY_INSTANCE_IDS), "instanceIds");
        getEM().createNamedQuery(CorrelatorDAOImpl.DELETE_CORRELATORS_BY_PROCESS).setParameter("process", this).executeUpdate();

        deleteInstances(Integer.MAX_VALUE);
        
        // delete process dao
        getEM().remove(this); // This deletes CorrelatorDAO
        getEM().flush();
    }
    
    private int deleteInstances(int transactionSize) {
        if(__log.isDebugEnabled()) __log.debug("Cleaning up process data.");

        deleteEvents();
        deleteCorrelations();
        deleteMessages();
        deleteVariables();
        deleteProcessInstances();
        
        return 0;
    }

    @SuppressWarnings("unchecked")
    private void deleteProcessInstances() {
        Collection faultIds = getEM().createNamedQuery(ProcessInstanceDAOImpl.SELECT_FAULT_IDS_BY_PROCESS).setParameter("process", this).getResultList();
        batchUpdateByIds(faultIds.iterator(), getEM().createNamedQuery(FaultDAOImpl.DELETE_FAULTS_BY_IDS), "ids");
        Collection instanceIds = getEM().createNamedQuery(ProcessInstanceDAOImpl.SELECT_INSTANCE_IDS_BY_PROCESS).setParameter("process", this).getResultList();
        batchUpdateByIds(instanceIds.iterator(), getEM().createNamedQuery(ActivityRecoveryDAOImpl.DELETE_ACTIVITY_RECOVERIES_BY_IDS), "ids");
        getEM().createNamedQuery(ProcessInstanceDAOImpl.DELETE_INSTANCES_BY_PROCESS).setParameter("process", this).executeUpdate();
    }

    @SuppressWarnings("unchecked")
    private void deleteVariables() {
        Collection xmlDataIds = getEM().createNamedQuery(XmlDataDAOImpl.SELECT_XMLDATA_IDS_BY_PROCESS).setParameter("process", this).getResultList();
        batchUpdateByIds(xmlDataIds.iterator(), getEM().createNamedQuery(XmlDataProperty.DELETE_XML_DATA_PROPERTIES_BY_XML_DATA_IDS), "xmlDataIds");
        Collection scopeIds = getEM().createNamedQuery(ScopeDAOImpl.SELECT_SCOPE_IDS_BY_PROCESS).setParameter("process", this).getResultList();
        batchUpdateByIds(scopeIds.iterator(), getEM().createNamedQuery(XmlDataDAOImpl.DELETE_XMLDATA_BY_SCOPE_IDS), "scopeIds");

//      Collection scopeIds = getEM().createNamedQuery(ScopeDAOImpl.SELECT_SCOPE_IDS_BY_PROCESS).setParameter("process", this).getResultList();
        batchUpdateByIds(scopeIds.iterator(), getEM().createNamedQuery(PartnerLinkDAOImpl.DELETE_PARTNER_LINKS_BY_SCOPE_IDS), "scopeIds");
        batchUpdateByIds(scopeIds.iterator(), getEM().createNamedQuery(ScopeDAOImpl.DELETE_SCOPES_BY_SCOPE_IDS), "ids");
    }

    @SuppressWarnings("unchecked")
    private void deleteMessages() {
        getEM().createNamedQuery(MessageDAOImpl.DELETE_MESSAGES_BY_PROCESS).setParameter("process", this).executeUpdate();
        Collection mexIds = getEM().createNamedQuery(MessageExchangeDAOImpl.SELECT_MEX_IDS_BY_PROCESS).setParameter("process", this).getResultList();
        batchUpdateByIds(mexIds.iterator(), getEM().createNamedQuery(MexProperty.DELETE_MEX_PROPERTIES_BY_MEX_IDS), "mexIds");
        getEM().createNamedQuery(MessageExchangeDAOImpl.DELETE_MEXS_BY_PROCESS).setParameter("process", this).executeUpdate();
    }

    @SuppressWarnings("unchecked")
    private void deleteCorrelations() {
        Collection corrSetIds = getEM().createNamedQuery(CorrelationSetDAOImpl.SELECT_CORRELATION_SET_IDS_BY_PROCESS).setParameter("process", this).getResultList();
        batchUpdateByIds(corrSetIds.iterator(), getEM().createNamedQuery(CorrSetProperty.DELETE_CORSET_PROPERTIES_BY_PROPERTY_IDS), "corrSetIds");
        batchUpdateByIds(corrSetIds.iterator(), getEM().createNamedQuery(CorrelationSetDAOImpl.DELETE_CORRELATION_SETS_BY_IDS), "ids");
    }

    @SuppressWarnings("unchecked")
    private void deleteEvents() {
        Collection eventIds = getEM().createNamedQuery(EventDAOImpl.SELECT_EVENT_IDS_BY_PROCESS).setParameter("process", this).getResultList();
        batchUpdateByIds(eventIds.iterator(), getEM().createNamedQuery(EventDAOImpl.DELETE_EVENTS_BY_IDS), "ids");
    }

    public int getNumInstances() {
        Long instanceCount = (Long) getSingleResult(getEM().createNamedQuery(ProcessInstanceDAOImpl.COUNT_INSTANCE_IDS_BY_PROCESS).setParameter("process", this));
        return (instanceCount == null ? 0 : instanceCount.intValue());
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

    @SuppressWarnings("unchecked")
    public Collection<ProcessInstanceDAO> getActiveInstances() {
        Query qry = getEM().createNamedQuery("ActiveInstances");
        qry.setParameter("process", this);
        qry.setParameter("state", ProcessState.STATE_ACTIVE);
        return qry.getResultList();
    }
}