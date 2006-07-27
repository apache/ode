/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.examples.eratosthenes;

import com.fs.jacob.SynchChannel;

/**
 * DOCUMENTME.
 * <p>Created on Feb 12, 2004 at 6:22:59 PM.</p>
 *
 * @jacob.kind
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public interface NaturalNumberStream {
  public void val(int n, SynchChannel ret);
}
