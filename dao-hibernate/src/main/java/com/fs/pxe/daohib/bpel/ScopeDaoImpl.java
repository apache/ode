/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel;

import com.fs.pxe.bpel.common.BpelEventFilter;
import com.fs.pxe.bpel.dao.*;
import com.fs.pxe.bpel.evt.BpelEvent;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.bpel.hobj.*;
import com.fs.utils.SerializableUtils;
import com.fs.utils.stl.CollectionsX;
import com.fs.utils.stl.UnaryFunction;
import com.fs.utils.stl.UnaryFunctionEx;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import java.util.*;

/**
 * Hibernate-based {@link ScopeDAO} implementation.
 */
class ScopeDaoImpl extends HibernateDao implements ScopeDAO {

  private static final Log __log = LogFactory.getLog(ScopeDaoImpl.class);

  private static final String QRY_VARIABLE = "from " + HXmlData.class.getName() +
          " as x where x.name = ? and x.scope.id = ?";

  private static final String QRY_CSET = "from " + HCorrelationSet.class.getName() +
          " as c where c.name = ? and c.scope.id = ?";

  private static final String QRY_SCOPE_EPR = "from " + HPartnerLink.class.getName() +
          " as e where e.modelId = ? and e.scope = ?";

  private HScope _scope;


	public ScopeDaoImpl(SessionManager sm, HScope scope) {
    super(sm, scope);
		_scope = scope;
	}

  /**
	 * @see com.fs.pxe.bpel.dao.ScopeDAO#getCorrelationSet(java.lang.String)
	 */
	public CorrelationSetDAO getCorrelationSet(String corrSetName) {
    Query qry = getSession().createQuery(QRY_CSET);
    qry.setString(0,corrSetName);
    qry.setLong(1,_scope.getId());
    HCorrelationSet cs;
    Iterator iter = qry.iterate();
    
    if(!iter.hasNext()){
      // if it doesn't exist, we make it
      cs = new HCorrelationSet(_scope, corrSetName);
      _scope.getCorrelationSets().add(cs);
      getSession().save(cs);
    }else{
      cs = (HCorrelationSet)iter.next();
    }

    Hibernate.close(iter);
    return new CorrelationSetDaoImpl(_sm, cs);
  }
  /**
   * @see com.fs.pxe.bpel.dao.ScopeDAO#getParentScope()
   */
  public ScopeDAO getParentScope() {
    return _scope.getParentScope() != null
      ? new ScopeDaoImpl(_sm, _scope.getParentScope())
      : null;
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ScopeDAO#getProcessInstance()
	 */
	public ProcessInstanceDAO getProcessInstance() {
		return new ProcessInstanceDaoImpl(_sm, _scope.getInstance());
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ScopeDAO#setState(com.fs.pxe.bpel.dao.ScopeStateEnum)
	 */
	public void setState(ScopeStateEnum state) {
		_scope.setState(state.toString());
    getSession().update(_scope);
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ScopeDAO#getState()
	 */
	public ScopeStateEnum getState() {
		return new ScopeStateEnum(_scope.getState());
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ScopeDAO#getName()
	 */
	public String getName() {
		return _scope.getName();
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ScopeDAO#getVariable(java.lang.String)
	 */
	public XmlDataDAO getVariable(String varName) {
		HXmlData data;
    Query qry = getSession().createQuery(QRY_VARIABLE);
    qry.setString(0,varName);
    qry.setLong(1,_scope.getId());
    Iterator iter = qry.iterate();

    if(iter.hasNext())
      data = (HXmlData)iter.next();
    else{
      data = new HXmlData();
      data.setName(varName);
      data.setScope(_scope);
      _scope.getVariables().add(data);
      getSession().save(data);
    }
    Hibernate.close(iter);
    return new XmlDataDaoImpl(_sm, data);

	}

  /**
   * @see com.fs.pxe.bpel.dao.ScopeDAO#createPartnerLink(java.lang.String,java.lang.String)
   */
  public PartnerLinkDAO createPartnerLink(int modelId, String pLinkName, String myRole, String partnerRole) {
    HPartnerLink epr = new HPartnerLink();
    epr.setModelId(modelId);
    epr.setLinkName(pLinkName);
    epr.setMyRole(myRole);
    epr.setPartnerRole(partnerRole);
    epr.setScope(_scope);
    _scope.getPartnerLinks().add(epr);
    getSession().save(epr);
    PartnerLinkDAOImpl eprDao = new PartnerLinkDAOImpl(_sm, epr);
    return eprDao;
  }

  /**
   * @see com.fs.pxe.bpel.dao.ScopeDAO#getPartnerLink(java.lang.String,java.lang.String)
   */
  public PartnerLinkDAO getPartnerLink(int plinkId) {
    Query qry = getSession().createQuery(QRY_SCOPE_EPR);
    qry.setInteger(0,plinkId);
    qry.setEntity(1,_scope);
    HPartnerLink hpl = (HPartnerLink) qry.uniqueResult();
    if (hpl == null)
      return null;
    return new PartnerLinkDAOImpl(_sm, hpl);
  }

  /**
	 * @see com.fs.pxe.bpel.dao.ScopeDAO#getScopeInstanceId()
	 */
	public Long getScopeInstanceId() {
		return _scope.getId();
	}

  /**
	 * @see com.fs.pxe.bpel.dao.ScopeDAO#getModelId()
	 */
	public int getModelId() {
		return _scope.getScopeModelId();
	}

  public Set<CorrelationSetDAO> getCorrelationSets() {
    Set<CorrelationSetDAO> results = new HashSet<CorrelationSetDAO>();
    for (HCorrelationSet hCorrelationSet : _scope.getCorrelationSets()) {
      results.add(new CorrelationSetDaoImpl(_sm, hCorrelationSet));
    }
    return results;
  }


  @SuppressWarnings("unchecked")
  public Collection<ScopeDAO> getChildScopes() {
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
    Query q = getSession().createFilter(_scope.getVariables(), "where this.scope=?");
    q.setEntity(0, _scope);
    return CollectionsX.transform(new LinkedList<XmlDataDAO>(), (Collection<HXmlData>)q.list(), new UnaryFunction<HXmlData,XmlDataDAO>() {
      public XmlDataDAO apply(HXmlData x) {
        return new XmlDataDaoImpl(_sm,x);
      }

    });
  }

  @SuppressWarnings("unchecked")
  public List<BpelEvent> listEvents(BpelEventFilter efilter) {

    CriteriaBuilder cb = new CriteriaBuilder();
    Criteria crit = _sm.getSession().createCriteria(HBpelEvent.class);
    if (efilter != null)
      cb.buildCriteria(crit, efilter);
    crit.add(Restrictions.eq("scopeId",_scope.getId()));

    List<HBpelEvent> hevents = crit.list();
    List<BpelEvent> ret = new ArrayList<BpelEvent>(hevents.size());
    try {
      CollectionsX.transform(ret,hevents,new UnaryFunctionEx<HBpelEvent,BpelEvent>() {
        public BpelEvent apply(HBpelEvent x) throws Exception{
          return (BpelEvent) SerializableUtils.toObject(x.getData().getBinary(),BpelEvent.class.getClassLoader());
        }

      });
    }catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return ret;
  }

}
