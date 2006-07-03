/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.provider;

import com.fs.pxe.bpel.evt.BpelEvent;
import com.fs.pxe.bpel.jmx.BpelEventNotification;
import com.fs.pxe.bpel.jmx.ProcessMBean;
import com.fs.pxe.bpel.o.OAgent;
import com.fs.pxe.bpel.o.OProcess;
import com.fs.pxe.bpel.o.OScope;
import com.fs.pxe.bpel.pmapi.InstanceNotFoundException;
import com.fs.pxe.bpel.pmapi.InvalidRequestException;
import com.fs.utils.jmx.SimpleMBean;
import com.fs.utils.msg.MessageBundle;
import com.fs.utils.stl.CollectionsX;
import com.fs.utils.stl.MemberOfFunction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;

import javax.management.MBeanNotificationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.openmbean.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JMX management bean (MBean) representing a BPEL process instances. This
 * is an OpenMBean, so <em>no custom types</code>.
 *
 * @todo Reorganize relationship between instancemanagement-debuggersupper-jmx
 */
class ProcessMBeanImpl extends SimpleMBean implements ProcessMBean {
  private static final Log __log = LogFactory.getLog(ProcessMBeanImpl.class);
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  static DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm");
  
  static String[] INSTANCE_FIELD_NAMES = new String[] { "PIID", "Created", "LastActive", "State", "Fault", "Correlations" };
  static String[] CORRELATION_FIELD_NAMES = new String[] { "ScopeInstanceId", "CorrelationSet", "Correlations" };
  static ArrayType CORRELATION_ARR;
  static CompositeType COMPOSITE_INSTANCE_DETAIL, COMPOSITE_CORRELATION;
  static TabularType TABULAR_QUERY_RESULTS, TABULAR_CORRELATION;

  static{
    try{
      CORRELATION_ARR = new ArrayType(1, SimpleType.STRING);
      COMPOSITE_CORRELATION = new CompositeType(
          "CorrelationSet",
          "Correlation Set",
          CORRELATION_FIELD_NAMES,
          CORRELATION_FIELD_NAMES,
          new OpenType[] { SimpleType.STRING, SimpleType.STRING, CORRELATION_ARR });
      TABULAR_CORRELATION = new TabularType(
          "CorrelationSets",
          "Correlation Sets",
          COMPOSITE_CORRELATION,
          new String[] { "ScopeInstanceId","CorrelationSet" });
      COMPOSITE_INSTANCE_DETAIL = new CompositeType(
          "ProcessInstance", 
          "Process Instance",
          INSTANCE_FIELD_NAMES,
          INSTANCE_FIELD_NAMES,
          new OpenType[] { SimpleType.STRING, SimpleType.DATE, SimpleType.DATE, SimpleType.STRING, SimpleType.STRING, TABULAR_CORRELATION });
      TABULAR_QUERY_RESULTS = new TabularType(
          "ProcessInstances", 
          "Results of instance query",
          COMPOSITE_INSTANCE_DETAIL,
          new String[]{ "PIID" });
    }catch(Exception e){
      e.printStackTrace();
    	throw new RuntimeException(e);
    }
  }


  protected OProcess _oproc;
  protected BpelService _bsvc;
  
  public ProcessMBeanImpl(BpelService bsvc) throws NotCompliantMBeanException {
    super(ProcessMBean.class);

    addNotificationInfo(new MBeanNotificationInfo(new String[] {
      com.fs.pxe.bpel.evt.ActivityEnabledEvent.class.getName(),
      com.fs.pxe.bpel.evt.ActivityExecStartEvent.class.getName(),
      com.fs.pxe.bpel.evt.CorrelationMatchEvent.class.getName(),
      com.fs.pxe.bpel.evt.CorrelationNoMatchEvent.class.getName(),
      com.fs.pxe.bpel.evt.CorrelationSetWriteEvent.class.getName(),
      com.fs.pxe.bpel.evt.ExpressionEvaluationFailedEvent.class.getName(),
      com.fs.pxe.bpel.evt.ExpressionEvaluationSuccessEvent.class.getName(),
      com.fs.pxe.bpel.evt.ProcessMessageExchangeEvent.class.getName(),
      com.fs.pxe.bpel.evt.NewProcessInstanceEvent.class.getName(),
      com.fs.pxe.bpel.evt.ProcessCompletionEvent.class.getName(),
      com.fs.pxe.bpel.evt.ProcessInstanceStartedEvent.class.getName(),
      com.fs.pxe.bpel.evt.ProcessInstanceStateChangeEvent.class.getName(),
      com.fs.pxe.bpel.evt.ProcessTerminationEvent.class.getName(),
      com.fs.pxe.bpel.evt.ScopeCompletionEvent.class.getName(),
      com.fs.pxe.bpel.evt.ScopeFaultEvent.class.getName(),
      com.fs.pxe.bpel.evt.VariableModificationEvent.class.getName(),
      com.fs.pxe.bpel.evt.VariableReadEvent.class.getName()
    },"BpelEvents", "BPEL Processing Events"));

     _bsvc = bsvc;
     // TODO:Fix thi
    _oproc = null; // bsvc.getOProcess();
  }

