/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime.channels;

import com.fs.jacob.SynchChannel;

/**
 *
 * @jacob.kind
 */
public interface Compensation  {

  public void forget();

  public void compensate(SynchChannel ret);

}
