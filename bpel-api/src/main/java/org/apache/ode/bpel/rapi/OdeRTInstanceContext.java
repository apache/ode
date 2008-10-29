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
package org.apache.ode.bpel.rapi;

import org.apache.ode.bpel.evt.ProcessInstanceEvent;

/**
 *<p>A collection of interfaces that are implemented by the engine for the 
 *benefit of the runtime. These interfaces expose facilities such as variables,
 *communication, timed interrupts, process control, and recovery management. 
 *</p>
 *<p> 
 * The basic idea here is that the engine provides "language-neutral" facilities,
 * and the runtime is responsible for all the BPEL-specifics. In theory, one could
 * implement a non-BPEL runtime on top of the engine. In other words, this interface
 * is the wall that prevents BPEL, and JACOB specific things from getting into the
 * engine (some concesssion is made to BPEL when it comes to the notion of partner
 * links).
 * </p>
 */
public interface OdeRTInstanceContext extends IOContext, ProcessControlContext, RecoveryContext, VariableContext {

	Long getPid();

	/**
	 * Sends the bpel event.
	 * 
	 * @param event
	 */
	void sendEvent(ProcessInstanceEvent event);

	/**
	 * Generate a unique (and monotonic) ID in the context of this instance.
	 * 
	 * @return
	 */
	long genId();

    /**
     * @param mexId
     * @param optionalFaultData
     */
    void noreply(String mexId, FaultInfo optionalFaultData);

    void checkResourceRoute(String url, String method, String pickResponseChannel, int selectorIdx);
}
