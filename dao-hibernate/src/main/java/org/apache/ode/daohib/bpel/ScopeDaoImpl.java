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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ScopeStateEnum;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HBpelEvent;
import org.apache.ode.daohib.bpel.hobj.HCorrelationSet;
import org.apache.ode.daohib.bpel.hobj.HPartnerLink;
import org.apache.ode.daohib.bpel.hobj.HScope;
import org.apache.ode.daohib.bpel.hobj.HXmlData;
import org.apache.ode.utils.SerializableUtils;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.UnaryFunction;
import org.apache.ode.utils.stl.UnaryFunctionEx;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

/**
 * Hibernate-based {@link ScopeDAO} implementation.
 */
public class ScopeDaoImpl extends HibernateDao implements ScopeDAO {


    private static final String QRY_VARIABLE = "from " + HXmlData.class.getName() +
            " as x where x.name = ? and x.scope.id = ?";

    private static final String QRY_CSET = "from " + HCorrelationSet.class.getName() +
            " as c where c.name = ? and c.scope.id = ?";

    private static final String QRY_SCOPE_EPR = "from " + HPartnerLink.class.getName() +
            " as e where e.modelId = ? and e.scope = ?";

    private HScope _scope;

    private HashMap<String,XmlDataDAO> _variables = new HashMap<String,XmlDataDAO>();

    public ScopeDaoImpl(SessionManager sm, HScope scope) {
        super(sm, scope);
        entering("ScopeDaoImpl.ScopeDaoImpl");
        _scope = scope;
    }

    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getCorrelationSet(java.lang.String)
     */
    public CorrelationSetDAO getCorrelationSet(String corrSetName) {
        entering("ScopeDaoImpl.getCorrelationSet");
        Query qry = getSession().createQuery(QRY_CSET);
        qry.setString(0,corrSetName);
        qry.setLong(1,_scope.getId());
        HCorrelationSet cs;
        List res = qry.list();

        if(res.size() == 0){
            // if it doesn't exist, we make it
            cs = new HCorrelationSet(_scope, corrSetName);
//            _scope.addCorrelationSet(cs);
            getSession().save(cs);
        } else {
            cs = (HCorrelationSet)res.get(0);
        }
        return new CorrelationSetDaoImpl(_sm, cs);
    }
    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getParentScope()
     */
    public ScopeDAO getParentScope() {
        entering("ScopeDaoImpl.getParentScope");
        return _scope.getParentScope() != null
                ? new ScopeDaoImpl(_sm, _scope.getParentScope())
                : null;
    }
    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getProcessInstance()
     */
    public ProcessInstanceDAO getProcessInstance() {
        entering("ScopeDaoImpl.getProcessInstance");
        return new ProcessInstanceDaoImpl(_sm, _scope.getInstance());
    }
    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#setState(org.apache.ode.bpel.dao.ScopeStateEnum)
     */
    public void setState(ScopeStateEnum state) {
        entering("ScopeDaoImpl.setState");
        _scope.setState(state.toString());
        getSession().update(_scope);
    }
    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getState()
     */
    public ScopeStateEnum getState() {
        return ScopeStateEnum.valueOf(_scope.getState());
    }
    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getName()
     */
    public String getName() {
        return _scope.getName();
    }
    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getVariable(java.lang.String)
     */
    public XmlDataDAO getVariable(String varName) {
        entering("ScopeDaoImpl.getVariable");
        XmlDataDAO cached = _variables.get(varName);
        if (cached != null) return _variables.get(varName);

        HXmlData data = null;
        for (HXmlData e : _scope.getVariables()) {
            if (e.getName().equals(varName) && e.getScope().getId().equals(_scope.getId())) {
                data = e;
            }
        }
        if (data == null) {
            data = new HXmlData();
            data.setName(varName);
            data.setScope(_scope);
            _scope.getVariables().add(data);
        }

        XmlDataDaoImpl varDao = new XmlDataDaoImpl(_sm, data);
        _variables.put(varName, varDao);
        return varDao;
    }

