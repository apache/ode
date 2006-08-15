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

import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;


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
   * Set the DAO connection factory. The DAO is used by the BPEL engine
   * to persist information about active processes.
   * @param daoCF {@link BpelDAOConnectionFactory} implementation.
   */
  void setDaoConnectionFactory(BpelDAOConnectionFactory daoCF)
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
   * Deploy a process from the filesystem.
   * @param deploymentUnitDirectory directory containing all deployment files
   * @return A collection of the process ids of the deployed processes
   */
  Collection<QName> deploy(File deploymentUnitDirectory);
  
  /**
   * Undeploy a process.
   * @param pid
   * @return successful or not
   */
  boolean undeploy(QName pid);

  /**
   * Undeploy a package.
   * @param file package file
   * @return successful or not
   */
  boolean undeploy(File file);

  /**
   * Activate a process.
   * @param pid process to activate
   * @param sticky is this change sticky, i.e. will the process be activated
   *        on restart.
   * @throws BpelEngineException
   */
  void activate(QName pid, boolean sticky) throws BpelEngineException;
  
  /**
   * Deactivate a process.
   * @param pid process to deactivate
   * @param sticky whether the change is sticky, i.e. will the process be 
   *        deactivated on restart
   * @throws BpelEngineException
   */
  void deactivate(QName pid, boolean sticky) throws BpelEngineException;
  
  /**
   * Get the BPEL management interface.
   * @return BPEL management interface
   */
  BpelManagementFacade getBpelManagementFacade();
  
  
}
