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
 * Representation of the BPEL <code>&lt;reply&gt;</code> activity.
 */
public class ReplyActivity extends Activity implements Communication {
    private final CommunicationHelper _commHelper;

    public ReplyActivity(Element el) {
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
     * Get the fault name with which to reply.
     *
     * @return the fault name
     */
    public QName getFaultName() {
        return getNamespaceContext().derefQName(getAttribute("faultName", null));
    }

    /**
     * Get the variable containing the reply message.
     *
     * @return name of variable containing the reply message
     */
    public String getVariable() {
        return getAttribute("variable", null);
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
