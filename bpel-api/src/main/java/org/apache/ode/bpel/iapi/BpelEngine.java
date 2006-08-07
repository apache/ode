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

import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Interface exposing the BPEL "engine". Basically, this interface facilitates
 * dropping off events for processing by the engine. It is expected that
 * <em>all</em> of the methods of this interface are called only from a 
 * "transactional context" (i.e. a transaction is associated with the thread
 * invoking the method). The exact nature of the transactional context is not
 * specified, however it must be the same context that is used by the BPEL
 * DAO layer implementation. If a method invoked from this interface throws 
 * an exception of any kind, then the current transaction <em>must</em> be 
 * rolled back. 
 */
public interface BpelEngine {
  /**
   * Check whether the given endpoint reference corresponds to a "my role" 
   * partner link in some process deployed in the engine. This method is
   * provided so that the integration layer can route to multiple BPEL engines.
   * @param epr target endpoint reference 
   * @return <code>true</code> if the EPR corresponds to a my-role of an
   *         active proces, <code>false</code> otherwise.
   */
  boolean isMyRoleEndpoint(EndpointReference epr)
    throws BpelEngineException; 


  /** 
   *  Create a "my role" message exchange for invoking a BPEL process. 
   *  
   *  @param serviceId the service id of the process being called, if known
   *  @param epr endpoint reference of the port being called, if known
   *  @param operation name of the operation
   *  @return {@link MyRoleMessageExchange} the newly created message
   *          exchange 
   */
  MyRoleMessageExchange createMessageExchange(
      String clientKey,
      QName serviceId,
      EndpointReference epr, 
      String operation)
    throws BpelEngineException; 



  /**
   * Retrieve a message identified by the given identifer.
   * @param mexId message exhcange identifier
   * @return associated message exchange 
   */
  MessageExchange getMessageExchange(String mexId);

  MessageExchange getMessageExchangeByClientKey(String clientKey);
  
  
  /**
   * Call-back to the engine used by the {@link Scheduler} implementation
   * for executing scheduled jobs.  
   * @param jobId job identifier returned by the {@link Scheduler}.scheduleXXX 
   *              methods.
   * @param jobDetail job details as passed in to the 
   *              {@link Scheduler}.scheduleXXX methods  
   */
  void onScheduledJob(String jobId, Map<String, Object> jobDetail);
}
