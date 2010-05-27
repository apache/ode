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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Data access object representing the endpoint reference of a specific
 * partner link role (typically the partnerRole). An EPR has an implicit
 * value attributed by the engine (usually by using the WSDL service
 * definition but anyway that's the communication layer business). An
 * EndpointReferenceDAO only has its own value if the default has been
 * overriden (by assignment).
 */
public interface PartnerLinkDAO {

  /**
   * Get the model id of the partner link.
   * @return
   */
  public int getPartnerLinkModelId();

  public String getMyRoleName();

  public String getPartnerRoleName();

  public String getPartnerLinkName();

  /**
   * Get the service name associated with this partner link.
   * @return
   */
  public QName getMyRoleServiceName();

  public void setMyRoleServiceName(QName svcName);

  public Element getMyEPR();

  public void setMyEPR(Element val);

  public Element getPartnerEPR();

  public void setPartnerEPR(Element val);

public String getMySessionId();

public String getPartnerSessionId();

public void setPartnerSessionId(String session);

public void setMySessionId(String sessionId);

}
