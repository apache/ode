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
package org.apache.ode.bpel.obj;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.namespace.QName;

import java.io.Serializable;

/**
 * Holds information about the failure handling of this activity.
 */
public class OFailureHandling extends ExtensibleImpl{

	public static final String EXTENSION_NS_URI = "http://ode.apache.org/activityRecovery";
	public static final QName FAILURE_FAULT_NAME = new QName(EXTENSION_NS_URI,
			"activityFailure");
	public static final QName FAILURE_EXT_ELEMENT = new QName(EXTENSION_NS_URI,
			"failureHandling");

	/** Number of times to retry the activity if failure occurs.
	* Defaults to zero. */
	private static final String RETRYFOR = "retryFor";

	/** Time delay between retries of the activity, in seconds.*/
	private static final String RETRYDELAY = "retryDelay";

	/** If true, fault when failure occurs, otherwise, enter activity recovery state. */
	private static final String FAULTONFAILURE = "faultOnFailure";

	/**
	 * This can be used more than jackson deserializer
	 */
	@JsonCreator
	public OFailureHandling(){
		setRetryDelay(0);
		setRetryFor(0);
		setFaultOnFailure(false);
	}
	@JsonIgnore
	public boolean isFaultOnFailure() {
		Object o = fieldContainer.get(FAULTONFAILURE);
		return o == null ? false : (Boolean)o;
	}

	@JsonIgnore
	public int getRetryDelay() {
		Object o = fieldContainer.get(RETRYDELAY);
		return o == null ? 0 : (Integer)o;
	}

	@JsonIgnore
	public int getRetryFor() {
		Object o = fieldContainer.get(RETRYFOR);
		return o == null ? 0 : (Integer)o;
	}

	public void setFaultOnFailure(boolean faultOnFailure) {
		fieldContainer.put(FAULTONFAILURE, faultOnFailure);
	}

	public void setRetryDelay(int retryDelay) {
		fieldContainer.put(RETRYDELAY, retryDelay);
	}

	public void setRetryFor(int retryFor) {
		fieldContainer.put(RETRYFOR, retryFor);
	}

}
