package com.fs.pxe.bpel.engine;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fs.pxe.bpel.dao.MessageExchangeDAO;
import com.fs.pxe.bpel.dao.ProcessDAO;
import com.fs.pxe.bpel.dao.ProcessInstanceDAO;
import com.fs.pxe.bpel.iapi.BpelEngine;
import com.fs.pxe.bpel.iapi.BpelEngineException;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.MessageExchange;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.pxe.bpel.iapi.MessageExchange.MessageExchangePattern;
import com.fs.pxe.bpel.iapi.MessageExchange.Status;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import com.fs.pxe.bpel.o.OPartnerLink;
import com.fs.pxe.bpel.o.OProcess;
import com.fs.pxe.bpel.runtime.ExpressionLanguageRuntimeRegistry;
import com.fs.utils.msg.MessageBundle;

import org.w3c.dom.Element;


public class BpelEngineImpl implements BpelEngine {

  private static final Log __log = LogFactory.getLog(BpelEngineImpl.class);

  private static final Messages __msgs = MessageBundle
      .getMessages(Messages.class);

  /**
   * Active processes, keyed by process id.
   */
  final HashMap<QName, BpelProcess> _activeProcesses = new HashMap<QName, BpelProcess>();

  /**
   * Active processes, keyed by myRole endpoint. NOTE: this is not being used at
   * this point. 
   */
  final HashMap<EndpointReference, BpelProcess> _eprToProcessMap = new HashMap<EndpointReference, BpelProcess>();

  /** Mapping from service name to active process. */
  private final HashMap<QName, BpelProcess> _serviceMap = new HashMap<QName,BpelProcess>();

  final Contexts _contexts;


  public BpelEngineImpl(Contexts contexts) {
    _contexts = contexts;
  }

  public boolean isMyRoleEndpoint(EndpointReference epr)
      throws BpelEngineException {
    return _eprToProcessMap.containsKey(epr);
  }

  public MyRoleMessageExchange createMessageExchange(
      String clientKey,
      QName targetService,
      EndpointReference epr,
      String operation) throws BpelEngineException {

    MessageExchangeDAO dao = _contexts.dao.getConnection()
        .createMessageExchange(MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE);
    dao.setCorrelationId(clientKey);
    dao.setCorrelationStatus(CorrelationStatus.UKNOWN_ENDPOINT.toString());
    dao.setPattern(MessageExchangePattern.UNKNOWN.toString());
    dao.setCallee(targetService);
    dao.setStatus(Status.NEW.toString());
    dao.setOperation(operation);
    dao.setEPR(epr == null ? null : epr.toXML().getDocumentElement());
    MyRoleMessageExchangeImpl mex = new MyRoleMessageExchangeImpl(this, dao);

    BpelProcess target = route(targetService, epr,null);
    if (target != null)
      target.initMyRoleMex(mex);
    
    return mex;
  }

  public MessageExchange getMessageExchange(String mexId)
      throws BpelEngineException {
    MessageExchangeDAO mexdao = _contexts.dao.getConnection()
        .getMessageExchange(mexId);
    if (mexdao == null)
      return null;

    ProcessDAO pdao = mexdao.getProcess();
    BpelProcess process = pdao == null ? null : _activeProcesses.get(pdao
        .getProcessId());

    MessageExchangeImpl mex;
    switch (mexdao.getDirection()) {
    case MessageExchangeDAO.DIR_BPEL_INVOKES_PARTNERROLE:
      if (process == null) {
        String errmsg = __msgs.msgProcessNotActive(pdao.getProcessId());
        __log.error(errmsg);
        // TODO: Perhaps we should define a checked exception for this
        // condition.
        throw new BpelEngineException(errmsg);
      } else {
        OPartnerLink plink = (OPartnerLink) process._oprocess.getChild(mexdao
            .getPartnerLinkModelId());
        PortType ptype = plink.partnerRolePortType;
        Operation op = plink.getPartnerRoleOperation(mexdao.getOperation());
        mex = new PartnerRoleMessageExchangeImpl(this, mexdao, ptype, op, null);
      }
      break;
    case MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE:
      mex = new MyRoleMessageExchangeImpl(this, mexdao);
      if (process != null) {
        OPartnerLink plink = (OPartnerLink) process._oprocess.getChild(mexdao
            .getPartnerLinkModelId());
        PortType ptype = plink.myRolePortType;
        Operation op = plink.getMyRoleOperation(mexdao.getOperation());
        mex.setPortOp(ptype, op);
      }
      break;
    default:
      String errmsg = "BpelEngineImpl: internal error, invalid MexDAO direction: "
          + mexId;
      __log.fatal(errmsg);
      throw new BpelEngineException(errmsg);
    }

    if (process != null)
      mex.setProcess(process._oprocess);

    return mex;
  }

