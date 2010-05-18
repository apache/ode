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

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.engine.BpelProcess;
import org.apache.ode.bpel.iapi.BpelEngine;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.ProcessConf;

/**
 * Hook into the BPEL server that enables intercepting of message exchange
 * invocation.
 * 
 * @author Maciej Szefler
 * 
 */
public interface MessageExchangeInterceptor {

    /**
     * Called when the message is scheduled, before any attempt to
     * invoke the BPEL server is made.
     * 
     * @param mex
     *            message exchange
     */
    void onJobScheduled(MyRoleMessageExchange mex, InterceptorContext ic)
        throws FailMessageExchangeException, FaultMessageExchangeException;
    
    /**
     * Called when the BPEL server is invoked, before any attempt to route the
     * message exchange to a process.
     * 
     * @param mex
     *            message exchange
     */
    void onBpelServerInvoked(MyRoleMessageExchange mex, InterceptorContext ic)
        throws FailMessageExchangeException, FaultMessageExchangeException;

    /**
     * Called when the BPEL server is invoked, after the message exchange has
     * been routed to the process.
     * 
     * @param mex
     *            message exchange
     */
    void onProcessInvoked(MyRoleMessageExchange mex, InterceptorContext ic)
        throws FailMessageExchangeException, FaultMessageExchangeException;

    /**
     * Called when the BPEL server is invoked, after the message exchange has
     * been routed to the process and it has been determined that a new instance
     * needs to be created.
     * 
     * @param mex
     *            message exchange
     */
    void onNewInstanceInvoked(MyRoleMessageExchange mex, InterceptorContext ic)
        throws FailMessageExchangeException, FaultMessageExchangeException;

    /**
     * Called when the BPEL server is invoked, before any attempt to route the
     * message exchange to a process.
     * 
     * @param mex
     *            message exchange
     */
    void onPartnerInvoked(PartnerRoleMessageExchange mex, InterceptorContext ic)
        throws FailMessageExchangeException, FaultMessageExchangeException;


    public interface InterceptorContext {

        BpelDAOConnection getConnection();

        ProcessDAO getProcessDAO();

        ProcessConf getProcessConf();
        
        BpelEngine getBpelEngine();

		BpelProcess getBpelProcess();

    }
}
