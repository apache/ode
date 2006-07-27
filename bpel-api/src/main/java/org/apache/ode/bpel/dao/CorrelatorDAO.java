/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.dao;

import org.apache.ode.bpel.common.CorrelationKey;

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

  /**
   * Enqueue a message exchange to the queue with a set of correlation keys.
   *
   * @param mex message exchange
   * @param correlationKeys pre-computed set of correlation keys for this message
   */
  void enqueueMessage(MessageExchangeDAO mex, CorrelationKey[] correlationKeys);


  /**
   * Dequeue a message exchange matching a correlationKey constraint.
   *
   * @param correlationKey correlation correlationKey constraint
   * @return opaque message-related data previously enqueued with the
   *         given correlation correlationKey
   */
  MessageExchangeDAO dequeueMessage(CorrelationKey correlationKey);

  /**
   * Find a route matching the given correlation key.
   * @param correlationKey correlation key
   * @return route matching the given correlation key
   */
  MessageRouteDAO findRoute(CorrelationKey correlationKey);

  /**
   * Add a route from the given correlation key to the given process instance.
   * @param routeGroupId identifier of the group of routes to which this route belongs
   * @param target target process instance
   * @param index relative order in which the route should be considered
   * @param correlationKey correlation key to match
   */
  void addRoute(String routeGroupId, ProcessInstanceDAO target, int index, CorrelationKey correlationKey);

  /**
   * Remove all routes with the given route-group identifier.
   * @param routeGroupId
   */
  void removeRoutes(String routeGroupId, ProcessInstanceDAO target);
}
