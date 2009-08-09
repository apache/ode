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
package org.apache.ode.bpel.rtrep.common.extension;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.extension.ExtensionOperation;
import org.w3c.dom.Element;

/**
 * Base class for creating new extension implementations.
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public abstract class AbstractSyncExtensionOperation implements ExtensionOperation {

	protected abstract void runSync(ExtensionContext context, Element element) throws FaultException;
	
	public void run(Object contexto, String cid, Element element)
			throws FaultException {
        ExtensionContext context = (ExtensionContext) contexto;
		try {
			runSync(context, element);
			context.complete(cid);
		} catch (FaultException f) {
			context.completeWithFault(cid, f);
		} catch (Exception e) {
			context.completeWithFault(cid, e);
		}
	}
}
