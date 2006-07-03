/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import com.fs.pxe.bpel.common.BpelEventFilter;
import com.fs.pxe.bpel.common.Filter;
import com.fs.pxe.bpel.common.InstanceFilter;
import com.fs.pxe.bpel.common.ProcessFilter;
import com.fs.pxe.bpel.dao.BpelDAOConnection;
import com.fs.pxe.bpel.dao.MessageExchangeDAO;
import com.fs.pxe.bpel.dao.ProcessDAO;
import com.fs.pxe.bpel.dao.ProcessInstanceDAO;
import com.fs.pxe.bpel.dao.ScopeDAO;
import com.fs.pxe.bpel.evt.BpelEvent;
import com.fs.pxe.bpel.evt.ScopeEvent;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.hobj.HLargeData;
import com.fs.pxe.daohib.ql.instances.HibernateInstancesQueryCompiler;
import com.fs.pxe.daohib.bpel.hobj.HBpelEvent;
import com.fs.pxe.daohib.bpel.hobj.HMessageExchange;
import com.fs.pxe.daohib.bpel.hobj.HProcess;
import com.fs.pxe.daohib.bpel.hobj.HProcessInstance;
import com.fs.pxe.daohib.bpel.hobj.HScope;
import com.fs.pxe.ql.eval.skel.CommandEvaluator;
import com.fs.pxe.ql.tree.Builder;
import com.fs.pxe.ql.tree.BuilderFactory;
import com.fs.pxe.ql.tree.nodes.Query;
import com.fs.utils.ISO8601DateParser;
import com.fs.utils.SerializableUtils;
import com.fs.utils.stl.CollectionsX;
import com.fs.utils.stl.UnaryFunctionEx;

/**
 * Hibernate-based {@link BpelDAOConnection} implementation.
 */
class BpelDAOConnectionImpl implements BpelDAOConnection {
  
  private static final Log __log = LogFactory.getLog(BpelDAOConnectionImpl.class);
  
  private Session _session;
  private SessionManager _sm;

  BpelDAOConnectionImpl(SessionManager sm) {
    _sm = sm;
    _session = _sm.getSession();
  }
  

  public MessageExchangeDAO createMessageExchange(char dir) {
    HMessageExchange mex = new HMessageExchange();
    mex.setDirection(dir);
    _session.save(mex);
    return new MessageExchangeDaoImpl(_sm, mex);
  }



  public MessageExchangeDAO getMessageExchange(String mexid) {
    HMessageExchange mex = (HMessageExchange) _session.get(HMessageExchange.class,new Long(mexid));
    return mex == null ? null : new MessageExchangeDaoImpl(_sm, mex);
  }


  public ProcessDAO createProcess(QName pid, QName type) {
    HProcess process = new HProcess();
    process.setProcessId(pid.toString());
    process.setType(type.toString());
    process.setDeployDate(new Date());
    _session.save(process);
    return new ProcessDaoImpl(_sm,process);
  }
  


  /**
   * @see com.fs.pxe.bpel.dao.BpelDAOConnection#getProcess(java.lang.String)
   */
  public ProcessDAO getProcess(QName processId) {

    try {
      Criteria criteria = _session.createCriteria(HProcess.class);
      criteria.add(Expression.eq("processId", processId.toString()));
      // For the moment we are expecting only one result.
      HProcess hprocess = (HProcess)criteria.uniqueResult();
      return hprocess == null ? null : new ProcessDaoImpl(_sm, hprocess);
    } catch (HibernateException e) {
      __log.error("DbError",e);
      throw e;
    }

  }

  public void close() {
  }

  /**
   * @see com.fs.pxe.bpel.dao.ProcessDAO#getInstance(java.lang.Long)
   */
  public ProcessInstanceDAO getInstance(Long instanceId) {
    return _getInstance(_sm,_session, instanceId);
  }


  public ScopeDAO getScope(Long siidl) {
    return _getScope(_sm, _session, siidl);
  }

  
  public Collection<ProcessInstanceDAO> instanceQuery(InstanceFilter criteria) {
    if(criteria.getLimit()==0) {
      return Collections.emptyList();
    }
    List<ProcessInstanceDAO> daos = new ArrayList<ProcessInstanceDAO>();

    Iterator<HProcessInstance> iter = _instanceQuery(_session,false, criteria);
    while(iter.hasNext()) {
      daos.add(new ProcessInstanceDaoImpl(_sm,iter.next()));
    }

    return daos;
  }

