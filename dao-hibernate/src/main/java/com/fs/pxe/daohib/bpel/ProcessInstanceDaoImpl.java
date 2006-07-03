/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel;

import com.fs.pxe.bpel.common.ProcessState;
import com.fs.pxe.bpel.dao.*;
import com.fs.pxe.bpel.evt.ProcessInstanceEvent;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.hobj.HLargeData;
import com.fs.pxe.daohib.bpel.hobj.*;
import com.fs.utils.QNameUtils;
import com.fs.utils.stl.CollectionsX;
import com.fs.utils.stl.UnaryFunction;

import java.util.*;

import javax.xml.namespace.QName;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.w3c.dom.Element;

/**
 * Hibernate-based {@link ProcessInstanceDAO} implementation.
 */
class ProcessInstanceDaoImpl extends HibernateDao implements ProcessInstanceDAO {
  /** Query for removing selectors. */
  private static final String QRY_DELSELECTORS = "delete from "  + 
    HCorrelatorSelector.class.getName() + " where instance = ?";

  private static final String QRY_VARIABLES = "from " + HXmlData.class.getName()
    + " as x where x.name = ? and x.scope.scopeModelId = ? and x.scope.instance.id = ?";

  private HProcessInstance _instance;

  private ScopeDAO _root;
  
	public ProcessInstanceDaoImpl(SessionManager sm, HProcessInstance instance) {
    super(sm, instance);
		_instance = instance;
	}

