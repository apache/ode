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
 * <p>
 * Representation of a message-driven event handler. This is used as part of a
 * <code>pick</code> activity in both the 1.1 and 2.0 dialects, and this is
 * used to represent the <code>onMessage</code> component of an
 * <code>eventHandlers</code> for a <code>scope</code> or
 * <code>process</code> in 1.1. In 2.0, the
 * {@link org.apache.ode.bpel.compiler.bom.OnEvent} is used as part of the
 * <code>eventHandlers</code> construct for a <code>scope</code> or
 * <code>process</code>.
 * </p>
 * <p>
 * Note that the semantics of the variable set with {@link #setVariable(String)}
 * are <em>different</em> depending on whether this is attached to a
 * <code>pick</code> activity or to an <code>eventHandlers</code> for a
 * <code>scope</code> or <code>process</code>. In the case of the
 * <code>pick</code>, the variable is interpreted as being declared in the
 * enclosing <code>scope</code>, but for the other construct, the variable is
 * local to the <code>onMessage</code> instance. (Recall that the
 * <code>eventHandlers</code> construct implements replication like the
 * <code>!</code> operator in the pi-calculus, so there may be multiple
 * instances of an <code>onMessage</code> handler around simultaneously.
 * </p>
 * 
 * @see org.apache.ode.bpel.compiler.bom.PickActivity
 * @see org.apache.ode.bpel.compiler.bom.OnEvent
 */
public class OnMessage extends BpelObject implements Communication {
    private CommunicationHelper _commHelper;

    public OnMessage(Element el) {
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
    
    public String getRoute() {
    	return getAttribute("route", "one");
    }

    /**
     * Get the activity associated with the event (i.e. the activity that is
     * activated).
     * 
     * @return activity activated when message event occurs
     */
    public Activity getActivity() {
        return getFirstChild(Activity.class);
    }

    /**
     * Get the input message variable for the event.
     * 
     * @return input message variable
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
