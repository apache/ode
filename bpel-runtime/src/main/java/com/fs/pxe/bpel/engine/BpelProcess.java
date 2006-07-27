/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.engine;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.fs.jacob.soup.ReplacementMap;
import com.fs.pxe.bpel.common.CorrelationKey;
import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.common.InvalidMessageException;
import com.fs.pxe.bpel.dao.CorrelatorDAO;
import com.fs.pxe.bpel.dao.MessageRouteDAO;
import com.fs.pxe.bpel.dao.ProcessDAO;
import com.fs.pxe.bpel.dao.ProcessInstanceDAO;
import com.fs.pxe.bpel.evt.CorrelationMatchEvent;
import com.fs.pxe.bpel.evt.CorrelationNoMatchEvent;
import com.fs.pxe.bpel.evt.NewProcessInstanceEvent;
import com.fs.pxe.bpel.explang.EvaluationException;
import com.fs.pxe.bpel.iapi.BpelEngineException;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.iapi.MessageExchange;
import com.fs.pxe.bpel.iapi.MessageExchange.FailureType;
import com.fs.pxe.bpel.iapi.MessageExchange.MessageExchangePattern;
import com.fs.pxe.bpel.iapi.MessageExchange.Status;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import com.fs.pxe.bpel.o.OBase;
import com.fs.pxe.bpel.o.OElementVarType;
import com.fs.pxe.bpel.o.OMessageVarType;
import com.fs.pxe.bpel.o.OPartnerLink;
import com.fs.pxe.bpel.o.OProcess;
import com.fs.pxe.bpel.o.OScope;
import com.fs.pxe.bpel.runtime.ExpressionLanguageRuntimeRegistry;
import com.fs.pxe.bpel.runtime.InvalidProcessException;
import com.fs.pxe.bpel.runtime.PROCESS;
import com.fs.pxe.bpel.runtime.PropertyAliasEvaluationContext;
import com.fs.pxe.bpel.runtime.msgs.Messages;
import com.fs.pxe.bpel.epr.WSAEndpoint;
import com.fs.utils.ArrayUtils;
import com.fs.utils.ObjectPrinter;
import com.fs.utils.msg.MessageBundle;

/**
 * Implementation of a BPEL process.
 */
public class BpelProcess {
  static final Log __log = LogFactory.getLog(BpelProcess.class);

  private static final Messages __msgs = MessageBundle
      .getMessages(Messages.class);

  private final Map<OPartnerLink, PartnerLinkPartnerRoleImpl> _partnerRoles = new HashMap<OPartnerLink, PartnerLinkPartnerRoleImpl>();

  private Map<OPartnerLink, PartnerLinkMyRoleImpl> _myRoles = new HashMap<OPartnerLink, PartnerLinkMyRoleImpl>();

  /**
   * MyRole Endpoint reference to partner link map.
   */
  private final Map<EndpointReference, PartnerLinkMyRoleImpl> _eprToMyRole = new HashMap<EndpointReference, PartnerLinkMyRoleImpl>();

  final DebuggerSupport _debugger;

  final OProcess _oprocess;

  final ExpressionLanguageRuntimeRegistry _expLangRuntimeRegistry;

  final ReplacementMap _replacementMap;

  final BpelEngineImpl _engine;

  final QName _pid;

  /** Mapping from "Service Name" (QNAME) to a myrole. */
  private Map<QName, PartnerLinkMyRoleImpl> _serviceMap  = 
    new HashMap<QName,PartnerLinkMyRoleImpl>();

  private Map<Integer,Element> _myEprs;
    
    
  private Set<EndpointReference> _myEndpoints = new HashSet<EndpointReference>();

