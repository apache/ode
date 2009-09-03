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
package org.apache.ode.jbi;

import java.util.Set;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;

/**
 * Base-class for classes providing JBI-ODE translation services. 
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class ServiceBridge {
    
    private static final Log __log = LogFactory.getLog(ServiceBridge.class);

    /**
     * Transfer message properties from the ODE message exchange to the JBI message exchange object.
     * 
     * @param jbiMex destination JBI message-exchange
     * @param odeMex source ODE message-exchange
     */
    protected void copyMexProperties(javax.jbi.messaging.MessageExchange jbiMex, PartnerRoleMessageExchange odeMex) {
        __log.debug(odeMex + ": pmex copyProperties");
        NormalizedMessage in = jbiMex.getMessage("in");
        for (String propName : odeMex.getPropertyNames()) {
            String val = odeMex.getProperty(propName);
            if (val != null) {
                in.setProperty(propName, val);
                __log.debug(jbiMex + ": set pmex property " + propName + " = " + val);
            }
        }

        in.setProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID, odeMex.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID));
        in.setProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID, odeMex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID));
    }
    
    /**
     * Transfer message properties from the JBI message exchange to the ODE message exchange object.
     * 
     * @param odeMex destination ODE message-exchange
     * @param jbiMex source JBI message-exchange
     */
    @SuppressWarnings("unchecked")
    protected void copyMexProperties(MyRoleMessageExchange odeMex, javax.jbi.messaging.MessageExchange jbiMex) {
        __log.debug(odeMex + ": mmex copyProperties");
        NormalizedMessage in = jbiMex.getMessage("in");
        for (String propName : (Set<String>) in.getPropertyNames()) {
            Object val = in.getProperty(propName);
            if (propName.startsWith("org.apache.ode") ) {
                // Handle ODE-specific properties
                if (val != null) {
                    String sval = val.toString();
                    odeMex.setProperty(propName, sval);
                    __log.debug(odeMex + ": set mmex property " + propName + " = " + sval);
                }
            } else {
                // Non ODE-specific properties,
                // TODO: Should we copy these?
                __log.debug(odeMex + ": other mmex property " + propName + " = " + val);
            }
        }
    }
}
