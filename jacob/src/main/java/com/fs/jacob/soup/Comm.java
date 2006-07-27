/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.soup;

import org.apache.ode.utils.ObjectPrinter;

/**
 * DOCUMENTME.
 * <p>Created on Feb 16, 2004 at 8:44:27 PM.</p>
 * 
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public abstract  class Comm extends SoupObject {
  private CommChannel _channel;
  private CommGroup _group;

  protected Comm() {}

  protected Comm(CommGroup group, CommChannel chnl) {
    _group = group;
    _channel = chnl;
  }


  public CommChannel getChannel() {
    return _channel;
  }

  public void setChannel(CommChannel channel) {
    _channel = channel;
  }

  public CommGroup getGroup() {
    return _group;
  }

  public void setGroup(CommGroup group) {
    if (_group != null)
      throw new IllegalStateException("Attempted to call setGroup() twice!");
    _group = group;
  }

  public String toString() {
    return ObjectPrinter.toString(this, new Object[] { "chnl", _channel, "group", _group });
  }
}
