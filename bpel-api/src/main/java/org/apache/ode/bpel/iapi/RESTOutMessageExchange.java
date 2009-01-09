package org.apache.ode.bpel.iapi;

import org.w3c.dom.Element;

/**
 * Outgoing RESTful message exchange implemented by the engine and used by IL implementations
 * that can provide RESTful interactions.
 */
public interface RESTOutMessageExchange {

    Resource getTargetResource();

    Message getRequest();

    void reply(Message response) throws BpelEngineException;

    void replyWithFailure(MessageExchange.FailureType type, String description, Element details) throws BpelEngineException;

}
