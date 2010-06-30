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
package org.apache.ode.bpel.iapi;

import javax.xml.namespace.QName;


/**
 * Interface implemented by the BPEL server. Provides methods for
 * life-cycle management.
 *
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 */
public interface BpelServer {

    /**
     * Configure the {@BpelEngine} with a message-exchange context. BPEL
     * engine uses this context to initiate communication with external
     * services.
     * @see MessageExchangeContext
     * @param mexContext {@link MessageExchangeContext} implementation
     */
    void setMessageExchangeContext(MessageExchangeContext mexContext)
            throws BpelEngineException;

    /**
     * Configure the {@BpelEngine} with a scheduler.
     */
    void setScheduler(Scheduler scheduler)
            throws BpelEngineException;


    /**
     * Configure the {@BpelEngine} with an endpoint-reference (EPR) context.
     * BPEL engine uses this context to EPRs.
     * @see EndpointReferenceContext
     * @param eprContext {@link EndpointReferenceContext} implementation
     */
    void setEndpointReferenceContext(EndpointReferenceContext eprContext)
            throws BpelEngineException;

    /**
     * Configure the {@BpelEngine} with a binding context. The BPEL engine uses
     * this context to register the services that it exposes and obtain communication
     * links to partner services.
     * @see BindingContext
     * @param bindingContext {@link BindingContext} implementation
     */
    void setBindingContext(BindingContext bindingContext)
            throws BpelEngineException;


    /**
     * Initialize the BPEL engine. The various contexts needed by the
     * engine must be configured before this method is called.
     */
    void init()
            throws BpelEngineException;

    /**
     * Start the BPEL engine. The BPEL engine will not execute process
     * instances until it is started.
     */
    void start()
            throws BpelEngineException;


    /**
     * Stop the BPEL engine: results in the cessation of process
     * execution.
     */
    void stop()
            throws BpelEngineException;



    /**
     * Called to shutdown the BPEL egnine.
     */
    void shutdown()
            throws BpelEngineException;


    /**
     * Get the {@link BpelEngine} interface for handling transaction operations.
     * @return transactional {@link BpelEngine} interfacce
     */
    BpelEngine getEngine();

    /**
     * Register a process with the server.
     * @param pid process to register
     * @throws BpelEngineException
     */
    void register(ProcessConf conf) throws BpelEngineException;

    /**
     * Unregister a process from the server.
     * @param pid process to unregister
     * @throws BpelEngineException
     */
    void unregister(QName pid) throws BpelEngineException;

    void cleanupProcess(ProcessConf conf) throws BpelEngineException;

    /**
     * @param pid The process definition QName
     * @return The debugger support.
     * @throws BpelEngineException if we could not find the process
     */
    DebuggerContext getDebugger(QName pid) throws BpelEngineException;

    /**
     * Sometimes it's required to acquire table locks at beginning of transaction.
     * This is for H2 database. 
     */
    void acquireTransactionLocks();
}
