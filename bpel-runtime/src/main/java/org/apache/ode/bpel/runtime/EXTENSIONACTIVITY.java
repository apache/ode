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

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.compiler.bom.ExtensibilityQNames;
import org.apache.ode.bpel.eapi.ExtensionContext;
import org.apache.ode.bpel.eapi.ExtensionOperation;
import org.apache.ode.bpel.o.OExtensionActivity;
import org.apache.ode.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * JacobRunnable that delegates the work of the <code>extensionActivity</code> activity to a
 * registered extension implementation.
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class EXTENSIONACTIVITY extends ACTIVITY {
    private static final long serialVersionUID = 1L;
    private static final Logger __log = LoggerFactory.getLogger(EXTENSIONACTIVITY.class);

    private OExtensionActivity _oext;

    public EXTENSIONACTIVITY(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
        _oext = (OExtensionActivity) _self.o;
    }

    public final void run() {
        final ExtensionContext context =
                new ExtensionContextImpl(_self, _scopeFrame, getBpelRuntimeContext());
        final QName extensionId = _oext.extensionName;
        try {
            ExtensionOperation ea =
                    getBpelRuntimeContext().createExtensionActivityImplementation(extensionId);
            if (ea == null) {
                if (_oext.getOwner().hasMustUnderstandExtension(extensionId.getNamespaceURI())) {
                    __log.warn("Lookup of extension activity " + extensionId + " failed.");
                    throw new FaultException(ExtensibilityQNames.UNKNOWN_EA_FAULT_NAME,
                            "Lookup of extension activity " + extensionId
                                    + " failed. No implementation found.");
                } else {
                    // act like <empty> - do nothing
                    context.complete();
                    return;
                }
            }

            ea.run(context, DOMUtils.stringToDOM(_oext.nestedElement));
        } catch (FaultException fault) {
            __log.error("Exception while invoking extension activity '" + _oext.name + "'.",
                    fault);
            context.completeWithFault(fault);
        } catch (SAXException e) {
            __log.error("Exception while invoking extension activity '" + _oext.name + "'.",
                    e);
            FaultException faultData =
                    new FaultException(ExtensibilityQNames.INVALID_EXTENSION_ELEMENT,
                            "The nested element of extension activity '" + _oext.name
                                    + "' for extension '" + _oext.extensionName
                                    + "' is no valid XML.", e);
            context.completeWithFault(faultData);
        } catch (IOException e) {
            __log.error("Exception while invoking extension activity '" + _oext.name + "'.",
                    e);
            FaultException faultData =
                    new FaultException(ExtensibilityQNames.INVALID_EXTENSION_ELEMENT,
                            "The nested element of extension activity '" + _oext.name
                                    + "' for extension '" + _oext.extensionName
                                    + "' is no valid XML.", e);
            context.completeWithFault(faultData);
        }

    }

}