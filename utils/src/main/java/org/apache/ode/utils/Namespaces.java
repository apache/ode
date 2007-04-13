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

package org.apache.ode.utils;

import javax.xml.namespace.QName;

/**
 * Constant class to centralize all namespaces declarations.
 */
public class Namespaces {

    /** BPEL 2.0 Final namespaces */
    public static final String WSBPEL2_0_FINAL_ABSTRACT = "http://docs.oasis-open.org/wsbpel/2.0/process/abstract";
    public static final String WSBPEL2_0_FINAL_EXEC = "http://docs.oasis-open.org/wsbpel/2.0/process/executable";
    public static final String WSBPEL2_0_FINAL_PLINK = "http://docs.oasis-open.org/wsbpel/2.0/plnktype";
    public static final String WSBPEL2_0_FINAL_SERVREF = "http://docs.oasis-open.org/wsbpel/2.0/serviceref";
    public static final String WSBPEL2_0_FINAL_VARPROP = "http://docs.oasis-open.org/wsbpel/2.0/varprop";

    /** BPEL 2.0 draft */
    public static final String WS_BPEL_20_NS = "http://schemas.xmlsoap.org/ws/2004/03/business-process/";

    /** BPEL 1.1 */
    public static final String BPEL11_NS = "http://schemas.xmlsoap.org/ws/2003/03/business-process/";

    /** Diverse WS-* stuff */
    public static final String WS_ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
    public static final String WS_ADDRESSING_WSDL_NS = "http://www.w3.org/2006/05/addressing/wsdl";
    public static final String SOAP_NS = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String WSDL_11 = "http://schemas.xmlsoap.org/wsdl/";
    public static final String WSDL_20 = "http://www.w3.org/2006/01/wsdl";
    public static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    public static final String JBI_END_POINT_REFERENCE = "http://java.sun.com/jbi/end-point-reference";
    public static final QName WS_ADDRESSING_ENDPOINT = new QName(WS_ADDRESSING_NS, "EndpointReference");

/** ODE stuff */
    public static final String ODE_PMAPI = "http://www.apache.org/ode/pmapi/types/2006/08/02/";
    public static final String ODE_EXTENSION_NS = "http://www.apache.org/ode/type/extension";
    public static final String INTALIO_SESSION_NS = "http://www.intalio.com/type/session";

}
