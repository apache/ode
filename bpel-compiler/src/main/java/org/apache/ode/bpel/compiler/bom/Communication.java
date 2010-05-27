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
package org.apache.ode.bpel.compiler.bom;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * Base interface for BPEL constructs representing a web-service communication.
 */
public interface Communication {

  /**
   * Get the operation for the communication.
   *
   * @return name of operation
   */
  String getOperation();


  /**
   * Get the partnerLink link on which to communicate.
   *
   * @return name of the partner link
   */
  String getPartnerLink();


  /**
   * Get the port type for the communication. This property is optional as the partner link type
   * implies a port type.
   *
   * @return name of portType for the communication (or <code>null</code>)
   */
  QName getPortType();


  List<Correlation> getCorrelations();

}
