/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ScopeStateEnum;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.bpel.evt.BpelEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A very simple, in-memory implementation of the {@link ScopeDAO} interface.
 */
class ScopeDaoImpl extends DaoBaseImpl implements ScopeDAO {
    private String _type;
    private ScopeStateEnum _state;
    private Map<String, XmlDataDAO> _variables = new HashMap<String, XmlDataDAO>();
    private Map<String, CorrelationSetDAO> _correlations = new HashMap<String, CorrelationSetDAO>();
    private Map<Integer, PartnerLinkDAO> _eprs = new HashMap<Integer, PartnerLinkDAO>();
    private ProcessInstanceDaoImpl _processInstance;
    private org.apache.ode.bpel.dao.ScopeDAO _parent;
    private Long _instanceId;
    private int _scopeModelId;

    /**
     * Constructor.
     * @param owner process instance owner
     * @param parent scope parent
     * @param type scope type (name)
     * @param scopeModelId
     */
    public ScopeDaoImpl(ProcessInstanceDaoImpl owner, ScopeDAO parent, String type, int scopeModelId) {
        _processInstance = owner;
        _parent = parent;
        _type = type;
        _instanceId = IdGen.newScopeId();
        _scopeModelId = scopeModelId;
    }


    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getCorrelationSet(java.lang.String)
     */
    public CorrelationSetDAO getCorrelationSet(String corrSetName) {
        CorrelationSetDAO corr = _correlations.get(corrSetName);

        if (corr == null) {
            corr = new CorrelationSetDaoImpl(corrSetName, this);
            _correlations.put(corrSetName, corr);
        }

        return corr;
    }

    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getParentScope()
     */
    public org.apache.ode.bpel.dao.ScopeDAO getParentScope() {
        return _parent;
    }

    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getProcessInstance()
     */
    public org.apache.ode.bpel.dao.ProcessInstanceDAO getProcessInstance() {
        return _processInstance;
    }

    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#setState(org.apache.ode.bpel.dao.ScopeStateEnum)
     */
    public void setState(org.apache.ode.bpel.dao.ScopeStateEnum state) {
        _state = state;
    }

    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getState()
     */
    public org.apache.ode.bpel.dao.ScopeStateEnum getState() {
        return _state;
    }

    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getName()
     */
    public String getName() {
        return _type;
    }

    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getVariable(java.lang.String)
     */
    public XmlDataDAO getVariable(String varName) {
        XmlDataDAO v = _variables.get(varName);

        if (v == null) {
            v = new XmlDataDaoImpl(this,varName);
            _variables.put(varName, v);
        }

        return v;
    }

    public Collection<CorrelationSetDAO> getCorrelationSets() {
        return _correlations.values();
    }

    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getScopeInstanceId()
     */
    public Long getScopeInstanceId() {
        return _instanceId;
    }

    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getModelId()
     */
    public int getModelId() {
        return _scopeModelId;
    }

    public Collection<ScopeDAO> getChildScopes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Collection<XmlDataDAO> getVariables() {
        return _variables.values();
    }

    public List<BpelEvent> listEvents(BpelEventFilter efilter) {
        // TODO: provide a better implementation.
        return new ArrayList<BpelEvent>();
    }

    public PartnerLinkDAO createPartnerLink(int plinkModelId, String pLinkName, String myRole, String partnerRole) {
        PartnerLinkDAOImpl eprImpl = new PartnerLinkDAOImpl();
        eprImpl.setPartnerLinkModelId(plinkModelId);
        eprImpl.setPartnerLinkName(pLinkName);
        eprImpl.setMyRoleName(myRole);
        eprImpl.setPartnerRoleName(partnerRole);
        _eprs.put(plinkModelId, eprImpl);
        return eprImpl;
    }

    public PartnerLinkDAO getPartnerLink(int modelId) {
        return _eprs.get(modelId);
    }

    public Collection<PartnerLinkDAO> getPartnerLinks() {
        return _eprs.values();
    }

}
