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

package org.apache.ode.axis2;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;

/**
 * User-friendly version of AxisFault
 */
public class OdeFault extends AxisFault {

    public static final QName FAULT = new QName("http://incubator.apache.org/ode", "Fault");

    public OdeFault(String message) {
        super(FAULT, message, null);
    }

    public OdeFault(Throwable cause) {
        this(cause.getMessage(), cause);
        if (cause instanceof AxisFault) {
            AxisFault f = (AxisFault) cause;
            setFaultCode(f.getFaultCode());
        }
    }

    public OdeFault(String message, Throwable cause) {
        super(new QName("java:"+cause.getClass().getPackage(), cause.getClass().getName(), "java"), message, cause);
    }

}
