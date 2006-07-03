/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.soup;

import com.fs.utils.ObjectPrinter;

/**
 * 
 * DOCUMENTME.
 * 
 * <p>
 * Created on Feb 16, 2004 at 9:48:47 PM.
 * </p>
 * 
 * 
 * 
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 * 
 */

public class CommChannel extends SoupObject {

  private Class _type;

  public CommChannel(Class type) {
    _type = type;
  }

  public Class getType() {
    return _type;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer(ObjectPrinter.getShortClassName(_type));
    if (getDescription() != null) {
      buf.append(':');
      buf.append(getDescription());
    }

    buf.append('#');
    buf.append(getId());
    return buf.toString();
  }

}

