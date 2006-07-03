/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.utils.uuid.UUID;

/**
 * Globally unique identifier of a system.
 */
class SystemUUID extends UUID {
  /**
   * @see com.fs.utils.uuid.UUID#UUID()
   */
  public SystemUUID() {
    super();
  }

  /**
   * @see UUID#UUID(String)
   */
  public SystemUUID(String id) {
    super(id);
  }

  /**
   * Re-constructs a system uuid from its string representation
   *
   * @param str uuid in string form
   *
   * @return Re-constructed uuid
   */
  public static SystemUUID fromIdString(String str) {
    return new SystemUUID(str);
  }

  protected String prefix() {
    return "SYS";
  }
}
