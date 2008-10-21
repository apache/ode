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
package org.apache.ode.extension.e4x;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.rtrep.common.extension.AbstractSyncExtensionOperation;
import org.apache.ode.bpel.rtrep.common.extension.ExtensionContext;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.xml.XMLLib;
import org.mozilla.javascript.xml.XMLLib.Factory;
import org.w3c.dom.Element;

/**
 * Implementation of a Javascript extension assign operation.
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class JSExtensionOperation extends AbstractSyncExtensionOperation {
	private static final Log __logger = LogFactory.getLog(JSExtensionOperation.class);
	
	public void runSync(ExtensionContext context, Element element) throws FaultException {
		CustomContextFactory.init();
        Context ctx = ContextFactory.getGlobal().enterContext();
        ctx.setOptimizationLevel(-1);
        ctx.setGeneratingDebug(false);
        ctx.setGeneratingSource(false);
        ctx.setDebugger(null, null);
        
		try {
			Scriptable scope = new TopLevelFunctions(context, ctx, context.getDUDir());
			String source = element.getTextContent();
			VariableDelegator delegator = new VariableDelegator(scope, context, ctx);
			ctx.evaluateString(delegator, source, context.getActivityName(), 1, null);
			delegator.writeVariables();
		} catch (WrappedException e) {
			__logger.warn("Error during JS execution.", e);
			if (e.getWrappedException() instanceof FaultException) {
				throw (FaultException)e.getWrappedException();
			}
			throw new FaultException(new QName("ExtensionEvaluationFault", JSExtensionBundle.NS), e.getMessage());
		} catch (FaultException e) {
			__logger.warn("Fault during JS execution.", e);
			throw e;
		} catch (Exception e) {
			__logger.warn("Error during JS execution.", e);
			throw new FaultException(new QName("ExtensionEvaluationFault", JSExtensionBundle.NS), e.getMessage());
		} finally {
			Context.exit();
		}
	}
	
	private static class CustomContextFactory extends ContextFactory {
		//Enforce usage of plain DOM
		protected Factory getE4xImplementationFactory() {
			return XMLLib.Factory.create("org.mozilla.javascript.xmlimpl.XMLLibImpl");
		}
		
		static void init() {
			if (!ContextFactory.hasExplicitGlobal()) {
				ContextFactory.initGlobal(new CustomContextFactory());
			}
		}
	}
}
