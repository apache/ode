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
package org.apache.ode.bom.api;

/**
 * Constants.
 */
public final class Constants {
  
  public static final boolean isBpelNamespace(String uri){
  	return uri.equals(NS_BPEL4WS_2003_03) || uri.equals(NS_WSBPEL_2004_03);
  }
  
  /**
   * BPEL Namespace, 03/2003
   */
  public static final String NS_BPEL4WS_2003_03 = "http://schemas.xmlsoap.org/ws/2003/03/business-process/";
  
  /**
   * Oasis Bpel Namesapce, 03/2004
   */
  public static final String NS_WSBPEL_2004_03 = "http://schemas.xmlsoap.org/ws/2004/03/business-process/";

  /**
   * BPEL Partnerlink Namespace, 05/2003
   */
  public static final String NS_BPEL4WS_PARTNERLINK_2003_05 = "http://schemas.xmlsoap.org/ws/2003/05/partner-link/";
  
  public static final String NS_WSBPEL_PARTNERLINK_2004_03 = "http://schemas.xmlsoap.org/ws/2004/03/partner-link/";

  public static final String NS_XML_SCHEMA_2001 = "http://www.w3.org/2001/XMLSchema";

  private Constants() {
  }

}