  public Collection<ProcessDAO> processQuery(ProcessFilter filter) {
    List<ProcessDAO> daos = new ArrayList<ProcessDAO>();

    Iterator<HProcess> iter = _processQuery(_session, filter);
    while(iter.hasNext()) {
      daos.add(new ProcessDaoImpl(_sm,iter.next()));
    }

    return daos;
  }

  @SuppressWarnings("unchecked")
  static Iterator<HProcess> _processQuery(Session session, ProcessFilter filter) {
    Criteria crit = session.createCriteria(HProcess.class);

    // Filtering using an example object
    HProcess exampleProcess = new HProcess();
    if (filter.getNameFilter() != null) exampleProcess.setType(filter.getNameFilter().replaceAll("\\*", "%"));
    crit.add(Example.create(exampleProcess).ignoreCase().enableLike().excludeZeroes());
    // TODO Implement process status filtering when status will exist

    // Specific filter for deployment date.
    if (filter.getDeployedDateFilter() != null) {
      for (String ddf : filter.getDeployedDateFilter()) {
        Date deployDate = null;
        try {
          deployDate = ISO8601DateParser.parse(Filter.getDateWithoutOp(ddf));
        } catch (ParseException e) {
          // Never occurs, the deploy date format is pre-validated by the filter
        }
        if (ddf.startsWith("=")) {
          crit.add(Restrictions.eq("deployDate", deployDate));
        } else if (ddf.startsWith("<=")) {
          crit.add(Restrictions.le("deployDate", deployDate));
        } else if (ddf.startsWith(">=")) {
          crit.add(Restrictions.ge("deployDate", deployDate));
        } else if (ddf.startsWith("<")) {
          crit.add(Restrictions.lt("deployDate", deployDate));
        } else if (ddf.startsWith(">")) {
          crit.add(Restrictions.gt("deployDate", deployDate));
        }
      }
    }

    // Ordering
    if (filter.getOrders() != null) {
      for (String key : filter.getOrders()) {
        boolean ascending = true;
        String orderKey = key;
        if (key.startsWith("+") || key.startsWith("-")) {
          orderKey = key.substring(1, key.length());
          if (key.startsWith("-")) ascending = false;
        }

        if ("name".equals(orderKey)) {
          if (ascending) crit.addOrder(Property.forName("processName").asc());
          else crit.addOrder(Property.forName("processName").desc());
        } else if ("namespace".equals(orderKey)) {
          if (ascending) crit.addOrder(Property.forName("processNamespace").asc());
          else crit.addOrder(Property.forName("processNamespace").desc());
        } else if ("version".equals(orderKey)) {
          if (ascending) crit.addOrder(Property.forName("version").asc());
          else crit.addOrder(Property.forName("version").desc());
          //    TODO Implement when process status will be implemented
          //    } else if ("status".equals(orderKey)) {
        } else if ("deployed".equals(orderKey)) {
          if (ascending) crit.addOrder(Property.forName("deployDate").asc());
          else crit.addOrder(Property.forName("deployDate").desc());
        }
      }
    }

    return crit.list().iterator();
  }

  @SuppressWarnings("unchecked")
  static Iterator<HProcessInstance> _instanceQuery(Session session, boolean countOnly, InstanceFilter filter) {
    Criteria crit = session.createCriteria(HProcessInstance.class);
    CriteriaBuilder cb=new CriteriaBuilder();
    cb.buildCriteria(crit, filter);
    return crit.list().iterator();
  }


  static ProcessInstanceDAO _getInstance(SessionManager sm, Session session, Long iid) {
    HProcessInstance instance = (HProcessInstance) session.get(HProcessInstance.class, iid);
    return instance != null
            ? new ProcessInstanceDaoImpl(sm, instance)
            : null;
  }


  static ScopeDAO _getScope(SessionManager sm, Session session, Long siid) {
    HScope scope = (HScope) session.get(HScope.class, siid);
    return scope != null
            ? new ScopeDaoImpl(sm, scope)
            : null;
  }
  