  public BpelProcess(BpelEngineImpl engine, QName pid, 
      OProcess oprocess,
      Map<Integer,QName> serviceNames,
      Map<Integer,Element> myEprs,
      BpelEventListener debugger,
      ExpressionLanguageRuntimeRegistry expLangRuntimeRegistry) {
    _pid = pid;
    _engine = engine;
    _replacementMap = new ReplacementMapImpl(oprocess);
    _oprocess = oprocess;
    _myEprs = myEprs;
    _expLangRuntimeRegistry = expLangRuntimeRegistry;

    for (OPartnerLink pl : _oprocess.getAllPartnerLinks()) {
      if (pl.hasMyRole()) {
        PartnerLinkMyRoleImpl myRole = new PartnerLinkMyRoleImpl(pl);
        _myRoles.put(pl, myRole);
        // TODO: We will need to allow this naming to be user-selected 
        // in order to support versioned processes.
        QName serviceName = serviceNames.get(pl.getId());
        if (serviceName == null)
          throw new NullPointerException("No service names for plink " + pl);
        _serviceMap.put(serviceName, myRole);
      }

      if (pl.hasPartnerRole()) {
        PartnerLinkPartnerRoleImpl partnerRole = new PartnerLinkPartnerRoleImpl(
            pl);
        _partnerRoles.put(pl, partnerRole);
      }
    }
    _debugger = new DebuggerSupport(this);
  }

  public void registerEPR(EndpointReference epr, OPartnerLink myrolePartnerLink) {
    PartnerLinkMyRoleImpl mrpl = _myRoles.get(myrolePartnerLink);
    if (mrpl == null)
      throw new IllegalArgumentException("No myrole for partner link: "
          + myrolePartnerLink);
    _eprToMyRole.put(epr, mrpl);
    __log.debug(this + " mapped EPR " + epr + " --> " + myrolePartnerLink);
  }

  static String generateMessageExchangeIdentifier(String partnerlinkName,
      String operationName) {
    StringBuffer sb = new StringBuffer(partnerlinkName);
    sb.append('.');
    sb.append(operationName);
    return sb.toString();
  }

  
  void invokeProcess(QName serviceName, EndpointReference epr, MyRoleMessageExchangeImpl mex) {
    PartnerLinkMyRoleImpl target = _serviceMap.get(serviceName);
    if (target == null) {
      String errmsg = __msgs.msgMyRoleRoutingFailure(mex.getMessageExchangeId());
      __log.error(errmsg);
      mex.setFailure(MessageExchange.FailureType.UNKNOWN_ENDPOINT,
          errmsg, epr.toXML().getDocumentElement());
      return;
    }
    
    mex.getDAO().setProcess(getProcessDAO());
    mex.setProcess(_oprocess);
    target.invokeProcess(mex);
  }
  
  void initMyRoleMex(MyRoleMessageExchangeImpl mex) {
    PartnerLinkMyRoleImpl target = _serviceMap.get(mex.getServiceName());
    if (target != null) {
      mex.setPortOp(target._plinkDef.myRolePortType,target._plinkDef.getMyRoleOperation(mex.getOperationName()));
    }
  }
  
  /**
   * Extract the value of a BPEL property from a BPEL messsage variable.
   * 
   * @param msgData
   *          message variable data
   * @param alias
   *          alias to apply
   * @param target
   *          description of the data (for error logging only)
   * @return value of the property
   * @throws FaultException
   */
  String extractProperty(Element msgData, OProcess.OPropertyAlias alias,
      String target) throws FaultException {

    PropertyAliasEvaluationContext ectx = new PropertyAliasEvaluationContext(
        msgData, alias);
    Node lValue = ectx.getRootNode();

    if (alias.location != null)
      try {
        lValue = _expLangRuntimeRegistry.evaluateNode(alias.location, ectx);
      } catch (EvaluationException ec) {
        throw new FaultException(_oprocess.constants.qnSelectionFailure, alias
            .getDescription());
      }

    if (lValue == null) {
      String errmsg = __msgs.msgPropertyAliasReturnedNullSet(alias
          .getDescription(), target);
      if (__log.isErrorEnabled()) {
        __log.error(errmsg);
      }
      throw new FaultException(_oprocess.constants.qnSelectionFailure, errmsg);
    }

    if (lValue.getNodeType() == Node.ELEMENT_NODE) {
      // This is a bit hokey, we concatenate all the children's values; we
      // really should be checking to make sure that we are only dealing with
      // text and attribute nodes.
      StringBuffer val = new StringBuffer();
      NodeList nl = lValue.getChildNodes();
      for (int i = 0; i < nl.getLength(); ++i) {
        Node n = nl.item(i);
        val.append(n.getNodeValue());
      }
      return val.toString();
    } else if (lValue.getNodeType() == Node.TEXT_NODE) {
      return ((Text) lValue).getWholeText();
    } else
      return null;
  }

