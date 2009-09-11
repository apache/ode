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
package org.apache.ode.bpel.intercept;

import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.runtime.InvalidProcessException;

/**
 * An example of a  simple interceptor providing a "throttling"  capability - that is an 
 * ability to limit the size of processes created in a server.
 */
public class ProcessSizeThrottler extends NoOpInterceptor {

    @Override
    public void onJobScheduled(MyRoleMessageExchange mex,
    		InterceptorContext ic) throws FailMessageExchangeException,
    		FaultMessageExchangeException {
    	while (ic.getBpelEngine().getHydratedProcessSize(ic.getBpelProcess().getPID()) > ic.getBpelEngine().getProcessThrottledMaximumSize()) {
    		if (!ic.getBpelEngine().dehydrateLastUnusedProcess()) {
    			mex.setProperty("causeCode", String.valueOf(InvalidProcessException.TOO_HUGE_PROCESSES_CAUSE_CODE));
	            throw new FailMessageExchangeException("The size of all running processes is more than the upper limit (" + ic.getBpelEngine().getProcessThrottledMaximumSize() + ")");
    		}
    	}
    }
}
