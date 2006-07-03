/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

/**
 * Base class for all framework msgs related to a particular system.
 */
abstract class SystemTask extends DomainTask {
  
  private SystemUUID _system;
  
  public SystemTask(SystemUUID systemUUID, String domain){
  	super(domain);
    _system = systemUUID;
  }
  /**
   * Set the unique system ID for this message.
   *
   * @param uuid the system ID
   */
  public void setSystemUUID(SystemUUID uuid){
  	_system = uuid;
  }

  /**
   * Get the system ID for this message
   *
   * @return the system ID
   */
  public SystemUUID getSystemUUID(){
  	return _system;
  }
}
