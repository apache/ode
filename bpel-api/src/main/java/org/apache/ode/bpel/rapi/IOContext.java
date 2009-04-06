package org.apache.ode.bpel.rapi;

import java.util.Date;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.FaultException;
import org.w3c.dom.Element;

public interface IOContext {

    /**
     * Non-deterministic "select" (used to implement receive/pick) and the like. Calling this method will request that the engine
     * wait for the first message on a certain set of input ports.
     * 
     * @param selectId
     *            client specified identifier for this operation.
     * @param timeout
     *            how long to wait for a response.
     * @param selectors
     *            the criteria for messages to receive
     */
    void select(String selectId, Date timeout, Selector[] selectors);

    /**
     * Cancel a previously issue {@link #select(String, Date, Selector[])) call. 
     * @param selectId identifier for the select. 
     * @return <code>true</code> if select-id was found/cancelled. 
     */
    void cancelSelect(String selectId);
  
    /**
     * Send a reply to an open message-exchange.
     * 
     * TODO: remove plink paramater, should not be needed. 
     * 
     * @param mexId mex id to reply to
     * @param plink partner link on which we are replying (TODO: remove) 
     * @param opName operation name
     * @param msg reply message
     * @param fault fault type, or <code>null</code> if no fault
     * @throws NoSuchOperationException
     */
    void reply(String mexId, PartnerLink plink, String opName, Element msg, QName fault) throws NoSuchOperationException;

    /**
     * Invoke a partner.
     * 
     * @param invokeId request identifier
     * @param partnerLinkInstance partner link (on which to communicate)
     * @param operation operation to invoke
     * @param outboundMsg outgoing message
     * @return message exchange identifier
     * @throws UninitializedPartnerEPR
     * @throws FaultException
     */
    String /* MexId */invoke(String invokeId, PartnerLink partnerLinkInstance, Operation operation, Element outboundMsg)
            throws UninitializedPartnerEPR;

    /**
     * Get partner's response to an invoke.
     * 
     * @param mexId message exchange identifier
     * @return partner's reply
     */
    Element getPartnerResponse(String mexId);

    /**
     * Get partner's fault response to an invoke, or <code>null</code> if response was not a fault.
     * 
     * @param mexId message exchange identifier
     * @return partner's fault reply.
     */
    QName getPartnerFault(String mexId);

    String getPartnerFaultExplanation(String mexId);

    QName getPartnerResponseType(String mexId);

    /**
     * Get the request (i.e. a message received) received from a partner's invoke.
     * 
     * @param mexId message exchange identifier
     * @return
     */
    Element getMyRequest(String mexId);

    void releasePartnerMex(String mexId, boolean instanceSucceeded);

    Element getSourceEPR(String mexId);

    String getSourceSessionId(String mexId);

    /**
     * Registers a timer for future notification.
     * 
     * @param timerChannel
     *            channel for timer notification
     * @param timeToFire
     *            future time to fire timer notification
     */
    void registerTimer(String timerId, Date timeToFire);

    /**
     * Cancel a timer.
     * 
     * @param timerId
     * @returns <code>true</code> if timer was found and canelled. 
     */
    boolean cancelTimer(String timerId);

}
