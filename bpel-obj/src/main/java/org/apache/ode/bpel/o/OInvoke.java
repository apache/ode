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

import org.apache.ode.bpel.o.OScope.CorrelationSet;

/**
 * Compiled rerpresentation of the BPEL <code>&lt;invoke&gt;</code> activity.
 */
public class OInvoke extends OActivity {

    static final long serialVersionUID = -1L  ;
    public OPartnerLink partnerLink;
    public OScope.Variable inputVar;
    public OScope.Variable outputVar;
    public Operation operation;

    /** Correlation sets initialized on the input message. */
    public final List<OScope.CorrelationSet> initCorrelationsInput = new ArrayList<OScope.CorrelationSet>();

    /** Correlation sets initialized on the output message. */
    public final List <OScope.CorrelationSet> initCorrelationsOutput = new ArrayList<OScope.CorrelationSet>();

    /** Correlation sets asserted on input. */
    public final List <OScope.CorrelationSet> assertCorrelationsInput = new ArrayList<OScope.CorrelationSet>();

    /** Correlation sets asserted on output. */
    public final List<OScope.CorrelationSet> assertCorrelationsOutput = new ArrayList<OScope.CorrelationSet>();

    /** Correlation sets joined on input. */
    public final List <OScope.CorrelationSet> joinCorrelationsInput = new ArrayList<OScope.CorrelationSet>();

    /** Correlation sets joined on output. */
    public final List<OScope.CorrelationSet> joinCorrelationsOutput = new ArrayList<OScope.CorrelationSet>();

    public OInvoke(OProcess owner, OActivity parent) {
        super(owner, parent);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // backward compatibility; joinCorrelationInput could be null if read from old definition
        if( joinCorrelationsInput == null ) {
            try {
                Field field = OInvoke.class.getDeclaredField("joinCorrelationsInput");
                field.setAccessible(true);
                field.set(this, new ArrayList<OScope.CorrelationSet>());
            } catch( NoSuchFieldException nfe ) {
                throw new IOException(nfe.getMessage());
            } catch( IllegalAccessException iae ) {
                throw new IOException(iae.getMessage());
            }
        }
        // backward compatibility; joinCorrelationOutput could be null if read from old definition
        if( joinCorrelationsOutput == null ) {
            try {
                Field field = OInvoke.class.getDeclaredField("joinCorrelationsOutput");
                field.setAccessible(true);
                field.set(this, new ArrayList<CorrelationSet>());
            } catch( NoSuchFieldException nfe ) {
                throw new IOException(nfe.getMessage());
            } catch( IllegalAccessException iae ) {
                throw new IOException(iae.getMessage());
            }
        }
    }
}
