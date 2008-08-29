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

    /** Correlation sets initialized on the input message. */
    public final List <OScope.CorrelationSet> initCorrelationsOutput = new ArrayList<OScope.CorrelationSet>();

    /** Correlation sets asserted on input. */
    public final List <OScope.CorrelationSet> assertCorrelationsInput = new ArrayList<OScope.CorrelationSet>();

    /** Correlation sets asserted on output. */
    public final List<OScope.CorrelationSet> assertCorrelationsOutput = new ArrayList<OScope.CorrelationSet>();

    public OInvoke(OProcess owner, OActivity parent) {
        super(owner, parent);
    }
}
