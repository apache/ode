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

package org.apache.ode.bpel.epr;

import org.apache.ode.bpel.iapi.EndpointReference;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * Adds methods on {@link EndpointReference} to set and manipulate endpoint references.
 */
public interface MutableEndpoint extends EndpointReference {

  static final String ADDRESS = "address";
  static final String SESSION = "session";
  static final String SERVICE_QNAME = "service";
  static final String PORT_NAME = "port";
  static final String BINDING_QNAME = "binding";

  /**
   * Expresses the fact that the endpoint can be either tranformed to a
   * Map representation or initialized from a Map. Used for endpoint
   * conversion, to transform one endpoint type into another (using Map
   * as an intermediary format).
   */
  Map toMap();

  /**
   * Expresses the fact that the endpoint can be either tranformed to a
   * Map representation or initialized from a Map. Used for endpoint
   * conversion, to transform one endpoint type into another (using Map
   * as an intermediary format).
   */
  void fromMap(Map eprMap);

  /**
   * Checks if the type of the provided node is the right one for this
   * ServiceEndpoint implementation. The endpoint should be unwrapped
   * (without service-ref) before calling this method.
   * @param node
   * @return true if the node content matches the service endpoint implementation, false otherwise
   */
  boolean accept(Node node);

  /**
   * Set service endpoint value from an XML node.
   * @param node
   */
  void set(Node node);

  /**
   * @return endpoint target URL
   */
  String getUrl();

}
