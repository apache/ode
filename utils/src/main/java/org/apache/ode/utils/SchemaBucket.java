/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils;

import java.net.URL;


/**
 * Utility class for obtaining XML schemas for commonly used document types.
 */
public class SchemaBucket {
  
  /**
   * Get the Basic Profile 1.0 schema for WSDL, amended to include the errata.
   * @return a <code>URL</code> to the resource
   */
  public static URL getBp1_0WsdlSchema() {
    return SchemaBucket.class.getResource("wsdl.xsd");
  }
  
}
