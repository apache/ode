/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.soap.mapping;

public abstract class AbstractSoapBinding {

  protected static boolean parseRpcStyle(String style) {
    if (style != null && style.equals("rpc")) {
      return true;
    }
    return false;
  }

}
