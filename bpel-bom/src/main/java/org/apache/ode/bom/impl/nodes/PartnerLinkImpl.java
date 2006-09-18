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
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.PartnerLink;

import javax.xml.namespace.QName;
import java.io.Serializable;


/**
 * A BPEL object model representation of a partnerLink link.
 */
public class PartnerLinkImpl extends BpelObjectImpl implements Serializable, PartnerLink {
  private static final long serialVersionUID = -1L;

  private String _name;
  private String _myRole;
  private String _partnerRole;
  private QName _partnerLinkType;
  private boolean initializePartnerRole = false;
 

  public void setMyRole(String string) {
    _myRole = string;
  }

  public String getMyRole() {
    return _myRole;
  }

  public void setName(String string) {
    _name = string;
  }

  public String getName() {
    return _name;
  }

  public void setPartnerLinkType(QName type) {
    _partnerLinkType = type;
  }

  public QName getPartnerLinkType() {
    return _partnerLinkType;
  }

  public void setPartnerRole(String string) {
    _partnerRole = string;
  }

  public String getPartnerRole() {
    return _partnerRole;
  }

  public boolean hasMyRole() {
    return _myRole != null;
  }

  public boolean hasPartnerRole() {
    return _partnerRole != null;
  }

  public boolean isInitializePartnerRole() {
    return initializePartnerRole;
  }

  public void setInitializePartnerRole(boolean initializePartnerRole) {
    this.initializePartnerRole = initializePartnerRole;
  }

}
