/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.impl;

import org.xml.sax.Locator;


/**
 * Base implementation (for extension) for the <code>Located</code> interface.
 */
public abstract class ALocated implements Located {

  private int _c = -1;
  private int _l = -1;
  private String _s;
  
  /**
   * @see Located#setLocator(Locator)
   */
  public void setLocator(Locator l) {
    if (l == null) return;
    _c = l.getColumnNumber();
    _l = l.getLineNumber();
    _s = l.getSystemId();
  }

  /**
   * @see Located#setSystemId(java.lang.String)
   */
  public void setSystemId(String s) {
    _s = s;
  }

  /**
   * @see Locator#getSystemId()
   */
  public String getSystemId() {
    return _s;
  }

  /**
   * @see Located#setColumnNumber(int)
   */
  public void setColumnNumber(int c) {
    _c = c;
  }

  /**
   * @see Locator#getColumnNumber()
   */
  public int getColumnNumber() {
    return _c;
  }

  /**
   * @see Located#setLineNumber(int)
   */
  public void setLineNumber(int l) {
    _l = l;
  }

  /**
   * @see Locator#getLineNumber()
   */
  public int getLineNumber() {
    return _l;
  }
  
  /**
   * @see Locator#getPublicId()
   */
  public String getPublicId() {
    return null;
  }
}
