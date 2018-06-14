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
package org.apache.ode.bpel.extension.bpel4restlight;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.eapi.AbstractExtensionBundle;
import org.apache.ode.utils.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension bundle for basic REST operation support in BPEL.
 * 
 * @author Michael Hahn (mhahn.dev@gmail.com)
 * 
 */
public class Bpel4RestLightExtensionBundle extends AbstractExtensionBundle {
	
	private static final Logger logger = LoggerFactory.getLogger(Bpel4RestLightExtensionBundle.class);

    public static final String NAMESPACE =
            "http://www.apache.org/ode/bpel/extensions/bpel4restlight";

    public static final QName FAULT_QNAME =
            new QName(Namespaces.ODE_EXTENSION_NS, "bpel4RestExtensions");

    @Override
    public String getNamespaceURI() {
        return NAMESPACE;
    }

    @Override
    public void registerExtensionActivities() {
        super.registerExtensionOperation("PUT", Bpel4RestLightOperation.class);
        super.registerExtensionOperation("GET", Bpel4RestLightOperation.class);
        super.registerExtensionOperation("POST", Bpel4RestLightOperation.class);
        super.registerExtensionOperation("DELETE", Bpel4RestLightOperation.class);
        
        if (logger.isDebugEnabled()) {
        	logger.debug("Bpel4RestLightExtensionBundle is registered under namespace '" + NAMESPACE + "'.");
        }
    }
}
