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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.*;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.*;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.QNameUtils;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.UnaryFunction;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Hibernate-based {@link ProcessInstanceDAO} implementation.
 */
public class ProcessInstanceDaoImpl extends HibernateDao implements ProcessInstanceDAO {
    private static final Log __log = LogFactory.getLog(ProcessInstanceDaoImpl.class);

    /** Query for removing selectors. */
    private static final String QRY_DELSELECTORS = "delete from " + HCorrelatorSelector.class.getName() + " where instance = ?";

    private static final String QRY_VARIABLES = "from " + HXmlData.class.getName()
            + " as x where x.name = ? and x.scope.scopeModelId = ? and x.scope.instance.id = ?";

    private static final String QRY_RECOVERIES = "from " + HActivityRecovery.class.getName() + " AS x WHERE x.instance.id = ?";

    private HProcessInstance _instance;

    private ScopeDAO _root;

    public ProcessInstanceDaoImpl(SessionManager sm, HProcessInstance instance) {
        super(sm, instance);
        entering("ProcessInstanceDaoImpl.ProcessInstanceDaoImpl");
        _instance = instance;
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getCreateTime()
     */
    public Date getCreateTime() {
        return _instance.getCreated();
    }

    public void setFault(FaultDAO fault) {
      entering("ProcessInstanceDaoImpl.setFault");
        _instance.setFault(((FaultDAOImpl) fault)._self);
        getSession().update(_instance);

    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#setFault(javax.xml.namespace.QName, String, int, int, org.w3c.dom.Element)
     */
    public void setFault(QName name, String explanation, int lineNo, int activityId, Element faultData) {
      entering("ProcessInstanceDaoImpl.setFault");
        if (_instance.getFault() != null)
            getSession().delete(_instance.getFault());

        HFaultData fault = new HFaultData();
        fault.setName(QNameUtils.fromQName(name));
        fault.setExplanation(explanation);
        fault.setLineNo(lineNo);
        fault.setActivityId(activityId);
        if (faultData != null) {
            HLargeData ld = new HLargeData(DOMUtils.domToString(faultData));
            fault.setData(ld);
            getSession().save(ld);
        }

        _instance.setFault(fault);
        getSession().save(fault);
        getSession().update(_instance);
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getFault()
     */
    public FaultDAO getFault() {
      entering("ProcessInstanceDaoImpl.getFault");
        if (_instance.getFault() == null)
            return null;
        else
            return new FaultDAOImpl(_sm, _instance.getFault());
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getExecutionState()
     */
    public byte[] getExecutionState() {
      entering("ProcessInstanceDaoImpl.getExecutionSate");
        if (_instance.getJacobState() == null)
            return null;
        return _instance.getJacobState().getBinary();
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#setExecutionState(byte[])
     */
    public void setExecutionState(byte[] bytes) {
      entering("ProcessInstanceDaoImpl.setExecutionSate");
        if (_instance.getJacobState() != null)
            getSession().delete(_instance.getJacobState());
        if (bytes.length > 0) {
            HLargeData ld = new HLargeData(bytes);
            _instance.setJacobState(ld);
            getSession().save(ld);
        }
        getSession().update(_instance);
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getProcess()
     */
    public ProcessDAO getProcess() {
      entering("ProcessInstanceDaoImpl.getProcess");
        return new ProcessDaoImpl(_sm, _instance.getProcess());
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getRootScope()
     */
    public ScopeDAO getRootScope() {
        entering("ProcessInstanceDaoImpl.getRootScope");
        if (_root != null)
            return _root;
        Query rootQry = getSession().createFilter(_instance.getScopes(), "where this.parentScope is null");
        HScope hroot = (HScope) rootQry.uniqueResult();
        if (hroot == null)
            return null;
        return _root = new ScopeDaoImpl(_sm, hroot);
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#setState(short)
     */
    public void setState(short state) {
        entering("ProcessInstanceDaoImpl.setState");
        _instance.setPreviousState(_instance.getState());
        _instance.setState(state);
        if (state == ProcessState.STATE_TERMINATED) {
            clearSelectors();
        }
        getSession().update(_instance);
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getState()
     */
    public short getState() {
        return _instance.getState();
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getPreviousState()
     */
    public short getPreviousState() {
        return _instance.getPreviousState();
    }

    public ScopeDAO createScope(ScopeDAO parentScope, String name, int scopeModelId) {
        entering("ProcessInstanceDaoImpl.createScope");
        HScope scope = new HScope();
        scope.setParentScope(parentScope != null ? (HScope) ((ScopeDaoImpl) parentScope).getHibernateObj() : null);
        scope.setName(name);
        scope.setScopeModelId(scopeModelId);
        scope.setState(ScopeStateEnum.ACTIVE.toString());
        scope.setInstance(_instance);
        scope.setCreated(new Date());
        // _instance.getScopes().add(scope);
        getSession().save(scope);

        return new ScopeDaoImpl(_sm, scope);
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getInstanceId()
     */
    public Long getInstanceId() {
        return _instance.getId();
    }

    public ScopeDAO getScope(Long scopeInstanceId) {
        entering("ProcessInstanceDaoImpl.getScope");
        Long id = Long.valueOf(scopeInstanceId);
        HScope scope = (HScope) getSession().get(HScope.class, id);
        return scope != null ? new ScopeDaoImpl(_sm, scope) : null;
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getScopes(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Collection<ScopeDAO> getScopes(String scopeName) {
        entering("ProcessInstanceDaoImpl.getScopes");
        Collection<HScope> hscopes;
        if (scopeName != null) {
            Query filter = _sm.getSession().createFilter(_instance.getScopes(), "where this.name=?");
            filter.setString(0, scopeName);
            hscopes = filter.list();
        } else
            hscopes = _instance.getScopes();
        ArrayList<ScopeDAO> ret = new ArrayList<ScopeDAO>();
        CollectionsX.transform(ret, hscopes, new UnaryFunction<HScope, ScopeDAO>() {
            public ScopeDAO apply(HScope x) {
                return new ScopeDaoImpl(_sm, x);
            }
        });
        return ret;
    }

    public Collection<ScopeDAO> getScopes() {
        return getScopes(null);
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getInstantiatingCorrelator()
     */
    public CorrelatorDAO getInstantiatingCorrelator() {
        entering("ProcessInstanceDaoImpl.getInstantiatingCorrelator");
        return new CorrelatorDaoImpl(_sm, _instance.getInstantiatingCorrelator());
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getLastActiveTime()
     */
    public Date getLastActiveTime() {
        return _instance.getLastActiveTime();
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#setLastActiveTime(java.util.Date)
     */
    public void setLastActiveTime(Date dt) {
        entering("ProcessInstanceDaoImpl.setLastActiveTime");
        _instance.setLastActiveTime(dt);
    }

    public Set<CorrelationSetDAO> getCorrelationSets() {
        entering("ProcessInstanceDaoImpl.getCorrelationSets");
        Set<CorrelationSetDAO> results = new HashSet<CorrelationSetDAO>();

        for (HCorrelationSet hCorrelationSet : _instance.getCorrelationSets()) {
            results.add(new CorrelationSetDaoImpl(_sm, hCorrelationSet));
        }

        return results;
    }

    public CorrelationSetDAO getCorrelationSet(String name) {
        entering("ProcessInstanceDaoImpl.getCorrelationSet");
        for (HCorrelationSet hCorrelationSet : _instance.getCorrelationSets()) {
            if (hCorrelationSet.getName().equals(name))
                return new CorrelationSetDaoImpl(_sm, hCorrelationSet);
        }
        return null;
    }

    /**
     * TODO this is never used, except by test cases - should be removed
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getVariables(java.lang.String, int)
     */
    public XmlDataDAO[] getVariables(String variableName, int scopeModelId) {
        entering("ProcessInstanceDaoImpl.getVariables");
        List<XmlDataDAO> results = new ArrayList<XmlDataDAO>();

        Iterator iter;
        Query qry = getSession().createQuery(QRY_VARIABLES);
        qry.setString(0, variableName);
        qry.setInteger(1, scopeModelId);
        qry.setLong(2, _instance.getId());
        iter = qry.iterate();

        while (iter.hasNext()) {
            results.add(new XmlDataDaoImpl(_sm, (HXmlData) iter.next()));
        }
        Hibernate.close(iter);

        return results.toArray(new XmlDataDAO[results.size()]);
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#finishCompletion()
     */
    public void finishCompletion() {
        entering("ProcessInstanceDaoImpl.finishCompletion");
        // make sure we have completed.
        assert (ProcessState.isFinished(this.getState()));
        // let our process know that we've done our work.
        this.getProcess().instanceCompleted(this);
    }

    public boolean delete(Set<CLEANUP_CATEGORY> cleanupCategories) {
        entering("ProcessInstanceDaoImpl.delete");
        boolean instanceDeleted = false;
        
        if(__log.isDebugEnabled()) __log.debug("Cleaning up instance data with categories = " + cleanupCategories);
        
        if( _instance.getJacobState() != null ) {
            getSession().delete(_instance.getJacobState());
            _instance.setJacobState(null);
        }
        
        if( cleanupCategories.contains(CLEANUP_CATEGORY.EVENTS) ) {
            deleteEvents();
        }
        
        if( cleanupCategories.contains(CLEANUP_CATEGORY.CORRELATIONS) ) {
            deleteCorrelations();
        }
        
        if( cleanupCategories.contains(CLEANUP_CATEGORY.MESSAGES) ) {
            deleteMessages();
        }
        
        if( cleanupCategories.contains(CLEANUP_CATEGORY.VARIABLES) ) {
            deleteVariables();
        }
        
        if( cleanupCategories.contains(CLEANUP_CATEGORY.INSTANCE) ) {
            deleteInstance();
            instanceDeleted = true;
        }

        getSession().flush();
        
        if(__log.isDebugEnabled()) __log.debug("Instance data cleaned up and flushed.");
        
        return instanceDeleted;
    }
    
    private void deleteInstance() {
        getSession().getNamedQuery(HLargeData.DELETE_FAULT_LDATA_BY_INSTANCE_ID).setParameter ("instanceId", _instance.getId()).executeUpdate();
        getSession().getNamedQuery(HFaultData.DELETE_FAULTS_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();

        getSession().delete(_instance); // this deletes JcobState, HActivityRecovery -> ActivityRecovery-LData
    }
     
    private void deleteVariables() {
        getSession().getNamedQuery(HCorrelationProperty.DELETE_CORPROPS_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();
        getSession().getNamedQuery(HCorrelationSet.DELETE_CORSETS_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();

        getSession().getNamedQuery(HVariableProperty.DELETE_VARIABLE_PROPERITES_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();
        getSession().getNamedQuery(HLargeData.DELETE_XMLDATA_LDATA_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();
        getSession().getNamedQuery(HXmlData.DELETE_XMLDATA_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();

        getSession().getNamedQuery(HLargeData.DELETE_PARTNER_LINK_LDATA_BY_INSTANCE).setParameter ("instance", _instance).setParameter ("instance2", _instance).executeUpdate();
        getSession().getNamedQuery(HPartnerLink.DELETE_PARTNER_LINKS_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();

        getSession().getNamedQuery(HScope.DELETE_SCOPES_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();
    }

    @SuppressWarnings("unchecked")
    private void deleteMessages() {
        // there are chances that some unmatched messages are still there
        getSession().getNamedQuery(HLargeData.DELETE_UNMATCHED_MESSAGE_LDATA_BY_INSTANCE).setParameter("instance", _instance).executeUpdate();
        Collection unmatchedMex = getSession().getNamedQuery(HMessageExchange.SELECT_UNMATCHED_MEX_BY_INSTANCE).setParameter("instance", _instance).list();
        if( !unmatchedMex.isEmpty() ) {
            getSession().getNamedQuery(HMessageExchange.DELETE_UNMATCHED_MEX).setParameter("mex", unmatchedMex).executeUpdate();
        }
        getSession().getNamedQuery(HCorrelatorMessage.DELETE_CORMESSAGES_BY_INSTANCE).setParameter("instance", _instance).executeUpdate();

        getSession().getNamedQuery(HCorrelatorSelector.DELETE_MESSAGE_ROUTES_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();
    }
    
    private void deleteCorrelations() {
        getSession().getNamedQuery(HCorrelationProperty.DELETE_CORPROPS_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();
        getSession().getNamedQuery(HCorrelationSet.DELETE_CORSETS_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();
    }

    private void deleteEvents() {
        getSession().getNamedQuery(HLargeData.DELETE_EVENT_LDATA_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();
        getSession().getNamedQuery(HBpelEvent.DELETE_EVENTS_BY_INSTANCE).setParameter ("instance", _instance).executeUpdate();      
    }

    public void insertBpelEvent(ProcessInstanceEvent event) {
        entering("ProcessInstanceDaoImpl.insertBpelEvent");
        // Defer to the BpelDAOConnectionImpl
        BpelDAOConnectionImpl._insertBpelEvent(_sm.getSession(), event, this.getProcess(), this);
    }

    public EventsFirstLastCountTuple getEventsFirstLastCount() {
        entering("ProcessInstanceDaoImpl.getEventsFirstLastCount");
        // Using a criteria, find the min,max, and count of event tstamps.
        Criteria c = _sm.getSession().createCriteria(HBpelEvent.class);
        c.add(Restrictions.eq("instance", _instance));
        c.setProjection(Projections.projectionList().add(Projections.min("tstamp")).add(Projections.max("tstamp")).add(
                Projections.count("tstamp")));

        Object[] ret = (Object[]) c.uniqueResult();
        EventsFirstLastCountTuple flc = new EventsFirstLastCountTuple();
        flc.first = (Date) ret[0];
        flc.last = (Date) ret[1];
        flc.count = (Integer) ret[2];
        return flc;
    }

    public long genMonotonic() {
        entering("ProcessInstanceDaoImpl.genMonotonic");
        long seq = _instance.getSequence() + 1;
        _instance.setSequence(seq);
        return seq;
    }

    protected void clearSelectors() {
        entering("ProcessInstanceDaoImpl.clearSelectors");
        Query q = getSession().createQuery(QRY_DELSELECTORS);
        q.setEntity(0, _instance);
        q.executeUpdate();
    }

    public int getActivityFailureCount() {
        return _instance.getActivityFailureCount();
    }

    public Date getActivityFailureDateTime() {
        return _instance.getActivityFailureDateTime();
    }

    public Collection<ActivityRecoveryDAO> getActivityRecoveries() {
        entering("ProcessInstanceDaoImpl.getActivityRecoveries");
        List<ActivityRecoveryDAO> results = new ArrayList<ActivityRecoveryDAO>();
        Query qry = getSession().createQuery(QRY_RECOVERIES);
        qry.setLong(0, _instance.getId());
        Iterator iter = qry.iterate();
        while (iter.hasNext())
            results.add(new ActivityRecoveryDaoImpl(_sm, (HActivityRecovery) iter.next()));
        Hibernate.close(iter);
        return results;
    }

    public void createActivityRecovery(String channel, long activityId, String reason, Date dateTime, Element data,
            String[] actions, int retries) {
        entering("ProcessInstanceDaoImpl.createActivityRecovery");
        HActivityRecovery recovery = new HActivityRecovery();
        recovery.setInstance(_instance);
        recovery.setChannel(channel);
        recovery.setActivityId(activityId);
        recovery.setReason(reason);
        recovery.setDateTime(dateTime);
        recovery.setRetries(retries);
        if (data != null) {
            HLargeData ld = new HLargeData(DOMUtils.domToString(data));
            recovery.setDetails(ld);
            getSession().save(ld);
        }
        String list = actions[0];
        for (int i = 1; i < actions.length; ++i)
            list += " " + actions[i];
        recovery.setActions(list);
        //_instance.getActivityRecoveries().add(recovery);
        getSession().save(recovery);
        _instance.setActivityFailureDateTime(dateTime);
        _instance.setActivityFailureCount(_instance.getActivityFailureCount() + 1);
        getSession().update(_instance);
    }

    /**
     * Delete previously registered activity recovery.
     */
    public void deleteActivityRecovery(String channel) {
        entering("ProcessInstanceDaoImpl.deleteActivityRecovery");
        for (HActivityRecovery recovery : _instance.getActivityRecoveries()) {
            if (recovery.getChannel().equals(channel)) {
                getSession().delete(recovery);
                _instance.setActivityFailureCount(_instance.getActivityFailureCount() - 1);
                getSession().update(_instance);
                return;
            }
        }
    }

    public BpelDAOConnection getConnection() {
        entering("ProcessInstanceDaoImpl.getConnection");
        return new BpelDAOConnectionImpl(_sm);
    }

    public int getExecutionStateCounter() {
        return _instance.getExecutionStateCounter();
    }

    public void setExecutionStateCounter(int stateCounter) {
        _instance.setExecutionStateCounter(stateCounter);

    }

}
