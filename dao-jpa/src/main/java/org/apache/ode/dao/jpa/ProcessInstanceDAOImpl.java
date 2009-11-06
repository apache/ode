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
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.ActivityRecoveryDAO;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ScopeStateEnum;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.w3c.dom.Element;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="ODE_PROCESS_INSTANCE")
@NamedQueries({
     @NamedQuery(name=ProcessInstanceDAOImpl.DELETE_INSTANCES_BY_PROCESS, query="delete from ProcessInstanceDAOImpl as i where i._process = :process"),
     @NamedQuery(name=ProcessInstanceDAOImpl.SELECT_INSTANCE_IDS_BY_PROCESS, query="select i._instanceId from ProcessInstanceDAOImpl as i where i._process = :process"),
     @NamedQuery(name=ProcessInstanceDAOImpl.COUNT_INSTANCE_IDS_BY_PROCESS, query="select count(i._instanceId) from ProcessInstanceDAOImpl as i where i._process = :process"),
     @NamedQuery(name=ProcessInstanceDAOImpl.SELECT_FAULT_IDS_BY_PROCESS, query="select i._faultId from ProcessInstanceDAOImpl as i where i._process = :process and i._faultId is not null"),
    @NamedQuery(name=ProcessInstanceDAOImpl.COUNT_FAILED_INSTANCES_BY_STATUS_AND_PROCESS_ID, 
            query="select count(i._instanceId), max(i._lastRecovery) from ProcessInstanceDAOImpl as i where i._process._processId = :processId and i._state in(:states) and exists(select r from ActivityRecoveryDAOImpl r where i = r._instance)")
})
public class ProcessInstanceDAOImpl extends OpenJPADAO implements ProcessInstanceDAO {
    private static final Log __log = LogFactory.getLog(ProcessInstanceDAOImpl.class);
    
    public final static String DELETE_INSTANCES_BY_PROCESS = "DELETE_INSTANCES_BY_PROCESS";
     public final static String SELECT_INSTANCE_IDS_BY_PROCESS = "SELECT_INSTANCE_IDS_BY_PROCESS";
     public final static String COUNT_INSTANCE_IDS_BY_PROCESS = "COUNT_INSTANCE_IDS_BY_PROCESS";

     public final static String SELECT_FAULT_IDS_BY_PROCESS = "SELECT_FAULT_IDS_BY_PROCESS";
    public final static String COUNT_FAILED_INSTANCES_BY_STATUS_AND_PROCESS_ID = "COUNT_FAILED_INSTANCES_BY_STATUS_AND_PROCESS_ID";
    
    @Id @Column(name="ID")
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long _instanceId;
    @Basic @Column(name="LAST_RECOVERY_DATE")
    private Date _lastRecovery;
    @Basic @Column(name="LAST_ACTIVE_TIME")
    private Date _lastActive;
    @Basic @Column(name="INSTANCE_STATE")
    private short _state;
    @Basic @Column(name="PREVIOUS_STATE")
    private short _previousState;
    @Lob @Column(name="EXECUTION_STATE")
    private byte[] _executionState;
    @Basic @Column(name="SEQUENCE")
    private long _sequence;
    @Basic @Column(name="DATE_CREATED")
    private Date _dateCreated = new Date();
    