  /**
   * Get the element name for a given WSDL part. If the part is an
   * <em>element</em> part, the name of that element is returned. If the part
   * is an XML schema typed part, then the name of the part is returned in the
   * null namespace.
   * 
   * @param part
   *          WSDL {@link javax.wsdl.Part}
   * @return name of element containing said part
   */
  static QName getElementNameForPart(OMessageVarType.Part part) {
    return (part.type instanceof OElementVarType) ? ((OElementVarType) part.type).elementType
        : new QName(null, part.name);
  }

  /** Create a version-appropriate runtime context. */
  private BpelRuntimeContextImpl createRuntimeContext(ProcessInstanceDAO dao,
      PROCESS template, MyRoleMessageExchangeImpl instantiatingMessageExchange) {
    return new BpelRuntimeContextImpl(this, dao, template,
        instantiatingMessageExchange);
  }

  /**
   * Replacement object for serializtation of the {@link OBase} (compiled BPEL)
   * objects in the JACOB VPU.
   */
  public static final class OBaseReplacementImpl implements Externalizable {
    private static final long serialVersionUID = 1L;

    int _id;

    public OBaseReplacementImpl() {
    }

    public OBaseReplacementImpl(int id) {
      _id = id;
    }

    public void readExternal(ObjectInput in) throws IOException {
      _id = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeInt(_id);
    }

  }

  private abstract class PartnerLinkRoleImpl {
    protected OPartnerLink _plinkDef;

    PartnerLinkRoleImpl(OPartnerLink plink) {
      _plinkDef = plink;
    }

    String getPartnerLinkName() {
      return _plinkDef.name;
    }
  }

  class PartnerLinkMyRoleImpl extends PartnerLinkRoleImpl {

    PartnerLinkMyRoleImpl(OPartnerLink plink) {
      super(plink);
    }

    public String toString() {
      StringBuffer buf = new StringBuffer("PartnerLinkRole-");
      buf.append(_plinkDef.name);
      buf.append('.');
      buf.append(_plinkDef.myRoleName);
      return buf.toString();
    }

