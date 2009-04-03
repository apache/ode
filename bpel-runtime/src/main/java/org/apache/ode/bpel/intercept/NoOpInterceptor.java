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
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;

/**
 * No-Op implementation of the
 * {@link org.apache.ode.bpel.intercept.MessageExchangeInterceptor interface;
 * good for sub-classing.
 * 
 * @author mszefler
 * 
 */
public class NoOpInterceptor implements MessageExchangeInterceptor {

	public void onJobScheduled(MyRoleMessageExchange mex,
			InterceptorContext ic) throws FailMessageExchangeException,
			FaultMessageExchangeException {
	}
	
	public void onBpelServerInvoked(MyRoleMessageExchange mex,
			InterceptorContext ic) throws FailMessageExchangeException,
			FaultMessageExchangeException {
	}

	public void onProcessInvoked(MyRoleMessageExchange mex,
			InterceptorContext ic) throws FailMessageExchangeException,
			FaultMessageExchangeException {
	}

	public void onPartnerInvoked(PartnerRoleMessageExchange mex,
			InterceptorContext ic) throws FailMessageExchangeException,
			FaultMessageExchangeException {
	}

	public void onNewInstanceInvoked(MyRoleMessageExchange mex,
			InterceptorContext ic) throws FailMessageExchangeException,
			FaultMessageExchangeException {

	}

}
