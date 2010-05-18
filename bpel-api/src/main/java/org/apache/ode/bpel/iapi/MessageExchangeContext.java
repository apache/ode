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

import org.apache.ode.bpel.iapi.MessageExchange.FailureType;
import org.w3c.dom.Element;


/**
 * <p>
 * Context provided by the integration layer exposing partner communication
 * to the BPEL engine. The BPEL engine may only invoke methods on this 
 * interface from a transactional context provided by the integration layer.
 * </p>
 * 
 * <p>
 * The following partner communication scenarios are possible:
 * <ol>
 * <li>partner is a accessible via a reliable transport (e.g. JMS, WS-RM)</li>
 * <li>partner is accessible via an unreliable transport (e.g. HTTP)</li>
 * <li>partner participates in the transaction (e.g. WS-TX)</li>
 * </ol>
 * It is important to note that each usage scenario is identical from the
 * point of view of the BPEL engine. However, the integration layer must
 * handle each of these scenarios in a different manner. See the method 
 * documentation for details.
 * </p>
 */
public interface MessageExchangeContext {

  /**
   * <p>Invoke a partner. This method is invoked by the BPEL engine when an 
   * <code>&lt;invoke&gt;</code> construct is encountered. The BPEL engine
   * will only invoke this method from a transactional context. This method 
   * MUST NOT block for extended periods (as it is called from within a 
   * transaction): to this end, actual invocation may be deferred or a 
   * synchronous operation may be decomposed into two asynchronous "legs".
   * The integration layer must provide a response to the message exchange
   * via the {@link PartnerRoleMessageExchange#reply(Message)}, 
   * {@link PartnerRoleMessageExchange#replyOneWayOk()}, 
   * {@link PartnerRoleMessageExchange#replyWithFailure(FailureType, String, Element)}
   * {@link PartnerRoleMessageExchange#replyWithFault(javax.xml.namespace.QName, Message)},
   * or {@link PartnerRoleMessageExchange#replyAsync()} methods. </p>
   * 
   * <p>Invocation of reliable, unreliable, and transactional transports should 
   * be treated differently. A brief description of how each of these scenarios
   * could be handled follows.</p>
   * 
   * <p>Reliable transports are transports such as JMS or WS-RM. For these
   * transports, the request should be enrolled in the current transaction. This
   * necessarily implies that the request is deferred until the transaction is
   * committed. It follows that for reliable request-response invocations
   * the response to the invocation will necessarily be processed in a separate 
   * transaction. </p>
   * 
   * <p>Unreliable transports are transports such as HTTP. For these transports,
   * where the operation is not idempotent it is typically required that "at 
   * most once" semantics are achieved. To this end the invocation could be 
   * noted and deferred until after the transaction is committed. </p> 
   *  
   * <p>Transactional transports are those transports that support transaction
   * propagation. For these transports, the invocation can be processed
   * immediately and the response provided to the engine via the 
   * {@link PartnerRoleMessageExchange#reply(Message)} method. </p>
   * 
   * @param mex engine-provided partner role message exchange representation,
   *        this object is valid only for the duration of the transaction 
   *        from which the {@link #invokePartner(PartnerRoleMessageExchange)}
   *        method is invoked
   * @throws ContextException if the port does not support the
   *         operation
   */
  void invokePartner(PartnerRoleMessageExchange mex)
    throws ContextException;
  
  /**
   * Method used to asynchronously deliver to the integration layer the BPEL 
   * engine's response to an invocation that could not complete synchronously. 
   * @see MyRoleMessageExchange#invoke(Message)
   */
  void onAsyncReply(MyRoleMessageExchange myRoleMex)
    throws BpelEngineException; 


}
