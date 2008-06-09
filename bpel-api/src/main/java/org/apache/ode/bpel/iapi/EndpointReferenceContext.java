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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import java.util.Map;

/**
 * Endpoint reference context: facililates the creation of
 * {@link EndpointReference} objects.
 */
public interface EndpointReferenceContext {

    /**
     * Resolve an end-point reference from its XML representation. The
     * nature of the representation is determined by the integration
     * layer. The BPEL engine uses this method to reconstruct
     * {@link EndpointReference}  objects that have been persisted in the
     * database via {@link EndpointReference#toXML(javax.xml.transform.Result)}
     * method.
     *
     * @param XML representation of the EPR
     * @return reconsistituted {@link EndpointReference}
     */
    EndpointReference resolveEndpointReference(Element epr);


    /**
     * Converts an endpoint reference from its XML representation to another
     * type of endpoint reference.
     *
     * @param targetType
     * @param sourceEndpoint
     * @return converted EndpointReference, being of targetType
     */
    EndpointReference convertEndpoint(QName targetType, Element sourceEndpoint);

    /**
     * 
     * @param epr
     * @return
     */
    Map getConfigLookup(EndpointReference epr);
}
