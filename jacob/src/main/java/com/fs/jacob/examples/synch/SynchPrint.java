/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.examples.synch;

import com.fs.jacob.SynchChannel;

/**
 * DOCUMENTME.
 * <p>Created on Mar 4, 2004 at 4:21:03 PM.</p>
 *
 * @jacob.kind
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public interface SynchPrint {
  public SynchChannel print(String msg);
}