  protected ObjectName createObjectName() {
    return _bsvc.getService().createLocalObjectName(new String[] {
      "type", "BPELProcessAdmin"
    });
  }

	/**
	 * @see com.fs.pxe.bpel.jmx.ProcessMBean#getName()
	 */
	public String getName() {
		return _oproc.processName;
	}

	/**
	 * @see com.fs.pxe.bpel.jmx.ProcessMBean#kill(Long)
	 */
	public void kill(final Long iid) {
    try {
      _bsvc.getManagementFacade().terminate(iid);
    } catch (InstanceNotFoundException infe) {
      __log.error(__msgs.msgInstanceNotFound(iid),infe);
      throw new IllegalArgumentException(__msgs.msgInstanceNotFound(iid));
    } catch (InvalidRequestException ire) {
      __log.error(__msgs.msgBpelManagementException(ire.getMessage()));
      throw new IllegalArgumentException(ire.getMessage());
    } catch (Exception e) {
      String msg = __msgs.msgBpelManagementException(e.getMessage());
      __log.error(msg, e);
      throw new RuntimeException(msg);
    }
  }
  
  /**
   * @see com.fs.pxe.bpel.jmx.ProcessMBean#suspend(Long)
   */
  public void suspend(final Long iid) {
    try {
      _bsvc.getManagementFacade().suspend(iid);
    } catch (InstanceNotFoundException infe) {
      __log.error(__msgs.msgInstanceNotFound(iid),infe);
      throw new IllegalArgumentException(__msgs.msgInstanceNotFound(iid));
    } catch (InvalidRequestException ire) {
      __log.error(__msgs.msgBpelManagementException(ire.getMessage()));
      throw new IllegalArgumentException(ire.getMessage());
    } catch (Exception e) {
      String msg = __msgs.msgBpelManagementException(e.getMessage());
      __log.error(msg, e);
      throw new RuntimeException(msg);
    }
  }
  /**
   * @see com.fs.pxe.bpel.jmx.ProcessMBean#resume(Long)
   */
  public void resume(final Long iid)  {
    try {
      _bsvc.getManagementFacade().resume(iid);
    } catch (InstanceNotFoundException infe) {
      __log.error(__msgs.msgInstanceNotFound(iid),infe);
      throw new IllegalArgumentException(__msgs.msgInstanceNotFound(iid));
    } catch (InvalidRequestException ire) {
      __log.error(__msgs.msgBpelManagementException(ire.getMessage()));
      throw new IllegalArgumentException(ire.getMessage());
    } catch (Exception e) {
      String msg = __msgs.msgBpelManagementException(e.getMessage());
      __log.error(msg, e);
      throw new RuntimeException(msg);
    }
  }
	/**
	 * @see com.fs.pxe.bpel.jmx.ProcessMBean#showVariableData(Long, String, int)
	 */
	public TabularData showVariableData(Long pid, String variableName, int scopeModelId) {
    try {
      return _showVariableData(pid, variableName, scopeModelId);
    } catch (Exception e) {
      String msg = __msgs.msgDbError();
      __log.error(msg, e);
      throw new RuntimeException(msg);
    }
  }
  
