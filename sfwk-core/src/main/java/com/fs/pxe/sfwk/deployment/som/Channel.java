/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som;

import java.io.Serializable;


/**
 * Parse-time and load-time description of a channel.
 */
public interface Channel extends Serializable, Marshallable {
  
  /**
   * Set the name of the channel.
   * @param s the name
   */
  public void setName(String s);
  
  /**
   * @return the name of this channel.
   */
  public String getName();
    
}
