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
package org.apache.ode.bpel.rtrep.v2;

import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Operation;


/**
 * Compiled represenation of a BPEL event handler.
 */
public class OEventHandler extends OAgent {
    static final long serialVersionUID = -1L  ;
    public List<OEvent> onMessages = new ArrayList<OEvent>();
    public List<OAlarm> onAlarms = new ArrayList<OAlarm>();

    public OEventHandler(OProcess owner) {
        super(owner);
    }

    public static class OAlarm extends OAgent {
        static final long serialVersionUID = -1L  ;

        public OExpression forExpr;
        public OExpression untilExpr;
        public OExpression repeatExpr;
        public OActivity activity;

        public OAlarm(OProcess owner){
            super(owner);
        }
    }
    
    public static class OEvent extends OScope {
        static final long serialVersionUID = -1L  ;
        
        /** Correlations to initialize. */
        public final List <OScope.CorrelationSet> initCorrelations = new ArrayList<OScope.CorrelationSet>();

        /** Correlation set to match on. */
        public OScope.CorrelationSet matchCorrelation;

        public OPartnerLink partnerLink;
        public Operation operation;
        public OScope.Variable variable;

        /** OASIS addition for disambiguating receives (optional). */
        public String messageExchangeId = "";


        public String getCorrelatorId() {
            return partnerLink.getId() + "." + operation.getName();
        }

        public OEvent(OProcess owner, OActivity parent) {
            super(owner, parent);
        }
    }
}
