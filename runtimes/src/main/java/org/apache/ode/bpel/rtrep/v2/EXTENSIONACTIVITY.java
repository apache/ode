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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.extension.ExtensionOperation;
import org.apache.ode.bpel.rtrep.common.extension.ExtensibilityQNames;
import org.apache.ode.bpel.rtrep.common.extension.ExtensionContext;

/**
 * JacobRunnable that delegates the work of the <code>extensionActivity</code> activity
 * to a registered extension implementation.
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class EXTENSIONACTIVITY extends ACTIVITY {
    private static final long serialVersionUID = 1L;
    private static final Log __log = LogFactory.getLog(EXTENSIONACTIVITY.class);

    public EXTENSIONACTIVITY(ActivityInfo self, ScopeFrame scopeFrame,
            LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
    }

    public final void run() {
        final ExtensionContext context = new ExtensionContextImpl(_self, _scopeFrame, getBpelRuntime());
        final OExtensionActivity oea = (OExtensionActivity)_self.o;
        
        try {
            ExtensionOperation ea = getBpelRuntime().createExtensionActivityImplementation(oea.extensionName);
            if (ea == null) {
                for (OProcess.OExtension oe : oea.getOwner().mustUnderstandExtensions) {
                    if (oea.extensionName.getNamespaceURI().equals(oe.namespaceURI)) {
                        __log.warn("Lookup of extension activity " + oea.extensionName + " failed.");
                        throw new FaultException(ExtensibilityQNames.UNKNOWN_EA_FAULT_NAME, "Lookup of extension activity " 
                                + oea.extensionName + " failed. No implementation found.");
                    }
                }
                // act like <empty> - do nothing
                context.complete(_self.parent.export());
                return;
            }

            ea.run(context, _self.parent.export(), oea.nestedElement.getElement());
        } catch (FaultException fault) {
            __log.error(fault);
            context.completeWithFault(_self.parent.export(), fault);
        }

    }

}
