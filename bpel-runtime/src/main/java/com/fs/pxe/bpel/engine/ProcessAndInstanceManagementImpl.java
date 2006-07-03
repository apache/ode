/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.bpel.engine;

import com.fs.pxe.bpel.common.*;
import com.fs.pxe.bpel.dao.*;
import com.fs.pxe.bpel.evt.BpelEvent;
import com.fs.pxe.bpel.evt.ProcessInstanceStateChangeEvent;
import com.fs.pxe.bpel.evt.ProcessTerminationEvent;
import com.fs.pxe.bpel.evtproc.ActivityStateDocumentBuilder;
import com.fs.pxe.bpel.o.OPartnerLink;
import com.fs.pxe.bpel.o.OProcess;
import com.fs.pxe.bpel.pmapi.*;
import com.fs.utils.DOMUtils;
import com.fs.utils.ISO8601DateParser;
import com.fs.utils.msg.MessageBundle;
import com.fs.utils.stl.CollectionsX;
import com.fs.utils.stl.UnaryFunction;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Implentation of the Process and InstanceManagement APIs.
 * 
 * TODO Pull up IM/PM methods from BpelManagementFacadeImpl
 */
class ProcessAndInstanceManagementImpl
        implements InstanceManagement, ProcessManagement {


  protected static final Messages __msgs = MessageBundle.getMessages(Messages.class);
  protected static Log __log = LogFactory.getLog(BpelManagementFacadeImpl.class);
  protected static final ProcessStatusConverter __psc = new ProcessStatusConverter();
  protected BpelEngineImpl _engine;
  protected BpelDatabase _db;
  private DebuggerSupport _debugSupport;

  public ProcessAndInstanceManagementImpl(BpelDatabase db, BpelEngineImpl engine) {
    _db = db;
    _engine = engine;
    
  }


  public ProcessInfoListDocument listProcesses(String filter, String orderKeys, final ProcessInfoCustomizer custom) {
    ProcessInfoListDocument ret = ProcessInfoListDocument.Factory.newInstance();
    final TProcessInfoList procInfoList = ret.addNewProcessInfoList();
    final ProcessFilter processFilter = new ProcessFilter(filter, orderKeys);

    try {
      _db.exec(new BpelDatabase.Callable<Object>() {
        public Object run(BpelDAOConnection conn) {
          Collection<ProcessDAO> processes = conn.processQuery(processFilter);
          for (ProcessDAO proc : processes) {
            fillProcessInfo(procInfoList.addNewProcessInfo(), proc, custom);
          }
          return null;
        }
      });
    } catch (Exception e) {
      __log.error("DbError", e);
      throw new ProcessingException("DbError",e);
    }

    return ret;
  }

  public ProcessInfoListDocument listProcesses(String filter, String orderKeys) {
    return listProcesses(filter, orderKeys, ProcessInfoCustomizer.ALL);
  }

  public ProcessInfoListDocument listProcesses() {
    return listProcesses(null, null, ProcessInfoCustomizer.ALL);
  }

  public ProcessInfoDocument getProcessInfo(String pid, ProcessInfoCustomizer custom) {
    return genProcessInfoDocument(pid, custom);
  }


  public ProcessInfoDocument getProcessInfo(String pid) {
    return getProcessInfo(pid, ProcessInfoCustomizer.ALL);
  }

  public ProcessInfoDocument activate(String pid) {
    // TODO: Figure out how to deal with activation/retirement.
    return genProcessInfoDocument(pid, ProcessInfoCustomizer.NONE);
  }


  public ProcessInfoDocument setRetired(final String pid, final boolean retired)
          throws ManagementException {
    try {
      _db.exec(new BpelDatabase.Callable<Object>() {
        public Object run(BpelDAOConnection conn) throws Exception {
          ProcessDAO proc = conn.getProcess(QName.valueOf(pid));
          if (proc == null)
            throw new InvalidRequestException("ProcessNotFound:" + pid);
          proc.setRetired(retired);

          return null;
        }
      });
    } catch (ManagementException me) {
      throw me;
    } catch (Exception e) {
      __log.error("DbError", e);
      throw new ProcessingException("DbError",e);
    }

    return genProcessInfoDocument(pid, ProcessInfoCustomizer.NONE);
  }

  public ProcessInfoDocument setProcessProperty(final String pid, final QName propertyName, final Node value)
          throws ManagementException {
    ProcessInfoDocument ret = ProcessInfoDocument.Factory.newInstance();
    final TProcessInfo pi = ret.addNewProcessInfo();
    try {
      _db.exec(new BpelDatabase.Callable<Object>() {
        public Object run(BpelDAOConnection conn) throws Exception {
          ProcessDAO proc = conn.getProcess(QName.valueOf(pid));
          if (proc == null)
            throw new ProcessNotFoundException("ProcessNotFound:" + pid);
          proc.setProperty(propertyName.getLocalPart(), propertyName.getNamespaceURI(), value);
          fillProcessInfo(pi, proc, new ProcessInfoCustomizer(false, true, false));
          return null;
        }
      });
    } catch (ManagementException me) {
      throw me;
    } catch (Exception e) {
      __log.error("DbError", e);
      throw new ProcessingException("DbError",e);
    }

    return ret;
  }

  public ProcessInfoDocument setProcessProperty(final String pid, final QName propertyName, final String value)
          throws ManagementException {
    ProcessInfoDocument ret = ProcessInfoDocument.Factory.newInstance();
    final TProcessInfo pi = ret.addNewProcessInfo();
    try {
      _db.exec(new BpelDatabase.Callable<Object>() {
        public Object run(BpelDAOConnection conn) throws Exception {
          ProcessDAO proc = conn.getProcess(QName.valueOf(pid));
          if (proc == null)
            throw new ProcessNotFoundException("ProcessNotFound:" + pid);
          proc.setProperty(propertyName.getLocalPart(), propertyName.getNamespaceURI(), value);
          fillProcessInfo(pi, proc, new ProcessInfoCustomizer(false, true, false));
          return null;
        }
      });
    } catch (ManagementException me) {
      throw me;
    } catch (Exception e) {
      __log.error("DbError", e);
      throw new ProcessingException("DbError",e);
    }

    return ret;
  }

  public ProcessInfoDocument setEndpointReference(final String pid, final String partnerLink,
                                                  final String roleName, final Element endpointRef)
          throws ManagementException {
    ProcessInfoDocument ret = ProcessInfoDocument.Factory.newInstance();
    final TProcessInfo pi = ret.addNewProcessInfo();
    try {
      _db.exec(new BpelDatabase.Callable<Object>() {
        public Object run(BpelDAOConnection conn) throws Exception {
          ProcessDAO proc = conn.getProcess(QName.valueOf(pid));
          if (proc == null)
            throw new ProcessNotFoundException("ProcessNotFound:" + pid);

          OProcess oprocess = _engine.getOProcess(QName.valueOf(pid));
          if (oprocess == null)
            throw new ProcessNotFoundException("ProcessNotFound:" + pid);

          OPartnerLink plink = oprocess.getPartnerLink(partnerLink);
          if (plink == null)
            throw new InvalidRequestException("Unknown partner-link "
                + partnerLink + " for process " + pid);
                        
          PartnerLinkDAO depdao = proc.getDeployedEndpointReference(plink.getId());
          
          if (plink != null && ((plink.myRoleName != null) && (plink.myRoleName.equals(roleName)))) {
            depdao.setMyEPR(endpointRef);
          } else if (plink != null && ((plink.partnerRoleName != null) && (plink.partnerRoleName.equals(roleName)))) {
            depdao.setPartnerEPR(endpointRef);
          } else {
            throw new InvalidRequestException("Invalid partnerLink/partnerRole combination "
                + partnerLink + "/" + roleName + " for process " + pid);
            
          }
          
          fillProcessInfo(pi, proc, new ProcessInfoCustomizer(false, false, true));
          return null;
        }
      });
    } catch (ManagementException me) {
      throw me;
    } catch (Exception e) {
      __log.error("DbError", e);
      throw new ProcessingException("DbError",e);
    }

    return ret;
  }


  public InstanceInfoListDocument listInstances(String filter, String order, int limit) {
    InstanceInfoListDocument ret = InstanceInfoListDocument.Factory.newInstance();
    final TInstanceInfoList infolist = ret.addNewInstanceInfoList();
    final InstanceFilter instanceFilter = new InstanceFilter(filter, order, limit);
    try {

      _db.exec(new BpelDatabase.Callable<Object>() {
        public Object run(BpelDAOConnection conn) {
          Collection<ProcessInstanceDAO> instances = conn.instanceQuery(instanceFilter);
          for (ProcessInstanceDAO instance : instances) {
            fillInstanceInfo(infolist.addNewInstanceInfo(), instance);
          }
          return null;
        }
      });
    } catch (Exception e) {
      __log.error("DbError", e);
      throw new ProcessingException("DbError",e);
    }

    return ret;
  }

  public InstanceInfoListDocument listInstances() {
    return listInstances(null, null, Integer.MAX_VALUE);
  }

  public InstanceInfoListDocument listInstances(int limit) {
    return listInstances(null, null, limit);
  }

  public InstanceInfoDocument getInstanceInfo(Long iid) throws InstanceNotFoundException {
    return genInstanceInfoDocument(iid);
  }



  public ScopeInfoDocument getScopeInfo(String siid) {
    return getScopeInfo(siid, false);
  }

  public ScopeInfoDocument getScopeInfo(String siid, boolean includeActivityInfo) {
    return genScopeInfoDocument(siid, includeActivityInfo);
  }

  public VariableInfoDocument getVariableInfo(final String scopeId, final String varName)
          throws ManagementException {
    VariableInfoDocument ret = VariableInfoDocument.Factory.newInstance();
    final TVariableInfo vinf = ret.addNewVariableInfo();
    final TVariableRef sref = vinf.addNewSelf();
    dbexec(new BpelDatabase.Callable<Object>()  {
      public Object run(BpelDAOConnection session) throws Exception {
        ScopeDAO scope = session.getScope(new Long(scopeId));
        if (scope == null) {
          throw new InvalidRequestException("ScopeNotFound:" + scopeId);
        }

        sref.setSiid(scopeId);
        sref.setIid(scope.getProcessInstance().getInstanceId().toString());
        sref.setName(varName );

        XmlDataDAO var = scope.getVariable(varName);
        if (var == null) {
          throw new InvalidRequestException("VarNotFound:" + varName);
        }

        Node nval = var.get();
        if (nval != null) {
          TVariableInfo.Value val = vinf.addNewValue();
          val.getDomNode().appendChild(
                  val.getDomNode().getOwnerDocument().importNode(nval,true));
        }
        return null;
      }
    });
    return ret;
  }

  //
  // INSTANCE ACTIONS
  //
  public InstanceInfoDocument fault(Long iid, QName faultname, Element faultData) {
    // TODO: Implement
    return genInstanceInfoDocument(iid);
  }


  public InstanceInfoDocument resume(final Long iid) {
    // We need debugger support in order to resume (since we have to force
    // a reduction. If one is not available the getDebugger() method should
    // throw a ProcessingException
    _debugSupport.resume(iid);

    return genInstanceInfoDocument(iid);
  }

  public InstanceInfoDocument suspend(final Long iid)
          throws ManagementException {
    DebuggerSupport debugSupport = getDebugger(iid);
    assert debugSupport != null : "getDebugger(Long) returned NULL!";
    debugSupport.suspend(iid);

    return genInstanceInfoDocument(iid);   
  }

  public InstanceInfoDocument terminate(final Long iid) throws ManagementException {
    DebuggerSupport debugSupport = getDebugger(iid);
    assert debugSupport != null : "getDebugger(Long) returned NULL!";
    debugSupport.terminate(iid);

    return genInstanceInfoDocument(iid);   
  }

  public Collection<Long> delete(String filter) {
    final InstanceFilter instanceFilter = new InstanceFilter(filter);

    List<Long> ret = new LinkedList<Long>();
    try {

      _db.exec(new BpelDatabase.Callable<Object>() {
        public Object run(BpelDAOConnection conn) {
          Collection<ProcessInstanceDAO> instances = conn.instanceQuery(instanceFilter);
          for (ProcessInstanceDAO instance :instances) {
            instance.delete();
          }
          return null;
        }
      });
    } catch (Exception e) {
      __log.error("DbError", e);
      throw new ProcessingException("DbError",e);
    }

    return ret;
  }

  //
  // EVENT RETRIEVAL
  //  
  public List<String> getEventTimeline(String instanceFilter, String eventFilter) {
    final InstanceFilter ifilter = new InstanceFilter(instanceFilter,null,0);
    final BpelEventFilter efilter = new BpelEventFilter(eventFilter,0);

    List<Date> tline = dbexec(new BpelDatabase.Callable<List<Date>>()  {
      public List<Date> run(BpelDAOConnection session) throws Exception {
        return session.bpelEventTimelineQuery(ifilter, efilter);
      }
    });

    ArrayList<String> ret = new ArrayList<String>(tline.size());
    CollectionsX.transform(ret,tline,new UnaryFunction<Date,String>() {
      public String apply(Date x) {
        return ISO8601DateParser.format(x);
      }
    });
    return ret;
  }

  public List<BpelEvent> listEvents(String instanceFilter, String eventFilter, int maxCount) {

    final InstanceFilter ifilter = new InstanceFilter(instanceFilter,null,0);
    final BpelEventFilter efilter = new BpelEventFilter(eventFilter,maxCount);

    return dbexec(new BpelDatabase.Callable<List<BpelEvent>>()  {
      public List<BpelEvent> run(BpelDAOConnection session) throws Exception {
        return session.bpelEventQuery(ifilter, efilter);
      }
    });
  }

  /**
   * Get the {@link DebuggerSupport} object for the given process identifier. Debugger
   * support is required for operations that resume execution in some way or manipulate
   * the breakpoints.
   * @param procid process identifier
   * @return associated debugger support object
   * @throws ManagementException
   */
  protected final DebuggerSupport getDebugger(QName procid) throws ManagementException {
    if (_debugSupport == null)
      throw new ProcessingException("DebugSupport required for debugger operation.");

    BpelProcess process = _engine._activeProcesses.get(procid);
    if (process == null)
      throw new InvalidRequestException("The process \"" + procid + "\" is available." );

    return _debugSupport;
  }


  /**
   * Get the {@link DebuggerSupport} object for the given instance identifier. Debugger
   * support is required for operations that resume execution in some way or manipulate
   * the breakpoints.
   * @param iid instance identifier
   * @return associated debugger support object
   * @throws ManagementException
   */
  protected final DebuggerSupport getDebugger(final Long iid) {
    QName processId;

    try {
      processId = _db.exec(new BpelDatabase.Callable<QName>() {
        public QName run(BpelDAOConnection conn) throws Exception {
          ProcessInstanceDAO instance = conn.getInstance(iid);
          return instance == null ? null : instance.getProcess().getProcessId();
        }
      });
    } catch (Exception e) {
      __log.error("DbError",e);
      throw new ProcessingException("DbError", e);
    }

    return getDebugger(processId);
  }

  /**
   * Execute a database transaction, unwrapping nested {@link ManagementException}s.
   * @param runnable action to run
   * @return
   * @throws ManagementException
   */
  protected <T> T dbexec(BpelProcessDatabase.Callable<T> runnable) throws ManagementException {
    try {
      return runnable.exec();
    } catch (ManagementException me) {
      throw me;
    } catch (Exception e) {
      __log.error("DbError", e);
      throw new ManagementException("DbError",e);
    }
  }

  /**
   * Execute a database transaction, unwrapping nested {@link ManagementException}s.
   * @param callable action to run
   * @return
   * @throws ManagementException
   */
  protected <T> T dbexec(BpelDatabase.Callable<T> callable) throws ManagementException {
    try {
      return _db.exec(callable);
    } catch (ManagementException me) {
      // Passthrough.
      throw me;
    } catch (Exception ex) {
      __log.error("DbError", ex);
      throw new ManagementException("DbError",ex);
    }
  }

  private ProcessInfoDocument genProcessInfoDocument(final String procid, final ProcessInfoCustomizer custom)
          throws ManagementException {
    ProcessInfoDocument ret = ProcessInfoDocument.Factory.newInstance();
    final TProcessInfo pi = ret.addNewProcessInfo();
    try {
      _db.exec(new BpelDatabase.Callable<Object>() {
        public Object run(BpelDAOConnection conn) {
          ProcessDAO proc = conn.getProcess(QName.valueOf(procid));
          if (proc == null)
            throw new InvalidRequestException("ProcessNotFound:" + procid);
          fillProcessInfo(pi, proc, custom);
          return null;
        }
      });
    } catch (ManagementException me) {
      throw me;
    } catch (Exception e) {
      __log.error("DbError", e);
      throw new ProcessingException("UnexpectedEx",e);
    }

    return ret;
  }

  /**
   * Generate a {@link InstanceInfoDocument} for a given instance. This
   * document contains general information about the instance.
   * @param iid instance identifier
   * @return generated document
   */
  private InstanceInfoDocument genInstanceInfoDocument(final Long iid) {
    if (iid == null)
      throw new InvalidRequestException("Must specifiy instance id.");

    InstanceInfoDocument ret = InstanceInfoDocument.Factory.newInstance();
    final TInstanceInfo ii = ret.addNewInstanceInfo();

    ii.setIid(iid.toString());
    dbexec(new BpelDatabase.Callable<Object>() {
      public Object run(BpelDAOConnection conn) throws Exception {
        ProcessInstanceDAO instance = conn.getInstance(iid);

        if (instance == null)
          throw new InstanceNotFoundException("" + iid);
        // TODO: deal with "ERROR" state information.
        fillInstanceInfo(ii,instance);
        return null;
      }
    });


    return ret;
  }



  /**
   * Generate a {@link ScopeInfoDocument} for a given scope instance. 
   * @param siid scope instance identifier
   * @param includeActivityInfo
   * @return generated document
   */
  private ScopeInfoDocument genScopeInfoDocument(final String siid, final boolean includeActivityInfo) {
    if (siid == null)
      throw new InvalidRequestException("Must specifiy scope instance id.");

    final Long siidl;
    try {
      siidl = new Long(siid);
    } catch (NumberFormatException nfe) {
      throw new InvalidRequestException("Invalid scope instance id.");
    }

    ScopeInfoDocument ret = ScopeInfoDocument.Factory.newInstance();
    final TScopeInfo ii = ret.addNewScopeInfo();

    ii.setSiid(siid);
    dbexec(new BpelDatabase.Callable<Object>() {
      public Object run(BpelDAOConnection conn) throws Exception {
        ScopeDAO instance = conn.getScope(siidl);
        if (instance == null)
          throw new InvalidRequestException("Scope not found: " + siidl);
        // TODO: deal with "ERROR" state information.
        fillScopeInfo(ii,instance,includeActivityInfo);
        return null;
      }
    });


    return ret;
  }


  /**
   * Fill in the <code>process-info</code> element of the transfer object.
   * @param info destination XMLBean
   * @param proc source DAO object
   * @param custom used to customize the quantity of information produced in the info
   */
  private void fillProcessInfo(TProcessInfo info, ProcessDAO proc, ProcessInfoCustomizer custom) {
    info.setPid(proc.getProcessId().toString());
    // TODO: ACTIVE and RETIRED should be used separately. 
    //Active process may be retired at the same time
    if(proc.isRetired()) {
      info.setStatus(TProcessStatus.RETIRED);
    } else {
      info.setStatus(TProcessStatus.ACTIVE);
    }

    TDefinitionInfo definfo = info.addNewDefinitionInfo();
    definfo.setProcessName(proc.getDefinitionName());

    TDeploymentInfo depinfo = info.addNewDeploymentInfo();
    depinfo.setDeployDate(toCalendar(proc.getDeployDate()));
    depinfo.setDeployer(proc.getDeployer());
    if (custom.includeInstanceSummary()) {
      TInstanceSummary isum = info.addNewInstanceSummary();
      genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.ACTIVE, proc);
      genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.COMPLETED, proc);
      genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.ERROR, proc);
      genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.FAILED, proc);
      genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.SUSPENDED, proc);
      genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.TERMINATED, proc);
    }


      TProcessInfo.Documents docinfo = info.addNewDocuments();
      //TODO: what happens if we have a remote URI?
      File deployDir = new File(proc.getDeployURI().getPath());
      File files[] = deployDir.listFiles();
      if (files != null)
        genDocumentInfo(docinfo, deployDir, files,true);
      else
      if (__log.isDebugEnabled())
        __log.debug("fillProcessInfo: No files for " + deployDir + " !!!");



    if (custom.includeProcessProperties()) {
      TProcessProperties properties = info.addNewProperties();
      for (ProcessPropertyDAO processPropertyDAO : proc.getProperties()) {
        TProcessProperties.Property tprocProp = properties.addNewProperty();
        tprocProp.setName(new QName(processPropertyDAO.getNamespace(),processPropertyDAO.getName()));
        Node propNode = tprocProp.getDomNode();
        if (processPropertyDAO.getSimpleContent() != null) {
          propNode.appendChild(propNode.getOwnerDocument()
                  .createTextNode(processPropertyDAO.getSimpleContent()));
        } else if (processPropertyDAO.getMixedContent() != null) {
          try {
            Node readNode = DOMUtils.stringToDOM(processPropertyDAO.getMixedContent());
            Document processInfoDoc = propNode.getOwnerDocument();
            Node node2append = processInfoDoc.importNode(readNode, true);
            propNode.appendChild(node2append);
          } catch (SAXException e) {
            __log.error("Mixed content stored in property " + processPropertyDAO.getName() +
                    " for process " + proc.getProcessId() + " couldn't be converted to a DOM " +
                    "document.", e);
          } catch (IOException e) {
            __log.error("Mixed content stored in property " + processPropertyDAO.getName() +
                    " for process " + proc.getProcessId() + " couldn't be converted to a DOM " +
                    "document.", e);
          }
        }
      }
    }

    if (custom.includeEndpoints()) {
      TEndpointReferences eprs = info.addNewEndpoints();
      OProcess oprocess = _engine.getOProcess(proc.getProcessId());
      for (OPartnerLink oplink : oprocess.getAllPartnerLinks()) {
        if (oplink.hasPartnerRole() && oplink.initializePartnerRole) {
          MultiKeyMap mm = buildEprMap(proc);
          Node eprNode = (Node) mm.get(oplink.name, oplink.partnerRoleName);
          if (eprNode != null) {
            TEndpointReferences.EndpointRef epr = eprs.addNewEndpointRef();
            epr.setPartnerLink(oplink.name);
            epr.setPartnerRole(oplink.partnerRoleName);
            Document eprNodeDoc = epr.getDomNode().getOwnerDocument();
            epr.getDomNode().appendChild(eprNodeDoc.importNode(eprNode, true));
          } else {
            __log.error("The process " + proc.getProcessId() + " has probably been deployed in a wrong way, the " +
                    "partner link " + oplink.name + " has no endpoint associated with it, which probably means " +
                    "that the deployment descriptor wasn't included in the process package you deployed.");
          }
        }
      }
    }

    //TODO: add documents to the above data structure.
  }

  private MultiKeyMap buildEprMap(ProcessDAO proc) {
    MultiKeyMap mm = new MultiKeyMap();
    for (PartnerLinkDAO eprDao : proc.getDeployedEndpointReferences()) {
      if (eprDao.getMyEPR() != null)
        mm.put(eprDao.getPartnerLinkName(), eprDao.getMyRoleName(), eprDao.getMyEPR());
      if (eprDao.getPartnerEPR() != null)
        mm.put(eprDao.getPartnerLinkName(), eprDao.getPartnerEPR(), eprDao.getPartnerEPR());
    }
    return mm;
  }

  /**
   * Generate document information elements for a set of files.
   * @param docinfo target element
   * @param files files
   * @param recurse recurse down directories?
   */
  private void genDocumentInfo(TProcessInfo.Documents docinfo,  File rootdir, File[] files,boolean recurse) {

    if (files == null)
      return;

    for (File f : files) {
      if (f.isHidden())
        continue;

      if (f.isDirectory()) {
        if (recurse)
          genDocumentInfo(docinfo, rootdir, f.listFiles(), true);
      } else if (f.isFile()) {
        genDocumentInfo(docinfo, rootdir, f);
      }

    }

  }

  private void genDocumentInfo(TProcessInfo.Documents docinfo, File rootDir, File f) {
    DocumentInfoGenerator dig = new DocumentInfoGenerator(rootDir,f);

    if (dig.isRecognized() && dig.isVisible()) {
      TDocumentInfo doc = docinfo.addNewDocument();

      doc.setName(dig.getName());
      doc.setSource(dig.getURL());
      doc.setType(dig.getType());
    }

  }

  private void genInstanceSummaryEntry(TInstanceSummary.Instances instances, TInstanceStatus.Enum state, ProcessDAO proc) {
    instances.setState(state);
    InstanceFilter instanceFilter = new InstanceFilter(""); // TODO: put this query back
    int count = _db.getConnection().instanceQuery(instanceFilter).size();
    instances.setCount(count);
  }

  private void fillInstanceInfo(TInstanceInfo info, ProcessInstanceDAO instance) {
    info.setIid("" + instance.getInstanceId());
    // TODO: add process QName to instance-info schema
    ProcessDAO processDAO = instance.getProcess();
    info.setPid(processDAO.getProcessId().toString());
    info.setProcessName(processDAO.getDefinitionName());
    if (instance.getRootScope() != null)
      info.setRootScope(genScopeRef(instance.getRootScope()));
    info.setDtStarted(toCalendar(instance.getCreateTime()));
    info.setDtLastActive(toCalendar(instance.getLastActiveTime()));
    info.setStatus(__psc.cvtInstanceStatus(instance.getState()));
    if (instance.getFault() != null) {
      TFaultInfo faultInfo = info.addNewFaultInfo();
      faultInfo.setFaultName(instance.getFault());
      // TODO: Put the fault data in if it is present.
    }

    ProcessInstanceDAO.EventsFirstLastCountTuple flc = instance.getEventsFirstLastCount();
    TInstanceInfo.EventInfo eventInfo = info.addNewEventInfo();
    Calendar first = Calendar.getInstance();
    Calendar last = Calendar.getInstance();

    // Setting valued correlation properties
    if (!instance.getCorrelationSets().isEmpty()) {
      TInstanceInfo.CorrelationProperties corrProperties = info.addNewCorrelationProperties();
      for (CorrelationSetDAO correlationSetDAO : instance.getCorrelationSets()) {
        for (Map.Entry<QName, String> property : correlationSetDAO.getProperties().entrySet()) {
          TCorrelationProperty tproperty = corrProperties.addNewCorrelationProperty();
          tproperty.setCsetid(""+correlationSetDAO.getCorrelationSetId());
          tproperty.setPropertyName(property.getKey());
          tproperty.setStringValue(property.getValue());
        }
      }
    }

    last.setTime(flc.last);
    first.setTime(flc.first);

    eventInfo.setFirstDtime(first);
    eventInfo.setLastDtime(last);
    eventInfo.setCount(flc.count);

  }

  private void fillScopeInfo(TScopeInfo scopeInfo, ScopeDAO scope, boolean includeActivityInfo) {
    scopeInfo.setSiid("" + scope.getScopeInstanceId());
    scopeInfo.setName(scope.getName());
    if (scope.getParentScope() != null)
      scopeInfo.setParentScopeRef(genScopeRef(scope.getParentScope()));

    scopeInfo.setStatus(__psc.cvtScopeStatus(scope.getState()));

    TScopeInfo.Children children = scopeInfo.addNewChildren();
    for (ScopeDAO i : scope.getChildScopes())
      fillScopeRef(children.addNewChildRef(), i);

    TScopeInfo.Variables vars = scopeInfo.addNewVariables();
    for (XmlDataDAO i : scope.getVariables())
      fillVariableRef(vars.addNewVariableRef(), i);

    // Setting correlations and their valued properties
    if (!scope.getCorrelationSets().isEmpty()) {
      TScopeInfo.CorrelationSets correlationSets = scopeInfo.addNewCorrelationSets();
      for (CorrelationSetDAO correlationSetDAO : scope.getCorrelationSets()) {
        TScopeInfo.CorrelationSets.CorrelationSet correlationSet =
                correlationSets.addNewCorrelationSet();
        correlationSet.setCsetid(""+correlationSetDAO.getCorrelationSetId());
        correlationSet.setName(correlationSetDAO.getName());
        for (Map.Entry<QName, String> property : correlationSetDAO.getProperties().entrySet()) {
          TCorrelationProperty tproperty = correlationSet.addNewCorrelationProperty();
          tproperty.setCsetid(""+correlationSetDAO.getCorrelationSetId());
          tproperty.setPropertyName(property.getKey());
          tproperty.setStringValue(property.getValue());
        }
      }

    }

    if (includeActivityInfo) {
      TScopeInfo.Activities activities = scopeInfo.addNewActivities();
      List<BpelEvent> events = scope.listEvents(null);
      ActivityStateDocumentBuilder b = new ActivityStateDocumentBuilder();
      for (BpelEvent e : events) b.onEvent(e);
      for (ActivityInfoDocument ai : b.getActivities())
        activities.addNewActivityInfo().set(ai.getActivityInfo());
    }
  }

  private void fillVariableRef(TVariableRef ref, XmlDataDAO i) {
    ref.setIid(i.getScopeDAO().getProcessInstance().getInstanceId().toString());
    ref.setSiid(i.getScopeDAO().getScopeInstanceId().toString());
    ref.setName(i.getName());
  }

  private TScopeRef genScopeRef(ScopeDAO scope) {
    TScopeRef tref = TScopeRef.Factory.newInstance();
    fillScopeRef(tref, scope);
    return tref;
  }

  private void fillScopeRef(TScopeRef tref, ScopeDAO scope) {
    tref.setSiid(scope.getScopeInstanceId().toString());
    tref.setStatus(__psc.cvtScopeStatus(scope.getState()));
    tref.setName(scope.getName());
    tref.setModelId("" + scope.getModelId());
  }

  /**
   * Convert a {@link Date} to a {@link Calendar}.
   * @param dtime a {@link Date}
   * @return a {@link Calendar}
   */
  private static Calendar toCalendar(Date dtime) {
    if (dtime == null)
      return null;

    Calendar c = Calendar.getInstance();
    c.setTime(dtime);
    return c;
  }


  /**
   * @see com.fs.pxe.bpel.pmapi.InstanceManagement#listInstances(java.lang.String)
   */
  public InstanceInfoListDocument listInstances(final String query) {
    InstanceInfoListDocument ret = InstanceInfoListDocument.Factory.newInstance();
    final TInstanceInfoList infolist = ret.addNewInstanceInfoList();

    try {
      _db.exec(new BpelDatabase.Callable<Object>() {
        public Object run(BpelDAOConnection conn) {
          Collection<ProcessInstanceDAO> instances = conn.instanceQuery(query);
          for (ProcessInstanceDAO instance : instances) {
            fillInstanceInfo(infolist.addNewInstanceInfo(), instance);
          }
          return null;
        }
      });
    } catch (Exception e) {
      __log.error("DbError", e);
      throw new ProcessingException("DbError",e);
    }

    return ret;
  }

}