    /**
     * Called when an input message has been received.
     * 
     * @param mex
     *          exchange to which the message is related
     */
    public void invokeProcess(MyRoleMessageExchangeImpl mex) {
      if (__log.isTraceEnabled()) {
        __log.trace(ObjectPrinter.stringifyMethodEnter(this + ":inputMsgRcvd",
            new Object[] { "messageExchange", mex }));
      }

      Operation operation = getMyRoleOperation(mex
          .getOperationName());
      if (operation == null) {
        __log.error(__msgs.msgUnknownOperation(mex
            .getOperationName(), _plinkDef.myRolePortType.getQName()));
        mex.setFailure(FailureType.UNKNOWN_OPERATION,
            mex.getOperationName(), null);
        return;
      }

      mex.getDAO().setPartnerLinkModelId(_plinkDef.getId());
      mex.setPortOp(_plinkDef.myRolePortType, operation);
      mex.setPattern(operation.getOutput() == null 
          ? MessageExchangePattern.REQUEST_ONLY 
          : MessageExchangePattern.REQUEST_RESPONSE);

      // Is this a /possible/ createInstance Operation?
      boolean isCreateInstnace = _plinkDef.isCreateInstanceOperation(operation);

      // now, the tricks begin: when a message arrives we have to see if there
      // is anyone waiting for it.
      // Get the correlator, a persisted communnication-reduction data structure
      // supporting correlation correlationKey matching!
      String correlatorId = genCorrelatorId(_plinkDef, operation.getName());

      CorrelatorDAO correlator = getProcessDAO().getCorrelator(correlatorId);

      CorrelationKey[] keys;
      MessageRouteDAO messageRoute = null;

      // We need to compute the correlation keys (based on the operation we can
      // infer which correlation keys to compute - this is merely a set
      // consisting of each correlationKey used in each correlation sets that is
      // ever
      // referenced in an <receive>/<onMessage> on this partnerlink/operation.
      keys = computeCorrelationKeys(mex);

      if (__log.isDebugEnabled()) {
        __log.debug("INPUTMSG: " + correlatorId + ": MSG RCVD keys="
            + ArrayUtils.makeCollection(HashSet.class, keys));
      }

      CorrelationKey matchedKey = null;

      // Try to find a route for one of our keys.
      for (CorrelationKey key : keys) {
        messageRoute = correlator.findRoute(key);
        if (messageRoute != null) {
          if (__log.isDebugEnabled()) {
            __log.debug("INPUTMSG: " + correlatorId + ": ckey " + key
                + " route is to " + messageRoute);
          }
          matchedKey = key;
          break;
        }
      }
      // Handling the "opaque correlation case": correlation is done on a
      // session identifier  associated with my epr
      if (messageRoute == null) {
        EndpointReference ref = mex.getEndpointReference();
        // Stateful interactions are only supported with WSA endpoints
        if (ref != null && ref instanceof WSAEndpoint) {
          String sessionId = ((WSAEndpoint)ref).getSessionId();
          CorrelationKey key = new CorrelationKey(-1,
              new String[] { sessionId });
          messageRoute = correlator.findRoute(key);
        }
      }

      // TODO - PXE-58

      // If no luck try to find a default route.
      if (messageRoute == null) {
        if (__log.isDebugEnabled()) {
          __log.debug("INPUTMSG: " + correlatorId
              + ": CorrelationKey routing failed; checking default route.");
        }

        messageRoute = correlator.findRoute(null);

        if (__log.isDebugEnabled()) {
          __log.debug("INPUTMSG: " + correlatorId + ": default route is to "
              + messageRoute);
        }
      }

      // If still no luck, and this operation qualifies for create-instance
      // treatment,
      // then create a new process instance.
      if (messageRoute == null && isCreateInstnace) {
        if (__log.isDebugEnabled()) {
          __log.debug("INPUTMSG: " + correlatorId
              + ": default routing failed, CREATING NEW INSTANCE");
        }
        ProcessDAO processDAO = getProcessDAO();
        if (processDAO.isRetired()) {
          throw new InvalidProcessException("Process is retired.",
              InvalidProcessException.RETIRED_CAUSE_CODE);
        }
        ProcessInstanceDAO newInstance = processDAO.createInstance(correlator);
        BpelRuntimeContextImpl instance = createRuntimeContext(newInstance,
            new PROCESS(_oprocess), mex);

        // send process instance event
        NewProcessInstanceEvent evt = new NewProcessInstanceEvent(new QName(
            _oprocess.targetNamespace, _oprocess.getName()), getProcessDAO()
            .getProcessId(), newInstance.getInstanceId());
        evt.setPortType(mex.getPortType().getQName());
        evt.setOperation(operation.getName());
        evt.setMexId(mex.getMessageExchangeId());
        _debugger.onEvent(evt);
        newInstance.insertBpelEvent(evt);
        mex.setCorrelationStatus(CorrelationStatus.CREATE_INSTANCE);
        mex.getDAO().setInstance(newInstance);
        
        instance.execute();
      } else if (messageRoute != null) {
        if (__log.isDebugEnabled()) {
          __log.debug("INPUTMSG: " + correlatorId + ": ROUTING to instance "
              + messageRoute.getTargetInstance().getInstanceId());
        }

        // Reload process instance for DAO.
        BpelRuntimeContextImpl instance = createRuntimeContext(messageRoute
            .getTargetInstance(), null, null);
        instance.inputMsgMatch(messageRoute.getGroupId(), messageRoute
            .getIndex(), mex);

        // Kill the route so some new message does not get routed to same
        // process
        // instance.
        correlator.removeRoutes(messageRoute.getGroupId(), messageRoute
            .getTargetInstance());

        Long iid = messageRoute.getTargetInstance().getInstanceId();

        // send process instance event
        CorrelationMatchEvent evt = new CorrelationMatchEvent(new QName(
            _oprocess.targetNamespace, _oprocess.getName()), getProcessDAO()
            .getProcessId(), iid, matchedKey);
        evt.setPortType(mex.getPortType().getQName());
        evt.setOperation(operation.getName());
        evt.setMexId(mex.getMessageExchangeId());

        _debugger.onEvent(evt);
        // store event
        getProcessDAO().getInstance(iid).insertBpelEvent(evt);

        mex.setCorrelationStatus(CorrelationStatus.MATCHED);
        mex.getDAO().setInstance(messageRoute.getTargetInstance());

        // run the vpu
        instance.execute();
      } else {
        if (__log.isDebugEnabled()) {
          __log.debug("INPUTMSG: " + correlatorId
              + ": SAVING to DB (no match) ");
        }

        // send event
        CorrelationNoMatchEvent evt = new CorrelationNoMatchEvent(
            mex.getPortType().getQName(), mex
                .getOperation().getName(), mex
                .getMessageExchangeId(), keys);

        evt.setProcessId(getProcessDAO().getProcessId());
        evt.setProcessName(new QName(_oprocess.targetNamespace, _oprocess
            .getName()));
        _debugger.onEvent(evt);

        mex.setCorrelationStatus(CorrelationStatus.QUEUED);

        // No match, means we add message exchange to the queue.
        correlator.enqueueMessage(mex.getDAO(), keys);

      }
      

      // Now we have to update our message exchange status. If the <reply> was
      // not hit during the invocation, then we will be in the "REQUEST" phase
      // which means that either this was a one-way or a two-way that needs to
      // delivery the reply asynchronously.
      if (mex.getStatus() == Status.REQUEST) {
        switch (mex.getPattern()) {
        case REQUEST_ONLY:
          mex.setStatus(Status.ONE_WAY);
          break;
        case REQUEST_RESPONSE:
          mex.setStatus(Status.ASYNC);
          break;
        default:
          String errmsg = "BpelProcess: internal error, message exchange pattern not set";
          __log.fatal(errmsg);
          throw new BpelEngineException(errmsg);
        }
      }

    }