  public String showVariableDataForScopeInstance(Long pid, String variableName, Long scopeInstanceId) {
    try {
      return _showVariableData(pid, variableName, scopeInstanceId);
    } catch (Exception e) {
      String msg = __msgs.msgDbError();
      __log.error(msg, e);
      throw new RuntimeException(msg);
    }
  }
  
  public CompositeData showInstanceDetail(final Long iid)  {
//    try {
//      return (CompositeData)_db.exec(new BpelDatabase.Callable<Object>(){
//        public Object run(BpelDAOConnection conn) throws Exception {
//          ProcessInstanceDAO p = conn.getInstance(iid);
//          if (p == null)
//            throw new InstanceNotFoundException("" +  iid);
//          return createInstanceDetail(p);
//        }
//      });
//    } catch (InstanceNotFoundException infe) {
//      String msg = __msgs.msgInstanceNotFound(iid);
//      __log.error(msg, infe);
//      throw new IllegalArgumentException(msg);
//    } catch (Exception e) {
//      __log.error("DbError",e);
//      throw new RuntimeException(e.toString());
//    }
    // TODO: Implement using Mangement API
    throw new UnsupportedOperationException();
    
  }
  
  
	public TabularData instanceQuerySimple(String fromDate, String toDate, short state)  {
//		InstanceQuery pq = new InstanceQuery();
//    
//    try{
//      if(fromDate != null && !"".equals(fromDate.trim()))
//      	pq.from = DATE_FORMAT.parse(fromDate);
//      if(toDate != null && !"".equals(toDate.trim()))
//      	pq.to = DATE_FORMAT.parse(toDate);
//      if(state > 0)
//        pq.processState = new short[]{ state };
//    }catch(ParseException e){
//    	throw new IllegalArgumentException("Malformed date string: must in the form dd/mm/yy hh:mm");
//    }
//    return instanceQueryAdvanced(pq);
    // TODO: Implement using Mangement API
    throw new UnsupportedOperationException();
	}

//	private TabularData createCorrelationTable(ProcessInstanceDAO dao){
//		TabularDataSupport tbl = new TabularDataSupport(TABULAR_CORRELATION);
//    for(Iterator iter = dao.getCorrelationSets().iterator(); iter.hasNext(); ){
//    	CorrelationSetDAO cset = (CorrelationSetDAO)iter.next();
//      CorrelationKey ckey = cset.getValue();
//      Object[] values = new Object[] { cset.getScope().getScopeInstanceId(), cset.getName(), ckey.getValues() };
//      CompositeDataSupport cd;
//      try {
//        cd = new CompositeDataSupport(COMPOSITE_CORRELATION, CORRELATION_FIELD_NAMES, values);
//        tbl.put(cd);
//      } catch (OpenDataException e) {
//        __log.error("Error creating composite data.", e);
//      }
//    }
//    return tbl;
//  }


//  private CompositeData createInstanceDetail(ProcessInstanceDAO pdao){
//    Object[] values = new Object[]{
//        pdao.getInstanceId(),
//        new Date(pdao.getCreateTime().getTime() ),
//        new Date(pdao.getLastActiveTime().getTime()),
//        ProcessState.stateToString(pdao.getState()),
//        pdao.getFault() == null ? "" : pdao.getFault().toString(),
//        createCorrelationTable(pdao) };
//    CompositeDataSupport cd = null;
//    try {
//      cd = new CompositeDataSupport(COMPOSITE_INSTANCE_DETAIL, INSTANCE_FIELD_NAMES, values);
//    } catch (OpenDataException e) {
//      __log.error("Error creating composite data.", e);
//    }
//    return cd;
//  }

  public void onEvent(BpelEvent event) {
    send(new BpelEventNotification(getObjectName(), nextNotificationSequence(), event));
  }

