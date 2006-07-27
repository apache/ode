/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.rr;

/**
 * Resource repository exception. 
 */
public class ResourceRepositoryException extends Exception {
 
  public ResourceRepositoryException(String msg) {
    super(msg);
  }
  
  public ResourceRepositoryException(Throwable cause) {
    super(cause);
  }
 
  public ResourceRepositoryException(String msg, Throwable cause) {
    super(msg, cause);
  }
  
}
