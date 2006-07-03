/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa;

import javax.xml.namespace.QName;

public interface GraphProvider {
  
  public StateFactory getStateFactory(String name) ;
    
  public String getQNameEdge(String src, QName child);
  
  public String getOtherEdge(String src, String uri);
}
