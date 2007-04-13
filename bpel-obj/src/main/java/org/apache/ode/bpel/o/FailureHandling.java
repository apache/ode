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
package org.apache.ode.bpel.o;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * Holds information about the failure handling of this activity.
 */
public class FailureHandling implements Serializable {
    private static final long serialVersionUID = 5637366976949702880L;
    
    public static final String EXTENSION_NS_URI = "http://ode.apache.org/activityRecovery";
    public static final QName FAILURE_FAULT_NAME  = new QName(EXTENSION_NS_URI, "activityFailure");
    public static final QName FAILURE_EXT_ELEMENT = new QName(EXTENSION_NS_URI, "failureHandling");

    // Number of times to retry the activity if failure occurs.
    // Defaults to zero.
    public int retryFor;

    // Time delay between retries of the activity, in seconds.
    public int retryDelay;

    // If true, fault when failure occurs, otherwise, enter activity recovery state.
    public boolean faultOnFailure;

}
