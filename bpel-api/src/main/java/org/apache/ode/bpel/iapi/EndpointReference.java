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

import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;

/**
 * Encapsulation of an end-point reference. Implementation of this interface
 * is provided by the integration layer. The implementing class <em>must</em>
 * provide an implementation of the
 * {@link java.lang.Object#equals(java.lang.Object)} method that returns
 * <code>true</code> if and only if the EPRs are equivalent.
 */
public interface EndpointReference {

  public static final QName SERVICE_REF_QNAME = new QName(Namespaces.WSBPEL2_0_FINAL_SERVREF, "service-ref");

  /**
   * Convert the EPR to an XML representation. The XML
   * structure is up to the integration layer. This method is
   * used by the BPEL engine to persist EPR references in the
   * database.
   * TODO: avoid using DOM
   * @return destination for the generated XML
   */
  Document toXML();
}
