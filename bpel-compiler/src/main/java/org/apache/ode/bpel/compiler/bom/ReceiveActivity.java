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
package org.apache.ode.bpel.compiler.bom;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Representation of a BPEL <code>&lt;receive&gt;</code> activity.
 */
public class ReceiveActivity extends CreateInstanceActivity implements Communication {
    private final CommunicationHelper _commHelper;
    
    public ReceiveActivity(Element el) {
        super(el);
        _commHelper = new CommunicationHelper(el);
    }

    /**
     * Get the optional message exchange identifier.
     * 
     * @return
     */
    public String getMessageExchangeId() {
        return getAttribute("messageExchange", null);
    }

    /**
     * Get the name of the variable that will hold the input message.
     * 
     * @return name of input message variable
     */
    public String getVariable() {
        return getAttribute("variable", null);
    }
    
    public String getRoute() {
    	return getAttribute("route", "one");
    }

    public String getOperation() {
        return _commHelper.getOperation();
    }

    public String getPartnerLink() {
        return _commHelper.getPartnerLink();
    }

    public QName getPortType() {
        return _commHelper.getPortType();
    }

    public List<Correlation> getCorrelations() {
        return _commHelper.getCorrelations();
    }
}