    @SuppressWarnings("unchecked")
    private Operation getMyRoleOperation(String operationName) {
      Operation op = _plinkDef.getMyRoleOperation(operationName);
      return op;
    }

    private CorrelationKey[] computeCorrelationKeys(
        MyRoleMessageExchangeImpl mex) {
      Operation operation = mex.getOperation();
      Element msg = mex.getRequest().getMessage();
      Message msgDescription = operation.getInput().getMessage();
      List<CorrelationKey> keys = new ArrayList<CorrelationKey>();

      Set<OScope.CorrelationSet> csets = _plinkDef
          .getCorrelationSetsForOperation(operation);

      for (Iterator<OScope.CorrelationSet> i = csets.iterator(); i.hasNext();) {
        OScope.CorrelationSet cset = i.next();
        CorrelationKey key = computeCorrelationKey(cset, _oprocess.messageTypes
            .get(msgDescription.getQName()), msg);
        keys.add(key);
      }

      return keys.toArray(new CorrelationKey[keys.size()]);
    }

    private CorrelationKey computeCorrelationKey(OScope.CorrelationSet cset,
        OMessageVarType messagetype, Element msg) {
      String[] values = new String[cset.properties.size()];

      int jIdx = 0;
      for (Iterator j = cset.properties.iterator(); j.hasNext(); ++jIdx) {
        OProcess.OProperty property = (OProcess.OProperty) j.next();
        OProcess.OPropertyAlias alias = property.getAlias(messagetype);

        if (alias == null) {
          // TODO: Throw a real exception! And catch this at compile time.
          throw new IllegalArgumentException("No alias matching property '"
              + property.name + "' with message type '" + messagetype + "'");
        }

        String value;
        try {
          value = extractProperty(msg, alias, msg.toString());
        } catch (FaultException fe) {
          String emsg = __msgs.msgPropertyAliasDerefFailedOnMessage(alias
              .getDescription(), fe.getMessage());
          __log.error(emsg, fe);
          throw new InvalidMessageException(emsg, fe);
        }
        values[jIdx] = value;
      }

      CorrelationKey key = new CorrelationKey(cset.getId(), values);
      return key;
    }

  }

