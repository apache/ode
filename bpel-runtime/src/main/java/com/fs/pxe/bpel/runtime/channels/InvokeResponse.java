/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime.channels;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Response channel for pick requests.
 * @jacob.kind
 */
public interface InvokeResponse {

  public void onResponse();

  void onFault();

  void onFailure();
  
}