  /**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getCreateTime()
	 */
	public Date getCreateTime() {
		return _instance.getCreated();
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#setFault(javax.xml.namespace.QName)
	 */
	public void setFault(QName fault) {
		_instance.setFault(QNameUtils.fromQName(fault));
    getSession().update(_instance);
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getFault()
	 */
	public QName getFault() {
		String fault = _instance.getFault();
    return fault == null
      ? null
      : QNameUtils.toQName(fault);
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#setFaultData(org.w3c.dom.Element)
	 */
	public void setFaultData(Element faultData) {
		// TODO
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getFaultData()
	 */
	public Element getFaultData() {
		// TODO
    return null;
	}
  
  
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getExecutionState()
	 */
	public byte[] getExecutionState() {
    if (_instance.getJacobState() == null) return null;
    return _instance.getJacobState().getBinary();
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#setExecutionState(byte[])
	 */
	public void setExecutionState(byte[] bytes) {
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
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getProcess()
	 */
	public ProcessDAO getProcess() {
		return new ProcessDaoImpl(_sm, _instance.getProcess());
	}
  
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getRootScope()
	 */
	public ScopeDAO getRootScope() {
    if (_root != null) 
      return _root;
    Query rootQry = getSession().createFilter(_instance.getScopes(),
        "where this.parentScope is null");
    HScope hroot = (HScope)rootQry.uniqueResult();
    if (hroot == null)
      return null;
		return _root = new ScopeDaoImpl(_sm, hroot);
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#setState(short)
	 */
	public void setState(short state) {
    _instance.setState(state);
    if(state==ProcessState.STATE_TERMINATED) {
      clearSelectors();
    }
    getSession().update(_instance);
  }
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getState()
	 */
	public short getState() {
		return _instance.getState();
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getPreviousState()
	 */
	public short getPreviousState() {
		return _instance.getPreviousState();
	}

  
	public ScopeDAO createScope(ScopeDAO parentScope, String name, int scopeModelId) {
		HScope scope = new HScope();
    scope.setParentScope(parentScope != null
        ? (HScope)((ScopeDaoImpl)parentScope).getHibernateObj()
        : null);
    scope.setName(name);
    scope.setScopeModelId(scopeModelId);
    scope.setState(ScopeStateEnum.ACTIVE.toString());
    scope.setInstance(_instance);
    scope.setCreated(new Date());
    _instance.getScopes().add(scope);
    getSession().save(scope);

		return new ScopeDaoImpl(_sm, scope);
	}

	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getInstanceId()
	 */
	public Long getInstanceId() {
		return _instance.getId();
	}

  public ScopeDAO getScope(Long scopeInstanceId) {
    Long id = Long.valueOf(scopeInstanceId);
    HScope scope = (HScope)getSession().get(HScope.class, id);
    return scope != null
            ? new ScopeDaoImpl(_sm, scope)
            : null;
  }
  
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getScopes(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
  public Collection<ScopeDAO> getScopes(String scopeName) {
    Collection<HScope> hscopes;
    if (scopeName != null) {
      Query filter = _sm.getSession().createFilter(_instance.getScopes(),
          "where this.name=?");
      filter.setString(0,scopeName);
      hscopes = filter.list();
    } else
      hscopes = _instance.getScopes();
    ArrayList<ScopeDAO> ret = new ArrayList<ScopeDAO>();
    CollectionsX.transform(ret, hscopes, new UnaryFunction<HScope,ScopeDAO> () {
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
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getInstantiatingCorrelator()
	 */
	public CorrelatorDAO getInstantiatingCorrelator() {
		return new CorrelatorDaoImpl(_sm, _instance.getInstantiatingCorrelator());
	}

  /**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getLastActiveTime()
	 */
	public Date getLastActiveTime() {
		return _instance.getLastActiveTime();
	}
	/**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#setLastActiveTime(java.util.Date)
	 */
	public void setLastActiveTime(Date dt) {
		_instance.setLastActiveTime(dt);
	}

  public Set<CorrelationSetDAO> getCorrelationSets() {
    Set<CorrelationSetDAO> results = new HashSet<CorrelationSetDAO>();

    for (HCorrelationSet hCorrelationSet : _instance.getCorrelationSets()) {
      results.add(new CorrelationSetDaoImpl(_sm, hCorrelationSet));
    }

    return results;
  }

  public CorrelationSetDAO getCorrelationSet(String name) {
    for (HCorrelationSet hCorrelationSet : _instance.getCorrelationSets()) {
      if (hCorrelationSet.getName().equals(name))
        return new CorrelationSetDaoImpl(_sm, hCorrelationSet);
    }
    return null;
  }

  /**
	 * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#getVariables(java.lang.String, int)
	 */
	public XmlDataDAO[] getVariables(String variableName, int scopeModelId) {
    List<XmlDataDAO> results = new ArrayList<XmlDataDAO>();

    Iterator iter;
    Query qry = getSession().createQuery(QRY_VARIABLES);
    qry.setString(0, variableName);
    qry.setInteger(1, scopeModelId);
    qry.setLong(2, _instance.getId());
    iter = qry.iterate();

    while(iter.hasNext()) {
    	results.add(new XmlDataDaoImpl(_sm, (HXmlData)iter.next()));
    }
    Hibernate.close(iter);

    return results.toArray(new XmlDataDAO[results.size()]);
	}

  /**
   * @see com.fs.pxe.bpel.dao.ProcessInstanceDAO#finishCompletion()
   */
  public void finishCompletion() {
    // make sure we have completed.
    assert (ProcessState.isFinished(this.getState()));
    // let our process know that we've done our work.
    this.getProcess().instanceCompleted(this);
  }

  public void delete() {
    _sm.getSession().delete(_instance);
  }

  public void insertBpelEvent(ProcessInstanceEvent event) {
    // Defer to the BpelDAOConnectionImpl
    BpelDAOConnectionImpl._insertBpelEvent(_sm.getSession(),event, this.getProcess(), this);
  }

  public EventsFirstLastCountTuple getEventsFirstLastCount() {

    // Using a criteria, find the min,max, and count of event tstamps.
    Criteria c = _sm.getSession().createCriteria(HBpelEvent.class);
    c.add(Restrictions.eq("instance",_instance));
    c.setProjection(Projections.projectionList().add(Projections.min("tstamp"))
                                                .add(Projections.max("tstamp"))
                                                .add(Projections.count("tstamp")));
    
    Object[] ret = (Object[]) c.uniqueResult();
    EventsFirstLastCountTuple flc = new EventsFirstLastCountTuple();
    flc.first = (Date) ret[0];
    flc.last = (Date) ret[1];
    flc.count = (Integer)ret[2];
    return flc;
  }

  public long genMonotonic() {
    long seq = _instance.getSequence()+1;
    _instance.setSequence(seq);
    return seq;
  }
  
  protected void clearSelectors() {
    Query q = getSession().createQuery(QRY_DELSELECTORS);
    q.setEntity(0, _instance);
    q.executeUpdate();    
  }

  public BpelDAOConnection getConnection() {
    return new BpelDAOConnectionImpl(_sm);
  }



}
