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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.compiler.bom.Bpel20QNames;
import org.apache.ode.bpel.o.OExtensionActivity;
import org.apache.ode.bpel.runtime.channels.ExtensionResponseChannel;
import org.apache.ode.bpel.runtime.channels.ExtensionResponseChannelListener;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.SerializableElement;

/**
 * JacobRunnable that delegates the work of the <code>extensionActivity</code> activity
 * to a registered extension implementation.
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class EXTENSIONACTIVITY extends ACTIVITY {
	private static final long serialVersionUID = 1L;
	private static final Log __log = LogFactory.getLog(EXTENSIONACTIVITY.class);

    private OExtensionActivity _oext;
    
	public EXTENSIONACTIVITY(ActivityInfo self, ScopeFrame scopeFrame,
			LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
        _oext = (OExtensionActivity) _self.o;
	}

    public final void run() {
        try {
        	final ExtensionResponseChannel responseChannel = newChannel(ExtensionResponseChannel.class);
        	final ExtensionContext helper = new ExtensionContextImpl(_self.o, _scopeFrame, getBpelRuntimeContext());
        	
        	getBpelRuntimeContext().executeExtension(DOMUtils.getElementQName(_oext.nestedElement.getElement()), helper, _oext.nestedElement.getElement(), responseChannel);
        	
            object(new ExtensionResponseChannelListener(responseChannel) {
				private static final long serialVersionUID = -1L;

				public void onCompleted() {
					_self.parent.completed(null, CompensationHandler.emptySet());
            	}
            	
            	public void onFailure(Throwable t) {
            		StringWriter sw = new StringWriter();
            		t.printStackTrace(new PrintWriter(sw));
            		FaultData fault = createFault(new QName(Bpel20QNames.NS_WSBPEL2_0, "subLanguageExecutionFault"), _oext, sw.getBuffer().toString());
                    _self.parent.completed(fault, CompensationHandler.emptySet());
            	};
            });

		} catch (FaultException fault) {
            __log.error(fault);
            FaultData faultData = createFault(fault.getQName(), _oext, fault.getMessage());
            _self.parent.completed(faultData, CompensationHandler.emptySet());
		}

    }

}
