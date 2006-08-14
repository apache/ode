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

import org.w3c.dom.Element;

public interface PartnerRoleMessageExchange extends MessageExchange {


  /**
   * Get the identifier of the process that created this message exchange.
   * @return
   */
  QName getCaller();
  
  /**
   * Get the communication channel.
   * @return communication channel
   */
  PartnerRoleChannel getChannel();
  
  
  /**
   * Indicate that the partner faulted in processing the message exchange.
   * 
   * @param faultType fault type
   * @param outputFaultMessage the input message
   *
   * @throws IllegalStateException if delivering this type of message is
   *         inappropriate at the present point.
   */
  void replyWithFault(String faultType, Message outputFaultMessage)
    throws BpelEngineException;

  /**
   * Indicate that the partner has responded to the message exchange.
   * 
   * @param response the response from the partner
   *
   * @throws IllegalStateException if delivering this type of message is
   *         inappropriate at the present point.
   */
  void reply(Message response)
    throws BpelEngineException;

  /**
   * Indicate that the partner has failed to process the message exchange. 
   * 
   * @param type type of failure
   * @param description description of failure
   */
  void replyWithFailure(FailureType type, String description, Element details) 
    throws BpelEngineException;  
  
  /**
   * Indicate that the partner processed the one-way invocation successfully.
   */
  void replyOneWayOk();
  
  /**
   * Indicate that the response to the request/response operation 
   * is not yet available and that the response will be delivered
   * asynchronously.
   */
  void replyAsync();



}
