package com.fs.pxe.bpel.engine;


import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import com.fs.pxe.bpel.dao.MessageExchangeDAO;
import com.fs.pxe.bpel.engine.WorkEvent.Type;
import com.fs.pxe.bpel.iapi.BpelEngineException;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.PartnerRoleMessageExchange;


class PartnerRoleMessageExchangeImpl extends MessageExchangeImpl 
  implements  PartnerRoleMessageExchange {

  PartnerRoleMessageExchangeImpl(BpelEngineImpl engine,
      MessageExchangeDAO dao,
      PortType portType,
      Operation operation,
      EndpointReference epr) {
    super(engine, dao);
    setPortOp(portType, operation);
  }
 

  public void replyOneWayOk() {
    setStatus(Status.ONE_WAY);
  }
  

  public EndpointReference getEndpointReference() throws BpelEngineException {
    if (_epr != null) return _epr;
    if (getDAO().getEPR() == null)
      return null;
    
    return _epr = _engine._contexts.eprContext.resolveEndpointReference(getDAO().getEPR());
  }

  public void replyAsync() {
    setStatus(Status.ASYNC);
  }



  public void replyWithFault(String faultType, Message outputFaultMessage) throws BpelEngineException {
    boolean isAsync = isAsync();
    setFault(faultType, outputFaultMessage);
    if (isAsync)
      continueAsync();
  }


  public void reply(Message response) throws BpelEngineException {
    boolean isAsync = isAsync();
    setResponse(response);
    if (isAsync)
      continueAsync();
    
  }

  public void replyWithFailure(FailureType type, String description, Element details) throws BpelEngineException {
    setFailure(type, description, details);
    continueAsync();
  }

  /**
   * Continue from the ASYNC state.
   *
   */
  private void continueAsync() {
    WorkEvent we = new WorkEvent();
    we.setIID(getDAO().getInstance().getInstanceId());
    we.setType(Type.INVOKE_RESPONSE);
    we.setChannel(getDAO().getChannel());
    we.setMexId(getDAO().getMessageExchangeId());
    _engine._contexts.scheduler.schedulePersistedJob(we.getDetail(),null);
  }

  /**
   * Check if we are in the ASYNC state.
   * @return
   */
  private boolean isAsync() {
    return getStatus()==Status.ASYNC;
  }


  public QName getCaller() {
    return _dao.getProcess().getProcessId();
  }
  
  public String toString() {
    try {
      return "{PartnerRoleMex#" + getMessageExchangeId() 
      + " [PID " + getCaller() + "] calling "
      + _epr + "." + getOperationName() + "(...)}";
      
    } catch (Throwable t) {
      return "{PartnerRoleMex#????}";
    }
    
  }
  
}
