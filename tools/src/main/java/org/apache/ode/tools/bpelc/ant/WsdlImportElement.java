/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.tools.bpelc.ant;

public class WsdlImportElement {
  
  private String _uri;
  
  public WsdlImportElement() {
    // This space intentially left blank.
  }
  
  public String getUri() {
    return _uri;
  }
  
  public void setUri(String s) {
    _uri = s;
  }
}
