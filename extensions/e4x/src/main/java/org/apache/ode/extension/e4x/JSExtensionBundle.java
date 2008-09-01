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

import org.apache.ode.bpel.rtrep.common.extension.AbstractExtensionBundle;

/**
 * Implementation of a Javascript extension bundle.
 * 
 * It currently provides only one extension operation to be used either as 
 * extensionActivity or as extensionAssignOperation.
 * 
 * @author Tammo van Lessen
 */
public class JSExtensionBundle extends AbstractExtensionBundle {
	public static final String NS = "http://ode.apache.org/extensions/e4x";
	public String getNamespaceURI() {
		return NS;
	}

	public void registerExtensionActivities() {
		registerExtensionOperation("snippet", JSExtensionOperation.class);
	}

}
