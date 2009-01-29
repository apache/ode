package org.apache.ode.bpel.iapi;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Outgoing RESTful message exchange implemented by the engine and used by IL implementations
 * that can provide RESTful interactions.
 */
public interface RESTOutMessageExchange extends MessageExchange {

    Resource getTargetResource();

    Message getRequest();

    void reply(Message response) throws BpelEngineException;

    void replyWithFailure(MessageExchange.FailureType type, String description, Element details) throws BpelEngineException;

    void replyWithFault(QName faultType, Message outputFaultMessage) throws BpelEngineException;

    void replyOneWayOk();
}
