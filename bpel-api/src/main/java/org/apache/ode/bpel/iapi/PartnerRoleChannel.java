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

package org.apache.ode.bpel.iapi;

import java.util.Set;

/**
 * Representation of a communication link to a partner or partners. Objects of this
 * type generally represent a physical resource in the integration layer that is used
 * to communicate with a partner or a set of partners. 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public interface PartnerRoleChannel {

    /**
     * Style of invocation supported on the given channel. 
     * 
     * @author Maciej Szefler
     */
    public enum InvocationStyle {
        /** 
         * The very ordinary blocking IO style --- the IL will block until the operation is complete, or until
         * a timeout is reached. 
         */
        BLOCKING, 
        
        /**
         * Asynchrnous style -- the IL will "queue" the invocation, and call-back asynchrnously when the response
         * is available. 
         */
        ASYNC, 
        
        /**
         * Reliable style -- the IL will queue the invocation using the current transaction. The response will be
         * delivered when available using a separate transaction. 
         */
        RELIABLE,
        
        
        /**
         * Transacted style -- the IL will enroll the operation with the current transaction. The IL will block until the
         * operation completes. 
         */
        TRANSACTED
    }

    
    Set<InvocationStyle> getSupportedInvocationStyle();
    
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
