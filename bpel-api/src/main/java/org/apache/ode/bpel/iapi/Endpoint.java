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

/**
 * Concrete representation of service endpoint. This consists of a service
 * qualified name and port name per WSDL specification.
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 * 
 */
public class Endpoint {
    /** Service QName */
    public final QName serviceName;

    /** Port Name */
    public final String portName;

    /** Constructor. */
    public Endpoint(QName serviceName, String portName) {
        if (serviceName == null || portName == null)
            throw new NullPointerException("serviceName and portName must not be null");
        this.serviceName = serviceName;
        this.portName = portName;
    }

    /**
     * Equality operator, check whether service name and port name are both equal.
     */
    @Override 
    public boolean equals(Object other) {
        if (!(other instanceof Endpoint)) {
            return false;
        }
        Endpoint o = (Endpoint) other;
        return o.serviceName.equals(serviceName) && o.portName.equals(portName);
    }

    @Override
    public int hashCode() {
        return serviceName.hashCode() ^ portName.hashCode();
    }
    
    /**
     * Print object in the form <em>serviceQName</em>:<em>port</em>
     */
    @Override
    public String toString() {
        return serviceName + ":" + portName;
    }

}
