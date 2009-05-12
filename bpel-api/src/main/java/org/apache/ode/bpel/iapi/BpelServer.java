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

import java.util.Set;

import javax.xml.namespace.QName;


/**
 * Interface implemented by the BPEL server. Provides methods for life-cycle management and process invocation. 
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

    
    /**
     * Inquire of the engine the invocation styles that are supported for a given service. 
     * @param serviceId service identifier 
     * @return set of supported {@link InvocationStyle}s
     */
    Set<InvocationStyle> getSupportedInvocationStyle(QName serviceId);
    
    /**
     * Create a "my role" message exchange for invoking a BPEL process.
     * 
     * @param serviceId
     *            the service id of the process being called, if known
     * @param operation
     *            name of the operation
     * 
     * @return {@link MyRoleMessageExchange} the newly created message exchange
     */
    MyRoleMessageExchange createMessageExchange(InvocationStyle istyle, QName serviceId, String operation,
                                                String foreignKey) throws BpelEngineException;

    RESTInMessageExchange createMessageExchange(Resource resource, String foreignKey) throws BpelEngineException;

    /**
     * Retrieve a message identified by the given identifer.
     * 
     * @param mexId
     *            message exhcange identifier
     * @return associated message exchange
     */
    MessageExchange getMessageExchange(String mexId) 
        throws BpelEngineException;

    MessageExchange getMessageExchangeByForeignKey(String foreignKey) 
        throws BpelEngineException;
}
