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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.extension.ExtensionValidator;
import org.apache.ode.bpel.extension.ExtensionBundleRuntime;
import org.apache.ode.bpel.extension.ExtensionBundleValidation;
import org.apache.ode.bpel.extension.ExtensionOperation;

import javax.xml.namespace.QName;

/**
* Abstract class that bundles and registers <code>&lt;extensionActivity&gt;</code> and
* <code>&lt;extensionAssignOperation&gt;</code> implementations related to a particular namespace.
*  
* @author Tammo van Lessen (University of Stuttgart)
*/
public abstract class AbstractExtensionBundle implements ExtensionBundleRuntime, ExtensionBundleValidation {

	private static Log __log = LogFactory.getLog(AbstractExtensionBundle.class);
	private Map<String, Class<? extends ExtensionOperation>> extensionsByName = new HashMap<String, Class<? extends ExtensionOperation>>();

	/**
	 * Returns the extension namespace this bundle provides implementations for.
	 * @return
	 */
	public abstract String getNamespaceURI();
	
	/**
	 * Register extension operations.
	 */
	public abstract void registerExtensionActivities();

	/**
	 * Register an {@link org.apache.ode.bpel.extension.ExtensionOperation} implementation as <code>&lt;extensionActivity&gt;</code>.
	 *
	 * @param localName
	 * @param activity
	 */
	protected final void registerExtensionOperation(String localName, Class<? extends ExtensionOperation> operation) {
		extensionsByName.put(localName, operation);
	}
	
	/**
	 * Returns a list of the local names of registered extension operations.
	 */
	public final Set<String> getExtensionOperationNames() {
		return Collections.unmodifiableSet(extensionsByName.keySet());
	}

	public final Class<? extends ExtensionOperation> getExtensionOperationClass(String localName) {
		return extensionsByName.get(localName);
	}

	public final ExtensionOperation getExtensionOperationInstance(String localName) throws InstantiationException, IllegalAccessException {
		return getExtensionOperationClass(localName).newInstance();
	}

	public final Map<QName, ExtensionValidator> getExtensionValidators() {
		Map<QName, ExtensionValidator> result = new HashMap<QName, ExtensionValidator>();
		String ns = getNamespaceURI();
		for (String localName : extensionsByName.keySet()) {
			if (ExtensionValidator.class.isAssignableFrom(extensionsByName.get(localName))) {
				try {
					result.put(new QName(ns, localName), (ExtensionValidator)getExtensionOperationInstance(localName));
				} catch (Exception e) {
					__log.warn("Could not instantiate extension validator for '{" + ns + "}" + localName);
				}
			}
		}
		return result;
    }
}
