package org.apache.ode.bpel.iapi;

import java.util.concurrent.TimeoutException;

/**
 * Message exchange used for a web-service based interaction between the integration layer and the
 * engine. Adds resource information.
 */
public interface RESTInMessageExchange extends MessageExchange {

    Resource getResource();

    Status invokeBlocking() throws BpelEngineException, TimeoutException;

    void setRequest(Message message);
}
