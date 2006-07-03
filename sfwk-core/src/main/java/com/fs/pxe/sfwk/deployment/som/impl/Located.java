/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.impl;

import org.xml.sax.Locator;

/**
 * Encapsulates SAX-type location information for an object generated from a SAX
 * event stream.
 */
public interface Located extends Locator {
  
  /**
   * Set the properties via the supplied SAX <code>Locator</code> object.
   * @param l the SAX <code>Locator</code> object from which to draw the location.
   */
  public void setLocator(Locator l);
  
  /**
   * Set the system identifier of the SAX stream that generated this object.
   * @param s the system identifier or <code>null</code> if none was available.
   */
  public void setSystemId(String s);

  /**
   * Set the column number of the SAX event that generated this object.
   * @param c the column number of the event or <code>-1</code> if none was available.
   */
  public void setColumnNumber(int c);
    
  /**
   * Set the line number of the SAX event that generated this object.
   * @param l the line number of the event or <code>-1</code> if none was available.
   */
  public void setLineNumber(int l);
  
}
