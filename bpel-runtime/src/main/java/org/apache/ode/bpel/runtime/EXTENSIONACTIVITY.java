/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ode.bpel.runtime;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.compiler.bom.ExtensibilityQNames;
import org.apache.ode.bpel.eapi.ExtensionContext;
import org.apache.ode.bpel.obj.OExtensionActivity;
import org.apache.ode.bpel.runtime.channels.ExtensionResponse;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.jacob.ReceiveProcess;
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
        try {
            final ExtensionResponse responseChannel = newChannel(ExtensionResponse.class);
            final ExtensionContext helper =
                    new ExtensionContextImpl(_scopeFrame, getBpelRuntimeContext());

            getBpelRuntimeContext().executeExtension(_oext.getExtensionName(), helper,
                    DOMUtils.stringToDOM(_oext.getNestedElement()), responseChannel);

            object(new ReceiveProcess() {
                private static final long serialVersionUID = 3643564901004147956L;
            }.setChannel(responseChannel).setReceiver(new ExtensionResponse() {
                private static final long serialVersionUID = -6977609968638662977L;

                public void onCompleted() {
                    _self.parent.completed(null, CompensationHandler.emptySet());
                }

                public void onFailure(Throwable t) {
                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));
                    FaultData fault = createFault(
                            _oext.getOwner().getConstants().getQnSubLanguageExecutionFault(), _oext,
                            sw.getBuffer().toString());
                    _self.parent.completed(fault, CompensationHandler.emptySet());
                };
            }));

        } catch (FaultException fault) {
            __log.error("Exception while invoking extension activity '" + _oext.getName() + "'.",
                    fault);
            FaultData faultData = createFault(fault.getQName(), _oext, fault.getMessage());
            _self.parent.completed(faultData, CompensationHandler.emptySet());
        } catch (SAXException e) {
            __log.error("Exception while invoking extension activity '" + _oext.getName() + "'.",
                    e);
            FaultData faultData =
                    createFault(ExtensibilityQNames.INVALID_EXTENSION_ELEMENT, _self.o,
                            "The nested element of extension activity '" + _oext.getName()
                                    + "' for extension '" + _oext.getExtensionName()
                                    + "' is no valid XML.");
            _self.parent.completed(faultData, CompensationHandler.emptySet());
        } catch (IOException e) {
            __log.error("Exception while invoking extension activity '" + _oext.getName() + "'.",
                    e);
            FaultData faultData =
                    createFault(ExtensibilityQNames.INVALID_EXTENSION_ELEMENT, _self.o,
                            "The nested element of extension activity '" + _oext.getName()
                                    + "' for extension '" + _oext.getExtensionName()
                                    + "' is no valid XML.");
            _self.parent.completed(faultData, CompensationHandler.emptySet());
        }

    }

}
