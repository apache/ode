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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Representation of a BPEL <code>&lt;partnerLink&gt;</code> decleration.
 */
public class PartnerLink extends BpelObject {

  public PartnerLink(Element el) {
        super(el);
    }


/**
   * Gets the 'myRole' of the partnerLink link.
   *
   * @return myRole
   */
  public String getMyRole() {
      return getAttribute("myRole",null);
  }


  /**
   * Name of the partnerLink link.
   *
   * @return partnerLink link name
   */
  public String getName() {
      return getAttribute("name", null);
  }


  /**
   * Get the partnerLink link type.
   *
   * @return partnerLink link type
   */
  public QName getPartnerLinkType() {
      return getNamespaceContext().derefQName(getAttribute("partnerLinkType", null));
  }

  /**
   * Get the 'partnerRole'
   *
   * @return partnerLink role
   */
  public String getPartnerRole() {
      return getAttribute("partnerRole", null);
  }

  /**
   * Returns <code>true</code> if partnerLink link has a 'myRole'
   *
   * @return 'myRole' is set.
   */
  public boolean hasMyRole() {
      return getMyRole() != null;
  }

  /**
   * Determine if  'partnerRole' is set.
   *
   * @return <code>true</code> if partnerLink link has a 'partnerRole'
   */
  public boolean hasPartnerRole() {
      return getPartnerRole() != null;
  }

  /**
   * Determine if the partner role has to be initialized with default value
   * @return initialize
   */
  public boolean isInitializePartnerRole() {
      return getAttribute("initializePartnerRole","no").equals("yes");
  }


}
