/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel;

/**
 * Exception used to indicate an error with the PXE configuration.
 */
public class PxeConfigException extends Exception {

  PxeConfigException(String msg) {
    super(msg);
  }
  
  PxeConfigException(PxeKernelModException e){
   super(e); 
  }

}
