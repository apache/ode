/*
* File:      $RCSfile$
* Copyright: (C) 1999-2005 FiveSight Technologies Inc.
*
*/
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.common.CorrelationKey;
import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.evt.ProcessInstanceEvent;
import com.fs.pxe.bpel.o.OMessageVarType;
import com.fs.pxe.bpel.o.OMessageVarType.Part;
import com.fs.pxe.bpel.o.OPartnerLink;
import com.fs.pxe.bpel.o.OProcess;
import com.fs.pxe.bpel.o.OScope;
import com.fs.pxe.bpel.runtime.channels.FaultData;
import com.fs.pxe.bpel.runtime.channels.InvokeResponseChannel;
import com.fs.pxe.bpel.runtime.channels.PickResponseChannel;
import com.fs.pxe.bpel.runtime.channels.TimerResponseChannel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.wsdl.Operation;
import java.util.Collection;
import java.util.Date;

import javax.xml.namespace.QName;

/**
 * <p>A facade for accessing all the BPEL functionality that is not implemented
 * directly in the JACOB framework, but rather "natively" . Things that are so
 * implemented include variables (i.e. the JACOB state-space does not include
 * dimensions for variables, these are instead implemented as database rows of
 * some sort), the communication activities pick/receive/invoke (i.e. there
 * are no JACOB channels representing partnerLink links), and correlation (i.e.
 * the JACOB objects do not match message to process instances, this happens
 * in this <em>native</em> layer).</p>  
 */
public interface BpelRuntimeContext {

  Long getPid();
  
  /**
   * Checks for variable initialization, i.e. has had a 'write'
   *
   * @param variable variable
   *
   * @return <code>true</code> if initialized
   */
  boolean isVariableInitialized(VariableInstance variable);

  /**
   * Checks for endpoint reference initialization, i.e. has had a 'write'
   * @param variable variable
   * @return <code>true</code> if initialized
   */
  boolean isEndpointReferenceInitialized(PartnerLinkInstance pLink, boolean isMyEpr);

  /**
   * Create a scope instance object.
   * @param parentScopeId _id of parent scope (null if root scope)
   * @param scopeType the type of scope, i.e. the name of the scope
   *
   * @return scope instance identifier
   */
  Long createScopeInstance(Long parentScopeId, OScope scopeType);

  /**
   * Initializes endpoint references for partner links inside a scope.
   * @param parentScopeId
   * @param partnerLinks
   */
  void initializePartnerLinks(Long parentScopeId, Collection<OPartnerLink> partnerLinks);

  /**
   *
   * @param var variable to read
   * @return
   */
  Node fetchVariableData(VariableInstance var, boolean forWriting)
          throws FaultException;

  Node fetchVariableData(VariableInstance var, OMessageVarType.Part partname, boolean forWriting)
          throws FaultException;

  /**
   * Fetches an endpoint reference.
   * @param pLink
   * @param isMyEPR
   * @return
   * @throws FaultException
   */
  Element fetchEndpointReferenceData(PartnerLinkInstance pLink, boolean isMyEPR) throws FaultException;

  /**
   * Fetches the session id associated with an endpoint reference (if there's one).
   * @param pLink
   * @param isMyEPR
   * @return session id
   * @throws FaultException
   */
  String fetchEndpointSessionId(PartnerLinkInstance pLink, boolean isMyEPR) throws FaultException;

  /**
   * Evaluate a property alias query expression against a variable, returning the normalized
   * {@link String} representation of the property value.
   * @param var variable to read
   * @param property property to read
   * @return value of property for variable, in String form
   * @throws FaultException in case of selection or other fault
   */
  String readProperty(VariableInstance var, OProcess.OProperty property)
          throws FaultException;

  Node initializeVariable(VariableInstance var, Node initData);

  Element writeEndpointReference(PartnerLinkInstance variable, Element data) throws FaultException;

  /**
   * Update (potentially partially) a partner EPR by consolidating the data received
   * during a session-based interaction (message exchanges with session information)
   * with the data we already have.
   * @param variable
   * @param data
   * @return the updated endpoint
   * @throws FaultException
   */
  Element updatePartnerEndpointReference(PartnerLinkInstance variable, Element data) throws FaultException;

  Node convertEndpointReference(Element epr, Node targetNode);

  void commitChanges(VariableInstance var, Node changes);

  boolean isCorrelationInitialized(CorrelationSetInstance cset);

  CorrelationKey readCorrelation(CorrelationSetInstance cset);

  void writeCorrelation(CorrelationSetInstance cset, CorrelationKey correlation);

  /**
   * Should be invoked by process template, signalling process completion
   * with no faults.
   *
   */
  void completedOk();

  /**
   * Should be invoked by process template, signalling process completion
   * with fault.
   */
  void completedFault(FaultData faultData);

 
  /**
   * Non-deterministic selection on incoming message-exchanges.
   */
  void select(PickResponseChannel response, Date timeout, boolean createInstnace, 
      Selector[] selectors) throws FaultException;

  /**
   * Cancel a timer, or pick.
   * @param timerResponseChannel
   */
  void cancel(TimerResponseChannel timerResponseChannel);

  /**
   * Send a reply to an open message-exchange.
   * @param msg response message
   * @param fault fault name, if this is a fault reply, otherwise <code>null</code>
   */
  void reply(PartnerLinkInstance plink, String opName, String mexId, Element msg, 
      String fault)
      throws FaultException;


  String invoke(PartnerLinkInstance partnerLinkInstance, 
      Operation operation, 
      Element outboundMsg, 
      InvokeResponseChannel invokeResponseChannel) throws FaultException;


  /**
   * Registers a timer for future notification.
   * @param timerChannel channel for timer notification
   * @param timeToFire future time to fire timer notification
   */
  void registerTimer(TimerResponseChannel timerChannel, Date timeToFire);
  
  /**
   * Terminates the process / sets state flag to terminate
   * and ceases all processing on the VPU.
   */
  void terminate();

  /**
   * Sends the bpel event.
   * @param event
   */
  void sendEvent(ProcessInstanceEvent event);

  ExpressionLanguageRuntimeRegistry getExpLangRuntime();

 
  /**
   * Generate a unique (and monotonic) ID in the context of this instance.
   * @return
   */
  long genId();

  Element getPartnerResponse(String mexId);

  Element getMyRequest(String mexId);

  QName getPartnerFault(String mexId);

  QName getPartnerResponseType(String mexId);

  Node getPartData(Element message, Part part);

  Element getSourceEPR(String mexId);

}
