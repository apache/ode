/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
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
