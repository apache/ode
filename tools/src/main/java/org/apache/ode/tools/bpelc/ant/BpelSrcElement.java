/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.tools.bpelc.ant;

public class BpelSrcElement {
  
  private String _url;
  
  public BpelSrcElement() {
    // This space intentionally left blank.
  }
  
  public void setUrl(String u) {
    _url = u;
  }
  
  public String getUrl() {
    return _url;
  }
}
