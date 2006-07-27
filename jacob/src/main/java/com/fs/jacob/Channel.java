/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob;

import java.io.Serializable;

/**
 * Interface implemented by channel proxies.
 */
public interface Channel extends Serializable {
  public String export();
}
