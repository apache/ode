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
package org.apache.ode.bpel.dao;

import java.util.List;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;

import java.util.Collection;

/**
 * <p>
 * Data access object representing a <em>correlator</em>. A correlator
 * does not have a simple explanation: it acts as match-maker connecting
 * messages with <em>message consumers</em> (i.e. BPEL pick and receive
 * operations) across time.  For each partnerLink "myRole" and operation
 * there is one correlator.
 * </p>
 * <p>
 * The correlator functions as a two-sided queue: when a message is
 * received the correlator is used to dequeue a consumer based on the
 * keys from the message. If no consumer matches the keys in the message,
 * the message itself is enqueued along with its keys. Conversely, when
 * a BPEL pick/receive operation is performed, the correlator is used
 * to dequeue a message matching a given correlation key. If no message is
 * found, the consumer (i.e. the pick/receive operation) is enqueued
 * along with the target key.
 * </p>
 * <p>
 * The end result is matching of messages to pick/receive operations,
 * irrespective of whether the operation or the message arrives first.
 * Make sense?
 * </p>
 */
public interface CorrelatorDAO {

  /**
   * Get the correlator identifier.
   * @return correlator identifier
   */
  String getCorrelatorId();

    void setCorrelatorId(String newId);

  /**
   * Enqueue a message exchange to the queue with a set of correlation keys.
   *
   * @param mex message exchange
   * @param correlationKeys pre-computed set of correlation keys for this message
   */
  void enqueueMessage(MessageExchangeDAO mex, CorrelationKeySet correlationKeySet);


  /**
   * Dequeue a message exchange matching a correlationKey constraint.
   *
   * @param correlationKey correlation correlationKey constraint
   * @return opaque message-related data previously enqueued with the
   *         given correlation correlationKey
   */
  MessageExchangeDAO dequeueMessage(CorrelationKeySet correlationKeySet);

    /**
     * @return all messages waiting on this correlator, use with care as it can potentially return a lot of values
     */
    Collection<CorrelatorMessageDAO> getAllMessages();

  /**
   * Find a route matching the given correlation key.
   * @param correlationKey correlation key
   * @return route matching the given correlation key
   */
  List<MessageRouteDAO> findRoute(CorrelationKeySet correlationKeySet);

  /**
   * Check if corresponding key set is free to register (see ODE-804) 
   * @param correlationKeySet
   * @return true - available, false - not available
   */
  boolean checkRoute(CorrelationKeySet correlationKeySet);
  
  /**
   * Add a route from the given correlation key to the given process instance.
   * @param routeGroupId identifier of the group of routes to which this route belongs
   * @param target target process instance
   * @param index relative order in which the route should be considered
   * @param correlationKey correlation key to match
   */
  void addRoute(String routeGroupId, ProcessInstanceDAO target, int index, CorrelationKeySet correlationKeySet, String routePolicy);

  /**
   * Remove all routes with the given route-group identifier.
   * @param routeGroupId
   */
  void removeRoutes(String routeGroupId, ProcessInstanceDAO target);

    /**
     * @return all routes registered on this correlator, use with care as it can potentially return a lot of values
     */
    Collection<MessageRouteDAO> getAllRoutes();
}
