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

import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor.InterceptorContext;

/**
 * Helper for invoking the appropriate {@link org.apache.ode.bpel.intercept.MessageExchangeInterceptor}
 * method -- basically a work-around for lack of closures.
 * @author mszefler
 *
 */
public abstract class InterceptorInvoker {

    private final String _name;
    // Closures anyone?

    /** Invoke {@link MessageExchangeInterceptor#onProcessInvoked(MyRoleMessageExchange, InterceptorContext)} */
    public static final InterceptorInvoker  __onProcessInvoked= new InterceptorInvoker("onProcessInvoked") {
        public void invoke(MessageExchangeInterceptor i, MessageExchange mex, InterceptorContext ictx)
            throws FailMessageExchangeException, FaultMessageExchangeException {
            i.onProcessInvoked((MyRoleMessageExchange) mex, ictx);
        }
    };

    /** Invoke {@link MessageExchangeInterceptor#onBpelServerInvoked(MyRoleMessageExchange, InterceptorContext)} */
    public static final InterceptorInvoker __onBpelServerInvoked = new InterceptorInvoker("onBpelServerInvoked") {
        public void invoke(MessageExchangeInterceptor i, MessageExchange mex, InterceptorContext ictx)
            throws FailMessageExchangeException, FaultMessageExchangeException {
            i.onBpelServerInvoked((MyRoleMessageExchange) mex, ictx);
        }
    };

    /** Invoke {@link MessageExchangeInterceptor#onBpelServerInvoked(MyRoleMessageExchange, InterceptorContext)} */
    public static final InterceptorInvoker __onJobScheduled = new InterceptorInvoker("onJobScheduled") {
        public void invoke(MessageExchangeInterceptor i, MessageExchange mex, InterceptorContext ictx)
            throws FailMessageExchangeException, FaultMessageExchangeException {
            i.onJobScheduled((MyRoleMessageExchange) mex, ictx);
        }
    };

    /** Invoke {@link MessageExchangeInterceptor#onPartnerInvoked(PartnerRoleMessageExchange, InterceptorContext)} */
    public static final InterceptorInvoker __onPartnerInvoked = new InterceptorInvoker("onPartnerInvoked") {
        public void invoke(MessageExchangeInterceptor i, MessageExchange mex, InterceptorContext ictx)
            throws FailMessageExchangeException, FaultMessageExchangeException {
            i.onPartnerInvoked((PartnerRoleMessageExchange) mex, ictx);
        }
    };

    /** Invoke {@link MessageExchangeInterceptor#onPartnerInvoked(PartnerRoleMessageExchange, InterceptorContext)} */
    public static final InterceptorInvoker __onNewInstanceInvoked = new InterceptorInvoker("onNewInstanceInvoked") {
        public void invoke(MessageExchangeInterceptor i, MessageExchange mex, InterceptorContext ictx)
            throws FailMessageExchangeException, FaultMessageExchangeException {
            i.onNewInstanceInvoked((MyRoleMessageExchange) mex, ictx);
        }
    };


    private InterceptorInvoker(String name) {
        _name = name;
    }

    public abstract void invoke(MessageExchangeInterceptor i, MessageExchange mex, InterceptorContext ictx)
        throws FailMessageExchangeException, FaultMessageExchangeException;

    public String toString() {
        return InterceptorInvoker.class.getName() + "." + _name;
    }
}
