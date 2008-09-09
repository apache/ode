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
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.iapi.ProcessConf;

/**
 * Hook into the BPEL server that enables intercepting of parntner/server invocations. This interface operates at 
 * a level that is a bit lower than the IAPI, as it allows access to internal engine datastructures. Caution should
 * be used when implementing interceptors. 
 * 
 * @author Maciej Szefler
 * 
 */
public interface MessageExchangeInterceptor {

    /**
     * Called when the BPEL server is invoked, before any attempt to route the
     * message exchange to a process.
     * 
     * @param mex
     *            message exchange
     */
    void onBpelServerInvoked(InterceptorEvent ic)
        throws FailMessageExchangeException, FaultMessageExchangeException;

    /**
     * Called when the BPEL server is invoked, after the message exchange has
     * been routed to the process.
     * 
     * @param mex
     *            message exchange
     */
    void onProcessInvoked(InterceptorEvent ic)
        throws FailMessageExchangeException, FaultMessageExchangeException;

    /**
     * Called when the BPEL server is invoked, after the message exchange has
     * been routed to the process and it has been determined that a new instance
     * needs to be created.
     * 
     * @param mex
     *            message exchange
     */
    void onNewInstanceInvoked(InterceptorEvent ic)
        throws FailMessageExchangeException, FaultMessageExchangeException;

    /**
     * Called when the BPEL server is invoked, before any attempt to route the
     * message exchange to a process.
     * 
     * @param mex
     *            message exchange
     */
    void onPartnerInvoked(InterceptorEvent ic)
        throws FailMessageExchangeException, FaultMessageExchangeException;


    /**
     * Representation of an intercept event. 
     * 
     * @author Maciej Szefler <mszefler at gmail dot com>
     *
     */
    public interface InterceptorEvent {

        /** Get the connection to the data store. */
        BpelDAOConnection getConnection();
        
        /** Get the DB representation of the process. */
        ProcessDAO getProcessDAO();

        /** Get the process configuration. */
        ProcessConf getProcessConf();
        
        /** Get the database representation of the message exchange. */
        MessageExchangeDAO getMessageExchangeDAO();
        

    }
}
