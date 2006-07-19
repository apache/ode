/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime.channels;

/**
 * Response channel for pick requests.
 * @jacob.kind
 */
public interface InvokeResponse {

  public void onResponse();

  void onFault();

  void onFailure();
  
}
