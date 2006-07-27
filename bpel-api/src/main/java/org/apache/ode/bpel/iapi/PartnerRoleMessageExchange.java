package org.apache.ode.bpel.iapi;


import javax.xml.namespace.QName;

import org.w3c.dom.Element;

public interface PartnerRoleMessageExchange extends MessageExchange {


  /**
   * Get the identifier of the process that created this message exchange.
   * @return
   */
  QName getCaller();
  
  /**
   * Indicate that the partner faulted in processing the message exchange.
   * 
   * @param faultType fault type
   * @param outputFaultMessage the input message
   *
   * @throws IllegalStateException if delivering this type of message is
   *         inappropriate at the present point.
   */
  void replyWithFault(String faultType, Message outputFaultMessage)
    throws BpelEngineException;

  /**
   * Indicate that the partner has responded to the message exchange.
   * 
   * @param response the response from the partner
   *
   * @throws IllegalStateException if delivering this type of message is
   *         inappropriate at the present point.
   */
  void reply(Message response)
    throws BpelEngineException;

  /**
   * Indicate that the partner has failed to process the message exchange. 
   * 
   * @param type type of failure
   * @param description description of failure
   */
  void replyWithFailure(FailureType type, String description, Element details) 
    throws BpelEngineException;  
  
  /**
   * Indicate that the partner processed the one-way invocation successfully.
   */
  void replyOneWayOk();
  
  /**
   * Indicate that the response to the request/response operation 
   * is not yet available and that the response will be delivered
   * asynchronously.
   */
  void replyAsync();



}
