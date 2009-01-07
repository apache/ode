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
package org.apache.ode.bpel.o;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
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
        public List <OScope.CorrelationSet> matchCorrelations = new ArrayList<OScope.CorrelationSet>();
        private OScope.CorrelationSet matchCorrelation;

        /** Correlation set to join on. */
        public final List<OScope.CorrelationSet> joinCorrelations = new ArrayList<CorrelationSet>();
        private OScope.CorrelationSet joinCorrelation;

        public OPartnerLink partnerLink;
        public Operation operation;
        public OScope.Variable variable;

        /** OASIS addition for disambiguating receives (optional). */
        public String messageExchangeId = "";
        
        public String route = "one";

        public String getCorrelatorId() {
            return partnerLink.getId() + "." + operation.getName();
        }

        public OEvent(OProcess owner, OActivity parent) {
            super(owner, parent);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        	in.defaultReadObject();

        	// backward compatibility; matchCorrelation could have a value if read from old definition
            if (matchCorrelations == null) matchCorrelations = new ArrayList<OScope.CorrelationSet>();
        	if( matchCorrelation != null ) {
        		matchCorrelations.add(matchCorrelation);
        	}
        	// backward compatibility; joinCorrelations could be null if read from old definition
        	if( joinCorrelations == null ) {
        		try {
        			Field field = OEvent.class.getDeclaredField("joinCorrelations");
        			field.setAccessible(true);
        			field.set(this, new ArrayList<CorrelationSet>());
        		} catch( NoSuchFieldException nfe ) {
        			throw new IOException(nfe.getMessage());
        		} catch( IllegalAccessException iae ) {
        			throw new IOException(iae.getMessage());
        		}
        	}
        	// backward compatibility; joinCorrelation could have a value if read from old definition
        	if( joinCorrelation != null ) {
        		joinCorrelation.hasJoinUseCases = true;
        		joinCorrelations.add(joinCorrelation);
        	}
        }
    }
}