package com.fs.pxe.bpel.engine;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.fs.pxe.bpel.dao.MessageDAO;
import com.fs.pxe.bpel.dao.MessageExchangeDAO;
import com.fs.pxe.bpel.iapi.BpelEngineException;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.MessageExchange;
import com.fs.pxe.bpel.o.OProcess;
import com.fs.utils.msg.MessageBundle;

abstract class MessageExchangeImpl implements MessageExchange {
  
  private static final Log __log = LogFactory.getLog(MessageExchangeImpl.class);
  protected static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  /** Process-Instance identifier.*/
  protected Long _iid;
  
  protected OProcess _oprocess;
  protected PortType _portType;
  protected Operation _operation;

  protected final BpelEngineImpl _engine;

  protected EndpointReference _epr;

  protected EndpointReference _callbackEpr;

  protected final MessageExchangeDAO _dao;
  
  /**
   * Constructor: requires the minimal information for a message exchange.
   * @param pattern
   * @param opname
   * @param epr
   */
  MessageExchangeImpl(BpelEngineImpl engine,
      MessageExchangeDAO dao,
      MessageExchangePattern pattern, 
      String opname, 
      EndpointReference epr) {
    _engine = engine;
    _dao = dao;
    _epr = epr;

    getDAO().setPattern(pattern.toString());
    getDAO().setOperation(opname);
    if (epr != null)
      getDAO().setEPR(epr.toXML().getDocumentElement());
  }
  
  public MessageExchangeImpl(BpelEngineImpl engine,
      MessageExchangeDAO dao) {
    _engine = engine;
    _dao = dao;
  }
  
 
  public String getMessageExchangeId() throws BpelEngineException {
    return getDAO().getMessageExchangeId();
  }

  public String getOperationName() throws BpelEngineException {
    return getDAO().getOperation();
  }

  public MessageExchangePattern getMessageExchangePattern() {
    return MessageExchangePattern.valueOf(getDAO().getPattern());
  }

  public boolean isTransactionPropagated() throws BpelEngineException {
    return getDAO().getPropagateTransactionFlag();
  }

  public boolean isFault() {
    // TODO
    return false;
  }

  public boolean isDone() {
    // TODO
    return false;
  }

  public Message getResponse() {
    return new MessageImpl(getDAO().getResponse());
  }

  public String getFault() {
    return getDAO().getFault();
  }

  public Message getFaultResponse() {
    return getResponse();
  }


  public MessageExchangePattern getPattern() {
    return MessageExchangePattern.valueOf(getDAO().getPattern());
  }
  
  public Status getStatus() {
    return Status.valueOf(getDAO().getStatus());
  }
  
  public Message getRequest() {
    return new MessageImpl(getDAO().getRequest());
  }

  public Operation getOperation() {
    return _operation;
  }

  

  public PortType getPortType() {
    return _portType;
  }

  /**
   * Associate message exchange with a process. 
   * @param oprocess
   */
  void setProcess(OProcess oprocess) {
    if (__log.isTraceEnabled())
      __log.trace("Mex[" + getMessageExchangeId() + "].setProcess("+oprocess+")");
    _oprocess = oprocess;
  }


  /**
   * Update the pattern of this message exchange. 
   * @param pattern
   */
  void setPattern(MessageExchangePattern pattern) {
    if (__log.isTraceEnabled())
      __log.trace("Mex[" + getMessageExchangeId() + "].setPattern("+pattern+")");
    getDAO().setPattern(pattern.toString());
  }
  

  void setPortOp(PortType portType, Operation operation) {
    if (__log.isTraceEnabled())
      __log.trace("Mex[" + getMessageExchangeId()  + "].setPortOp("+portType+","+operation+")");
    _portType = portType;
    _operation = operation;
  }

  protected MessageExchangeDAO getDAO() {
    return _dao;
  }

  void setFault(String faultType, Message outputFaultMessage) throws BpelEngineException {
    if (getStatus() != Status.REQUEST)
      throw new IllegalStateException("Not in REQUEST state!");
    
    setStatus(Status.FAULT);
    getDAO().setFault(faultType);
    getDAO().setResponse(((MessageImpl)outputFaultMessage)._dao);
  }

  void setResponse(Message outputMessage) throws BpelEngineException {
    if (getStatus() != Status.REQUEST && getStatus()!=Status.ASYNC)
      throw new IllegalStateException("Not in REQUEST state!");
   
    setStatus(Status.RESPONSE);
    getDAO().setFault(null);
    getDAO().setResponse(((MessageImpl)outputMessage)._dao);
  }

  void setFailure(FailureType type, String description, Element details) throws BpelEngineException {
    setStatus(Status.FAILURE);
//    getDAO().setFailureMessage(description);
//    getDAO().setFailureDetails(details);
  }

  void setStatus(Status status) {
    getDAO().setStatus(status.toString());
  }

  public Message createMessage(javax.xml.namespace.QName msgType) {
    MessageDAO mdao = getDAO().createMessage(msgType);
    return new MessageImpl(mdao);
  }

  public void setEndpointReference(EndpointReference ref) {
    _epr = ref;
    if (ref != null)
      getDAO().setEPR(ref.toXML().getDocumentElement());
  }

  public EndpointReference getEndpointReference() throws BpelEngineException {
    if (_epr != null) return _epr;
    if (getDAO().getEPR() == null)
      return null;

    return _epr = _engine._contexts.eprContext.resolveEndpointReference(getDAO().getEPR());
  }

  public void setCallbackEndpointReference(EndpointReference ref) {
    _callbackEpr = ref;
    if (ref != null)
      getDAO().setCallbackEPR(ref.toXML().getDocumentElement());
  }

  public EndpointReference getCallbackEndpointReference() throws BpelEngineException {
    if (_callbackEpr != null) return _callbackEpr;
    if (getDAO().getCallbackEPR() == null)
      return null;

    return _callbackEpr = _engine._contexts.eprContext.resolveEndpointReference(getDAO().getCallbackEPR());
  }

  QName getServiceName() {
    return getDAO().getCallee();
  }

  public String getProperty(String key) {
    return getDAO().getProperty(key);
  }

  public void setProperty(String key, String value) {
    getDAO().setProperty(key,value);
  }
  
  
  
  
}
