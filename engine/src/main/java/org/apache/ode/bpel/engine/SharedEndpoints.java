package org.apache.ode.bpel.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;


/**
 * An in-memory map from the endpoints provided by various processes in 
 * the server to their corresponding endpoint references. 
 *
 * @author $author$
 * @version $Revision$
 */
public class SharedEndpoints {
    // Map of every endpoint provided by the server 
    private static Map<Endpoint, EndpointReference> _eprs = new HashMap<Endpoint, EndpointReference>();
    private static List<Endpoint> _referenceCounts = new ArrayList<Endpoint>();

    /**
     * Creates a new SharedEndpoints object.
     */
    public SharedEndpoints() {
        init();
    }

    /**
     * This is called when the server is initializing
     */
    public void init() {
        _eprs.clear();
        _referenceCounts.clear();
    }

    /**
     * Add an endpoint along with its corresponding EPR
     *
     * @param endpoint endpoint
     * @param epr epr
     */
    public void addEndpoint(Endpoint endpoint, EndpointReference epr) {
        _eprs.put(endpoint, epr);
    }

    /**
     * Remove an endpoint along with its EPR
     * This is called when there are no more references 
     * to this endpoint from any BPEL process 
     * (which provides a service at this endpoint)
     *
     * @param endpoint endpoint
     */
    public void removeEndpoint(Endpoint endpoint) {
        _eprs.remove(endpoint);
    }

    /**
     * Get the EPR for an endpoint
     *
     * @param endpoint endpoint
     *
     * @return type
     */
    public EndpointReference getEndpointReference(Endpoint endpoint) {
        return _eprs.get(endpoint);
    }

    /**
     * Increment the number of BPEL processes who provide 
     * a service specifically at this endpoint.
     *
     * @param endpoint endpoint
     */
    public void incrementReferenceCount(Endpoint endpoint) {
        _referenceCounts.add(endpoint);
    }

    /**
     * Decrement the number of BPEL processes who provide 
     * a service specifically at this endpoint.
     *
     * @param endpoint endpoint
     *
     * @return type
     */
    public boolean decrementReferenceCount(Endpoint endpoint) {
        return _referenceCounts.remove(endpoint);
    }
}
