/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

/**
 * Interface implemented by activities that can be used to create an process
 * instance.
 */
public interface CreateInstanceActivity extends Activity {

  /**
   * Get the value of the createInstance flag.
   *
   * @return value of createInstance flag
   */
  boolean getCreateInstance();

  /**
   * Set the value of the createInstance flag.
   *
   * @param createInstance new createInstance flag
   */
  void setCreateInstance(boolean createInstance);

}
