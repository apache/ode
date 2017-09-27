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

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.extension.ExtensionOperation;
import org.apache.ode.bpel.o.OExtensionActivity;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.runtime.common.extension.AbstractSyncExtensionOperation;
import org.apache.ode.bpel.runtime.common.extension.ExtensibilityQNames;
import org.apache.ode.bpel.runtime.common.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JacobRunnable that delegates the work of the <code>extensionActivity</code>
 * activity to a registered extension implementation.
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class EXTENSIONACTIVITY extends ACTIVITY {
	private static final long serialVersionUID = 1L;
	private static final Logger __log = LoggerFactory
			.getLogger(EXTENSIONACTIVITY.class);

	public EXTENSIONACTIVITY(ActivityInfo self, ScopeFrame scopeFrame,
			LinkFrame linkFrame) {
		super(self, scopeFrame, linkFrame);
	}

	public final void run() {
		final ExtensionContext context = new ExtensionContextImpl(this,
				getBpelRuntimeContext());
		final OExtensionActivity oea = (OExtensionActivity) _self.o;

		try {
			ExtensionOperation ea = getBpelRuntimeContext()
					.createExtensionActivityImplementation(oea.extensionName);
			if (ea == null) {
				for (OProcess.OExtension oe : oea.getOwner().mustUnderstandExtensions) {
					if (oea.extensionName.getNamespaceURI().equals(
							oe.namespaceURI)) {
						__log.warn("Lookup of extension activity "
								+ oea.extensionName + " failed.");
						throw new FaultException(
								ExtensibilityQNames.UNKNOWN_EA_FAULT_NAME,
								"Lookup of extension activity "
										+ oea.extensionName
										+ " failed. No implementation found.");
					}
				}
				// act like <empty> - do nothing
				context.complete();
				return;
			}

			ea.run(context, oea.nestedElement.getElement());

			// Complete the context for sync extension operations. Asynchronous
			// operations have to control their completion themselves.
			if (ea instanceof AbstractSyncExtensionOperation) {
				context.complete();
			}
		} catch (FaultException fault) {
			__log.error("Execution of extension activity caused an exception.",
					fault);
			context.completeWithFault(fault);
		}

	}

}