    public PartnerLinkDAO createPartnerLink(int modelId, String pLinkName, String myRole, String partnerRole) {
        entering("ScopeDaoImpl.createPartnerLink");
        HPartnerLink epr = new HPartnerLink();
        epr.setModelId(modelId);
        epr.setLinkName(pLinkName);
        epr.setMyRole(myRole);
        epr.setPartnerRole(partnerRole);
        epr.setScope(_scope);
//        _scope.addPartnerLink(epr);
        try {
            getSession().save(epr);
        } catch (Exception e) {
            super.logDao.error("Error while saving Partner Link : " + epr.toString());
            throw new RuntimeException(e);
        }
        PartnerLinkDAOImpl eprDao = new PartnerLinkDAOImpl(_sm, epr);
        return eprDao;
    }

    public PartnerLinkDAO getPartnerLink(int plinkId) {
        entering("ScopeDaoImpl.getPartnerLink");
        Query qry = getSession().createQuery(QRY_SCOPE_EPR);
        qry.setInteger(0,plinkId);
        qry.setEntity(1,_scope);
        HPartnerLink hpl = (HPartnerLink) qry.uniqueResult();
        if (hpl == null)
            return null;
        return new PartnerLinkDAOImpl(_sm, hpl);
    }

    public Collection<PartnerLinkDAO> getPartnerLinks() {
        entering("ScopeDaoImpl.getPartnerLinks");
        ArrayList<PartnerLinkDAO> plinks = new ArrayList<PartnerLinkDAO>();
        for (HPartnerLink hPartnerLink : _scope.getPartnerLinks()) {
            plinks.add(new PartnerLinkDAOImpl(_sm, hPartnerLink));
        }
        return plinks;
    }

    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getScopeInstanceId()
     */
    public Long getScopeInstanceId() {
        return _scope.getId();
    }

    /**
     * @see org.apache.ode.bpel.dao.ScopeDAO#getModelId()
     */
    public int getModelId() {
        return _scope.getScopeModelId();
    }

    public Set<CorrelationSetDAO> getCorrelationSets() {
        entering("ScopeDaoImpl.getCorrelationSets");
        Set<CorrelationSetDAO> results = new HashSet<CorrelationSetDAO>();
        for (HCorrelationSet hCorrelationSet : _scope.getCorrelationSets()) {
            results.add(new CorrelationSetDaoImpl(_sm, hCorrelationSet));
        }
        return results;
    }


    @SuppressWarnings("unchecked")
    public Collection<ScopeDAO> getChildScopes() {
        entering("ScopeDaoImpl.getChildScopes");
        Query q = getSession().createQuery("from " + HScope.class.getName() + " as x where x.parentScope=?");
        q.setEntity(0, _scope);
        Collection<HScope> hscopes = q.list();
        return CollectionsX.transform(new LinkedList<ScopeDAO>(), hscopes, new UnaryFunction<HScope,ScopeDAO>() {
            public ScopeDAO apply(HScope x) {
                return new ScopeDaoImpl(_sm,x);
            }

        });
    }

    @SuppressWarnings("unchecked")
    public Collection<XmlDataDAO> getVariables() {
        entering("ScopeDaoImpl.getVariables");
        Query q = getSession().createFilter(_scope.getVariables(), "where this.scope=?");
        q.setEntity(0, _scope);
        return CollectionsX.transform(new LinkedList<XmlDataDAO>(), (Collection<HXmlData>)q.list(), new UnaryFunction<HXmlData,XmlDataDAO>() {
            public XmlDataDAO apply(HXmlData x) {
                return new XmlDataDaoImpl(_sm,x);
            }

        });
    }

    @SuppressWarnings("unchecked")
    public List<BpelEvent> listEvents() {
        entering("ScopeDaoImpl.listEvents");
//        CriteriaBuilder cb = new CriteriaBuilder();
        Criteria crit = _sm.getSession().createCriteria(HBpelEvent.class);
//        if (efilter != null)
//            cb.buildCriteria(crit, efilter);
        crit.add(Restrictions.eq("scopeId",_scope.getId()));

        List<HBpelEvent> hevents = crit.list();
        List<BpelEvent> ret = new ArrayList<BpelEvent>(hevents.size());
        try {
            CollectionsX.transformEx(ret,hevents,new UnaryFunctionEx<HBpelEvent,BpelEvent>() {
                public BpelEvent apply(HBpelEvent x) throws Exception{
                    return (BpelEvent) SerializableUtils.toObject(x.getData(),BpelEvent.class.getClassLoader());
                }

            });
        }catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return ret;
    }
}
