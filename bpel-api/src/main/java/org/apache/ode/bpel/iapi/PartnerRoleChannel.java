package org.apache.ode.bpel.iapi;

/**
 * Representation of a communication link to a partner or partners. Objects of this
 * type generally represent a physical resource in the integration layer that is used
 * to communicate with a partner or a set of partners. 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public interface PartnerRoleChannel {

    /**
     * Return the endpoint reference to the endpoint with which the
     * channel was initialized or <code>null</code> if the channel
     * was initialized without an initial endpoint.
     * @return endpoint reference or null
     */
    EndpointReference getInitialEndpointReference();
    
    
    /**
     * Close the communication channel.
     */
    void close();
    
}
