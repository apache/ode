/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment;


/**
 * <p>
 * An exception thrown when a SAR does not appear to be in the
 * expected format.
 * </p>
 */
public class SarFormatException extends Exception {
  
  /**
   * @see Exception#Exception(java.lang.String)
   */
  public SarFormatException(String s) {
    super(s);
  }
  
  /**
   * @see Exception#Exception(java.lang.String, java.lang.Throwable)
   */
  public SarFormatException(String s, Throwable e) {
    super(s,e);
  }
}
