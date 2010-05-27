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
package org.apache.ode.bpel.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.o.OBase;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OVarType;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.jacob.vpu.JacobVPU;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Base class extended by all BPEL-related abstractions. Provides methods for
 * manipulating the BPEL database, creating faults, and accessing the native
 * facilities.
 *
 * Created on Jan 12, 2004 at 5:41:27 PM.
 * @author Maciej Szefler
 */
public abstract class BpelJacobRunnable extends JacobRunnable {
    private static final Log __log = LogFactory.getLog(BpelJacobRunnable.class);

    protected BpelRuntimeContext getBpelRuntimeContext() {
        BpelRuntimeContext nativeApi = (BpelRuntimeContext) JacobVPU.activeJacobThread().getExtension(BpelRuntimeContext.class);
        assert nativeApi != null;
        return nativeApi;
    }

    protected Log log() {
        return __log;
    }

    protected final FaultData createFault(QName fault, Element faultMsg, OVarType faultType, OBase location){
        return new FaultData(fault, faultMsg, faultType, location);
    }

    protected final FaultData createFault(QName fault, OBase location, String faultExplanation) {
        return new FaultData(fault, location,faultExplanation);
    }

    protected final FaultData createFault(QName fault, OBase location){
        return createFault(fault, location, null);
    }


    protected JacobRunnable createChild(ActivityInfo childInfo, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        return new ACTIVITYGUARD(childInfo, scopeFrame, linkFrame);
    }

    protected void initializeCorrelation(CorrelationSetInstance cset, VariableInstance variable)
            throws FaultException {
        if (__log.isDebugEnabled()) {
          __log.debug("Initializing correlation set " + cset.declaration.name);
        }
        // if correlation set is already initialized,
        // then skip
        if (getBpelRuntimeContext().isCorrelationInitialized(cset)) {
          // if already set, we ignore
            if (__log.isDebugEnabled()) {
                __log.debug("OCorrelation set " + cset + " is already set: ignoring");
            }

            return;
        }

        String[] propNames = new String[cset.declaration.properties.size()];
        String[] propValues = new String[cset.declaration.properties.size()];

        for (int i = 0; i < cset.declaration.properties.size(); ++i) {
            OProcess.OProperty property = cset.declaration.properties.get(i);
            propValues[i] = getBpelRuntimeContext().readProperty(variable, property);
            propNames[i] = property.name.toString();
            if (__log.isDebugEnabled())
              __log.debug("Setting correlation property " + propNames[i] + "=" + propValues[i]);
        }

        CorrelationKey ckeyVal = new CorrelationKey(cset.declaration.name, propValues);
        getBpelRuntimeContext().writeCorrelation(cset,ckeyVal);
    }

    protected long genMonotonic() {
        return getBpelRuntimeContext().genId();
    }

}