  protected TabularData _showVariableData(final Long iid, final String variableName, final int scopeModelId) {
  //    final TabularDataSupport tbl = new TabularDataSupport(BpelMBeanImpl.VARIABLES_TABULAR_TYPE);
  //    try {
  //      _db.exec(_db.new Callable<Object>(){
  //        public Object run(BpelDAOConnection conn) throws Exception {
  //          ProcessInstanceDAO p = conn.getInstance(iid);
  //          int smId = scopeModelId;
  //          if(smId < 1){
  //            OScope scope = BpelMBeanImpl.findFirstScopeForVariable(_oproc.procesScope, variableName);
  //            if(scope == null)
  //              throw new IllegalArgumentException("Bad variable name '" + variableName + "'");
  //            smId = scope.getId();
  //          }
  //
  //          XmlDataDAO[] vars = p.getVariables(variableName, smId);
  //          for(int i = 0; i < vars.length; ++i){
  //            Object[] values = new Object[]{
  //              Integer.valueOf(scopeModelId),
  //                vars[i].getScopeDAO().getScopeInstanceId(),
  //                DOMUtils.domToString(vars[i].get())};
  //            CompositeDataSupport cd;
  //            try {
  //              cd = new CompositeDataSupport(BpelMBeanImpl.VARIABLE_COMPOSITE_TYPE, BpelMBeanImpl.VARIABLE_COMPOSITE_NAMES, values);
  //              tbl.put(cd);
  //            } catch (OpenDataException e) {
  //              __log.error("Error creating composite data.", e);
  //            }
  //          }
  //          return null;
  //        }
  //      });
  //    } catch (RuntimeException re) {
  //      throw re;
  //    } catch (Exception ex) {
  //      __log.error("UnexpectedEx", ex);
  //      throw new RuntimeException(ex);
  //    }
  //    return tbl;
      //TODO Implement via management API
      throw new UnsupportedOperationException();
    }

  protected String _showVariableData(final Long iid, final String variableName, final Long scopeInstanceId) {
  //    try {
  //      return (String)_db.exec(_db.new Callable<Object>(){
  //        public Object run(BpelDAOConnection conn) throws Exception {
  //          ProcessInstanceDAO p = conn.getInstance(iid);
  //          ScopeDAO scope = p.getScope(scopeInstanceId);
  //          XmlDataDAO var = scope.getVariable(variableName);
  //          if(var == null || var.isNull())
  //            return null;
  //          return DOMUtils.domToString(var.get());
  //        }
  //      });
  //    } catch (RuntimeException re) {
  //      throw re;
  //    } catch (Exception ex) {
  //      __log.error("UnexpectedEx", ex);
  //      throw new RuntimeException(ex);
  //    }
      //TODO Implement via management API
      throw new UnsupportedOperationException();  
    }

  static String[] VARIABLE_COMPOSITE_NAMES = new String[] { "OScopeId", "ScopeInstanceId", "XmlData"}; 
  static CompositeType VARIABLE_COMPOSITE_TYPE;
  static TabularType VARIABLES_TABULAR_TYPE;
  static {
    try{
      VARIABLE_COMPOSITE_TYPE = new CompositeType(
        "Variable",
        "BPEL Variable",
        VARIABLE_COMPOSITE_NAMES,
        VARIABLE_COMPOSITE_NAMES,
        new OpenType[] { SimpleType.INTEGER, SimpleType.STRING, SimpleType.STRING });
      VARIABLES_TABULAR_TYPE = new TabularType(
          "Variables",
          "BPEL Variables",
          VARIABLE_COMPOSITE_TYPE,
          new String[] { "ScopeInstanceId" });
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * Perform a breadth-first search for a variable of a given name.
   * @param agent agent where the search should start
   * @param var variable name we are looking for
   * @return
   */
  static OScope findFirstScopeForVariable(OAgent agent, final String var){
    final MemberOfFunction<OAgent> finder = new MemberOfFunction<OAgent>() {
      public boolean isMember(OAgent agent) {
        return agent instanceof OScope && ((OScope)agent).getLocalVariable(var) != null;
      }
    };

    HashSet<OAgent> searchSet = new HashSet<OAgent>();
    searchSet.add(agent);


    while (!searchSet.isEmpty()) {
      // Try to find in our search set.
      OScope found = (OScope) CollectionsX.find_if(searchSet,finder);
      if (found != null)
        return found;

      // Create a new search set consisting of all the children of the
      // current search set.
      HashSet<OAgent> newSearchSet = new HashSet<OAgent>();
      for(Iterator<OAgent> iter = searchSet.iterator(); iter.hasNext(); )
        newSearchSet.addAll(iter.next().nested);
      searchSet = newSearchSet;
    }

    return null;
  }

}