  private class PartnerLinkPartnerRoleImpl extends PartnerLinkRoleImpl {
    private PartnerLinkPartnerRoleImpl(OPartnerLink plink) {
      super(plink);
    }

    public void processPartnerResponse(
        PartnerRoleMessageExchangeImpl messageExchange) {
      if (__log.isDebugEnabled()) {
        __log.debug("Processing partner's response for partnerLink: "
            + messageExchange);
      }

      BpelRuntimeContextImpl processInstance = createRuntimeContext(
          messageExchange.getDAO().getInstance(), null, null);
      processInstance.invocationResponse(messageExchange);
      processInstance.execute();
    }
  }

  /**
   * @see com.fs.pxe.bpel.engine.BpelProcess#handleWorkEvent(java.io.Serializable)
   */
  public void handleWorkEvent(Map<String, Object> jobData) {
    ProcessInstanceDAO procInstance;

    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("handleWorkEvent",
          new Object[] { "jobData", jobData }));
    }

    WorkEvent we = new WorkEvent(jobData);
    procInstance = getProcessDAO().getInstance(we.getIID());
    if (procInstance == null) {
      if (__log.isDebugEnabled()) {
        __log.debug("handleWorkEvent: no ProcessInstance found with iid "
            + we.getIID() + "; ignoring.");
      }
      return;
    }

    BpelRuntimeContextImpl processInstance = createRuntimeContext(procInstance,
        null, null);
    switch (we.getType()) {
    case TIMER:
      if (__log.isDebugEnabled()) {
        __log.debug("handleWorkEvent: TimerWork event for process instance "
            + processInstance);
      }

      processInstance.timerEvent(we.getChannel());
      break;
    case RESUME:
      if (__log.isDebugEnabled()) {
        __log.debug("handleWorkEvent: ResumeWork event for iid " + we.getIID());
      }


      processInstance.execute();

      break;
    case INVOKE_RESPONSE:
      if (__log.isDebugEnabled()) {
        __log.debug("InvokeResponse event for iid " + we.getIID());
      }

      processInstance.invocationResponse(we.getMexId(), we.getChannel());
      processInstance.execute();
      break;
    }
  }

  ProcessDAO getProcessDAO() {
    return _engine._contexts.dao.getConnection().getProcess(_pid);
  }

  static String genCorrelatorId(OPartnerLink plink, String opName) {
    return plink.getId() + "." + opName;
  }

  /**
   * Get all the services that are implemented by this process.
   * @return list of qualified names corresponding to the myroles. 
   */
  public Set<QName> getServiceNames() {
    return _serviceMap.keySet();
  }

  void activate() {
    for (QName svc : _serviceMap.keySet()) {
      PartnerLinkMyRoleImpl pm = _serviceMap.get(svc);
      _myEndpoints.add(_engine._contexts.eprContext.activateEndpoint(_pid,svc,_myEprs.get(pm._plinkDef.getId())));
    }
  }
  
  void deactivate() {
    for (EndpointReference ref : _myEndpoints) 
      _engine._contexts.eprContext.deactivateEndpoint(ref);
  }


  
}