  boolean unregisterProcess(QName process) {
    BpelProcess p = _activeProcesses.remove(process);
    if (p != null) {
      p.deactivate();
      _eprToProcessMap.values().remove(p);
      _serviceMap.values().remove(p);
    }
    return p != null;
  }

  boolean isProcessRegistered(QName pid) {
    return _activeProcesses.containsKey(pid);
  }

  void registerProcess(QName pid,
      OProcess compiledProcess,
      Map<Integer,QName> serviceNames,
      Map<Integer,Element> myEprs,
      ExpressionLanguageRuntimeRegistry elangRegistry) {
    BpelProcess process = new BpelProcess(this, pid, compiledProcess,
        serviceNames,
        myEprs,
        null,
        elangRegistry);
    _activeProcesses.put(pid, process);
    for (QName sn : serviceNames.values()) {
      __log.info( "Register process: serviceId=" + sn + ", process=" + process );
      _serviceMap.put(sn,process);
    }

    process.activate();
  }

  void addRoute(EndpointReference epr, QName pid) {
    BpelProcess target = _activeProcesses.get(pid);
    if (target == null)
      throw new IllegalArgumentException("Process " + pid + " is not active.");
    _eprToProcessMap.put(epr, target);
  }
  
  void delRoute(EndpointReference epr) {
    _eprToProcessMap.remove(epr);
    
  }
 
  /**
   * Route to a process using the service id.
   * 
   * @param service
   *          process id
   * @param request 
   * @return
   */
  BpelProcess route(QName service, EndpointReference epr, Message request) {
    // TODO: use the message to route to the correct service if more than one
    // service is listening on the same endpoint.
	if ( __log.isDebugEnabled() ) {
      __log.debug( "Route: service=" + service + ", epr=" + epr + " request=" + request );
	}
    return _serviceMap.get(service);
  }
  

  OProcess getOProcess(QName processId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("todo");
  }

  public void onScheduledJob(String jobId, Map<String, Object> jobDetail) {

    WorkEvent we = new WorkEvent(jobDetail);

    ProcessInstanceDAO instance = _contexts.dao.getConnection().getInstance(
        we.getIID());
    if (instance == null) {
      __log.error(__msgs.msgScheduledJobReferencesUnknownInstance(we.getIID()));
      // nothing we can do, this instance is not in the database, it will always
      // fail.
      return;
    }

    ProcessDAO processDao = instance.getProcess();
    BpelProcess process = _activeProcesses.get(processDao.getProcessId());
    if (process == null) {
      // If the process is not active, it means that we should not be doing
      // any work on its behalf, therefore we will reschedule the events
      // for some time in the future (1 minute).
      Date future = new Date(System.currentTimeMillis() + (60 * 1000));
      __log.info(__msgs.msgReschedulingJobForInactiveProcess(processDao
          .getProcessId(), jobId, future));
      _contexts.scheduler.schedulePersistedJob(jobDetail, future);
    }

    assert process != null;
    process.handleWorkEvent(jobDetail);
  }


  public MessageExchange getMessageExchangeByClientKey(String clientKey) {
    // TODO Auto-generated method stub
    return null;
  }


}