  public void insertBpelEvent(BpelEvent event, 
      ProcessDAO process, 
      ProcessInstanceDAO instance) 
  {
    _insertBpelEvent(_sm.getSession(), event, process, instance);
  }

  /**
   * Helper method for inserting bpel events into the database.
   * @param sess
   * @param event
   * @param process
   * @param instance
   */
  static void _insertBpelEvent(Session sess, BpelEvent event,  ProcessDAO process, 
      ProcessInstanceDAO instance)  
  {    
    HBpelEvent hevent = new HBpelEvent();
    hevent.setTstamp(new Timestamp(System.currentTimeMillis()));
    hevent.setType(BpelEvent.eventName(event));
    hevent.setDetail(event.toString());
    if (process != null)
      hevent.setProcess((HProcess) ((ProcessDaoImpl)process).getHibernateObj());
    if (instance != null)
      hevent.setInstance((HProcessInstance) ((ProcessInstanceDaoImpl)instance).getHibernateObj());
    if (event instanceof ScopeEvent)
      hevent.setScopeId(((ScopeEvent)event).getScopeId());
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(event);
      oos.flush();
      HLargeData ld = new HLargeData(bos.toByteArray());
      hevent.setData(ld);
      sess.save(ld);
    } catch (Throwable ex) {
      // this is really unexpected.
      __log.fatal("InternalError: BpelEvent serialization failed.", ex);
    }
    sess.save(hevent);
  }

  @SuppressWarnings({"unchecked","deprecation"})
  public List<Date> bpelEventTimelineQuery(InstanceFilter ifilter, BpelEventFilter efilter) {
    CriteriaBuilder cb = new CriteriaBuilder();
    Criteria crit = _session.createCriteria(HBpelEvent.class);
    if (ifilter != null)
      cb.buildCriteria(crit, efilter);
    if (ifilter !=  null)
      cb.buildCriteria(crit.createCriteria("instance"),ifilter);
    crit.setFetchMode("tstamp", FetchMode.EAGER);
    crit.setProjection(Projections.property("tstamp"));
    return crit.list();
  }


  @SuppressWarnings("unchecked")
  public List<BpelEvent> bpelEventQuery(InstanceFilter ifilter, BpelEventFilter efilter) {
    CriteriaBuilder cb = new CriteriaBuilder();
    Criteria crit = _session.createCriteria(HBpelEvent.class);
    if (efilter != null)
      cb.buildCriteria(crit, efilter);
    if (ifilter !=  null)
      cb.buildCriteria(crit.createCriteria("instance"),ifilter);
    List<HBpelEvent> hevents = crit.list();
    List<BpelEvent> ret = new ArrayList<BpelEvent>(hevents.size());
    try {
      CollectionsX.transform(ret,hevents,new UnaryFunctionEx<HBpelEvent,BpelEvent>() {
        public BpelEvent apply(HBpelEvent x) throws Exception{
          return (BpelEvent) SerializableUtils.toObject(x.getData().getBinary(),BpelEvent.class.getClassLoader());
        }
      
      });
    }catch (Exception ex) {
      __log.fatal("Internal error: unable to transform HBpelEvent",ex);
      throw new RuntimeException(ex); 
    }
    return ret;
  }

  /**
   * @see com.fs.pxe.bpel.dao.BpelDAOConnection#instanceQuery(String)
   */
  @SuppressWarnings("unchecked")
  public Collection<ProcessInstanceDAO> instanceQuery(String expression) {
    Builder<String> builder = BuilderFactory.getInstance().createBuilder();
    final com.fs.pxe.ql.tree.nodes.Node rootNode = builder.build(expression);
    
    HibernateInstancesQueryCompiler compiler = new HibernateInstancesQueryCompiler();
    
    CommandEvaluator<List, Session> eval = compiler.compile((Query)rootNode);
    List<HProcessInstance> instancesList = (List<HProcessInstance>)eval.evaluate(_session);
    
    Collection<ProcessInstanceDAO> result = new ArrayList<ProcessInstanceDAO>(instancesList.size());
    for(HProcessInstance instance : instancesList) {
      result.add(getInstance(instance.getId()));
    }
    return result;
  }

}
