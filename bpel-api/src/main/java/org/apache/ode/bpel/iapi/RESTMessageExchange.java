package org.apache.ode.bpel.iapi;

/**
 * Message exchange used for a web-service based interaction between the integration layer and the
 * engine. Adds resource information.
 */
public interface RESTMessageExchange extends MessageExchange {

    Resource getResource();
}
