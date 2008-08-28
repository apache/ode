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
package org.apache.ode.bpel.extension;

import org.apache.ode.bpel.common.FaultException;
import org.w3c.dom.Element;

/**
 * This is the basis interface for implementations of  
 * <code>&lt;extensionAssignOperation&gt;</code> and <code>&lt;extensionActivity&gt;</code>
 * nodes.
 * 
 * Implementations of this interface must provide a default constructor as they are created
 * using reflection.
 * 
 * @see AbstractExtensionBundle
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public interface ExtensionOperation {

	void run(Object context, Element element) throws FaultException;
	
}
