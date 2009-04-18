/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
    
    public int getReferenceCount(EndpointReference epr) {
    	int referenceCount = 0;
    	for (Endpoint endpoint : _eprs.keySet()) {
    		if (_eprs.get(endpoint).equals(epr)) {
    			for (Endpoint reference : _referenceCounts) {
    				if (reference.equals(endpoint)) {
    					++referenceCount;
    				}
    			}
    		}
    	}
    	return referenceCount;    	
    }
}