    @OneToOne(fetch=FetchType.LAZY,cascade={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}) @Column(name="ROOT_SCOPE_ID")
    private ScopeDAOImpl _rootScope;
    @OneToMany(targetEntity=ScopeDAOImpl.class,mappedBy="_processInstance",fetch=FetchType.LAZY,cascade={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Collection<ScopeDAO> _scopes = new ArrayList<ScopeDAO>();
    @OneToMany(targetEntity=ActivityRecoveryDAOImpl.class,mappedBy="_instance",fetch=FetchType.LAZY,cascade={CascadeType.ALL})
    private Collection<ActivityRecoveryDAO> _recoveries = new ArrayList<ActivityRecoveryDAO>();

    @SuppressWarnings("unused")
    @Basic @Column(name="FAULT_ID", insertable=false, updatable=false, nullable=true)
    private long _faultId;
    @OneToOne(fetch=FetchType.LAZY,cascade={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}) @Column(name="FAULT_ID")
    private FaultDAOImpl _fault;
    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PROCESS_ID")
    private ProcessDAOImpl _process;
    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="INSTANTIATING_CORRELATOR_ID")
    private CorrelatorDAOImpl _instantiatingCorrelator;

    @OneToMany(targetEntity=MessageExchangeDAOImpl.class,mappedBy="_processInst",fetch=FetchType.LAZY)
    @SuppressWarnings("unused")
    private Collection<MessageExchangeDAO> _messageExchanges = new ArrayList<MessageExchangeDAO>();
    
    private transient int _activityFailureCount = -1;
    
    public ProcessInstanceDAOImpl() {}
    public ProcessInstanceDAOImpl(CorrelatorDAOImpl correlator, ProcessDAOImpl process) {
        _instantiatingCorrelator = correlator;
        _process = process;
    }

    public void createActivityRecovery(String channel, long activityId,
            String reason, Date dateTime, Element data, String[] actions,
            int retries) {
        ActivityRecoveryDAOImpl ar = new ActivityRecoveryDAOImpl(channel, activityId, reason, dateTime, data, actions, retries);
        _recoveries.add(ar);
        ar.setInstance(this);
        _lastRecovery = dateTime;
    }

    public ScopeDAO createScope(ScopeDAO parentScope, String name, int scopeModelId) {
        ScopeDAOImpl ret = new ScopeDAOImpl((ScopeDAOImpl)parentScope,name,scopeModelId,this);
        ret.setState(ScopeStateEnum.ACTIVE);
        _scopes.add(ret);
        _rootScope = (parentScope == null)?ret:_rootScope;
        
        // Must persist the scope to generate a scope ID
        getEM().persist(ret);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public Collection<CorrelationSetDAO> selectCorrelationSets(Collection<ProcessInstanceDAO> instances) {
        return getEM().createNamedQuery(CorrelationSetDAOImpl.SELECT_CORRELATION_SETS_BY_INSTANCES).setParameter("instances", instances).getResultList();
    }
    
    public void delete(Set<CLEANUP_CATEGORY> cleanupCategories) {
        delete(cleanupCategories, true);
    }

    public void delete(Set<CLEANUP_CATEGORY> cleanupCategories, boolean deleteMyRoleMex) {
        if(__log.isDebugEnabled()) __log.debug("Cleaning up instance Data with " + cleanupCategories);
        
        // remove jacob state
        setExecutionState(null);
        if (getEM() != null) {
            if( !cleanupCategories.isEmpty() ) {
                // by default, we do not flush before select; flush it, so we can delete no matter if an entity is loaded up
                // or not; more importantly, OpenJPA will secretly load from the entire table if some entities reside only
                // in memory
                getEM().flush();
            }
            
            if (cleanupCategories.contains(CLEANUP_CATEGORY.EVENTS)) {
                deleteEvents();
            }
            if (cleanupCategories.contains(CLEANUP_CATEGORY.CORRELATIONS)) {
                deleteCorrelations();
            }
            if( cleanupCategories.contains(CLEANUP_CATEGORY.MESSAGES) ) {
                deleteMessageRoutes();
            }
            if (cleanupCategories.contains(CLEANUP_CATEGORY.VARIABLES)) {
                deleteVariables();
            }
            if (cleanupCategories.contains(CLEANUP_CATEGORY.INSTANCE)) {
                deleteInstance();
            }

            getEM().flush();
        }
    }
    
    private void deleteInstance() {
        if( _fault != null ) {
            getEM().remove(_fault);
        }
        getEM().remove(this); // This deletes ActivityRecoveryDAO 
    }
    
    @SuppressWarnings("unchecked")
    private void deleteVariables() {
        Collection xmlDataIds = getEM().createNamedQuery(XmlDataDAOImpl.SELECT_XMLDATA_IDS_BY_INSTANCE).setParameter("instance", this).getResultList();
        batchUpdateByIds(xmlDataIds.iterator(), getEM().createNamedQuery(XmlDataProperty.DELETE_XML_DATA_PROPERTIES_BY_XML_DATA_IDS), "xmlDataIds");
        Collection scopeIds = getEM().createNamedQuery(ScopeDAOImpl.SELECT_SCOPE_IDS_BY_INSTANCE).setParameter("instance", this).getResultList();
        batchUpdateByIds(scopeIds.iterator(), getEM().createNamedQuery(XmlDataDAOImpl.DELETE_XMLDATA_BY_SCOPE_IDS), "scopeIds");

        batchUpdateByIds(scopeIds.iterator(), getEM().createNamedQuery(PartnerLinkDAOImpl.DELETE_PARTNER_LINKS_BY_SCOPE_IDS), "scopeIds");
        batchUpdateByIds(scopeIds.iterator(), getEM().createNamedQuery(ScopeDAOImpl.DELETE_SCOPES_BY_SCOPE_IDS), "ids");
    }

    private void deleteMessageRoutes() {
        getEM().createNamedQuery(MessageRouteDAOImpl.DELETE_MESSAGE_ROUTES_BY_INSTANCE).setParameter ("instance", this).executeUpdate();
    }
    
    @SuppressWarnings("unchecked")
    private void deleteCorrelations() {
        Collection corrSetIds = getEM().createNamedQuery(CorrelationSetDAOImpl.SELECT_CORRELATION_SET_IDS_BY_INSTANCE).setParameter("instance", this).getResultList();
        batchUpdateByIds(corrSetIds.iterator(), getEM().createNamedQuery(CorrSetProperty.DELETE_CORSET_PROPERTIES_BY_PROPERTY_IDS), "corrSetIds");
        batchUpdateByIds(corrSetIds.iterator(), getEM().createNamedQuery(CorrelationSetDAOImpl.DELETE_CORRELATION_SETS_BY_IDS), "ids");
    }

    private void deleteEvents() {
        getEM().createNamedQuery(EventDAOImpl.DELETE_EVENTS_BY_INSTANCE).setParameter ("instance", this).executeUpdate();
    }
    
    public void deleteActivityRecovery(String channel) {
        ActivityRecoveryDAOImpl toRemove = null;
        for (ActivityRecoveryDAO _recovery : _recoveries) {
            ActivityRecoveryDAOImpl arElement = (ActivityRecoveryDAOImpl) _recovery;
            if (arElement.getChannel().equals(channel)) {
                toRemove = arElement;
                break;
            }
        }
        if (toRemove != null) {
            getEM().remove(toRemove);
            _recoveries.remove(toRemove);
        }

    }
    
    public void finishCompletion() {
        // make sure we have completed.
        assert (ProcessState.isFinished(this.getState()));
        // let our process know that we've done our work.
    }

    public long genMonotonic() {
        return _sequence++;
    }

    public int getActivityFailureCount() {
        if( _activityFailureCount == -1 ) {
            _activityFailureCount = _recoveries.size();
        }
        
        return _activityFailureCount;
    }
    
    public void setActivityFailureCount(int activityFailureCount) {
        _activityFailureCount = activityFailureCount;
    }

    public Date getActivityFailureDateTime() {
        return _lastRecovery;
    }

    public Collection<ActivityRecoveryDAO> getActivityRecoveries() {
        return _recoveries;
    }

    public CorrelationSetDAO getCorrelationSet(String name) {
        //    TODO: should this method be deprecated?
        
        //  Its not clear where the correlation set for the process is used
        //  or populated.
        
        throw new UnsupportedOperationException();
        
        //return null;
    }

    public Set<CorrelationSetDAO> getCorrelationSets() {
        //    TODO: should this method be deprecated?
        //  Its not clear where the correlation set for the process is used
        //  or populated.
        return new HashSet<CorrelationSetDAO>();
    }

    public Date getCreateTime() {
        return _dateCreated;
    }

    public EventsFirstLastCountTuple getEventsFirstLastCount() {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getExecutionState() {
        return _executionState;
    }

    public FaultDAO getFault() {
        return _fault;
    }

    public Long getInstanceId() {
        return _instanceId;
    }

    public CorrelatorDAO getInstantiatingCorrelator() {
        return _instantiatingCorrelator;
    }

    public Date getLastActiveTime() {
        return _lastActive;
    }

    public short getPreviousState() {
        return _previousState;
    }

    public ProcessDAO getProcess() {
        return _process;
    }

    public ScopeDAO getRootScope() {
        return _rootScope;
    }

    public ScopeDAO getScope(Long scopeInstanceId) {
        return getEM().find(ScopeDAOImpl.class, scopeInstanceId);
    }

    public Collection<ScopeDAO> getScopes(String scopeName) {
        Collection<ScopeDAO> ret = new ArrayList<ScopeDAO>();
        
        for (ScopeDAO sElement : _scopes) {
            if ( sElement.getName().equals(scopeName)) ret.add(sElement);
        }
        return ret;
    }

    public Collection<ScopeDAO> getScopes() {
        return _scopes;
    }

    public short getState() {
        return _state;
    }

    public XmlDataDAO[] getVariables(String variableName, int scopeModelId) {
        
        //TODO: This method is not used and should be considered a deprecation candidate.
        
        List<XmlDataDAO> results = new ArrayList<XmlDataDAO>();
        
        for (ScopeDAO sElement : _scopes) {
            if ( sElement.getModelId() == scopeModelId) {
                XmlDataDAO var = sElement.getVariable(variableName);
                if ( var != null ) results.add(var);
            }
        }
        return results.toArray(new XmlDataDAO[results.size()]);
    }

    public void insertBpelEvent(ProcessInstanceEvent event) {
        getConn().insertBpelEvent(event, getProcess(), this);
    }

    public void setExecutionState(byte[] execState) {
        _executionState = execState;
    }

    public void setFault(FaultDAO fault) {
        _fault = (FaultDAOImpl)fault;
    }

    public void setFault(QName faultName, String explanation, int faultLineNo,
            int activityId, Element faultMessage) {
        _fault = new FaultDAOImpl(faultName,explanation,faultLineNo,activityId,faultMessage);
    }

    public void setLastActiveTime(Date dt) {
        _lastActive = dt;
    }

    public void setState(short state) {
        _previousState = _state;
        _state = state;
    }
    
    void removeRoutes(String routeGroupId) {
        _process.removeRoutes(routeGroupId, this);
    }

    public BpelDAOConnection getConnection() {
        return new BPELDAOConnectionImpl(getEM());
    }
    
    public Collection<String> getMessageExchangeIds() {
        Collection<String> c = new HashSet<String>();
        for (MessageExchangeDAO m : _messageExchanges) {
            c.add(m.getMessageExchangeId());
        }
        return c;
    }    
}
