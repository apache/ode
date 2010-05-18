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

import javax.wsdl.PortType;
import javax.xml.namespace.QName;


/**
 * Interface used by the BPEL Server to establish communication links with
 * the external world via the Integration Layer. This interface is implemented
 * by the integration layer.
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public interface BindingContext {
    
    /**
     * Activate a "myRole" endpoint. This is a notifaction to the integration
     * layer that the BPEL engine is interested in receiving requests for the
     * given endpoint and that the IL should establish the communication mechanisms
     * that would make this happen. 
     * @param processId
     * @param myRoleEndpoint endpoint identifer (service qname + port)
     * @returns an endpoint reference in XML  format.
     */
    EndpointReference activateMyRoleEndpoint(QName processId, Endpoint myRoleEndpoint);

    /**
     * Deactivate a "myRole" endpoint. This is a notification to the integration layer
     * that the BPEL engine is no longer interested in receiving requests for the
     * given endpoint and that the IL should tear down any communication mechanisms
     * created in {@link #activateMyRoleEndpoint(QName, Endpoint)}.
     * @param myRoleEndpoint
     */
    void deactivateMyRoleEndpoint(Endpoint myRoleEndpoint);
    
    /**
     * Create a communication channel for communicating with a partner. A default or
     * initial value for the partner endpoint may be given. 
     * @param processId process identifier of the process requesting this channel
     * @param portType type of the port 
     * @param initialPartnerEndpoint endpoint identifer (service qname + port) of the partner
     *                               that we will communicate with on the link by default (i.e.
     *                               if the partner link is not assigned to)
     */
    PartnerRoleChannel createPartnerRoleChannel(QName processId, PortType portType,
            Endpoint initialPartnerEndpoint);
    
    /**
     * Calculate the size of the service that this endpoint references.
     * @param epr the endpoint reference for the service
     * @returns the size of the service
     */
    long calculateSizeofService(EndpointReference epr);
    
    
}
