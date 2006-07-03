/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.soup;

import com.fs.jacob.ML;

/**
 * Persistent store representation of an object (i.e. channel read) waiting
 * for a message (i.e. channel write / method application). This class
 * maintains an opaque byte array which specifies the continuation (the exact
 * nature of this data is determined by the main JACOB VPU), as well as
 * information regarding which methods are supported by the object, and
 * whether the read is of a replicated variety.
 */
public class CommRecv extends Comm {
  private ML _continuation;

  protected CommRecv() {}

  public CommRecv(CommChannel chnl,  ML continuation) {
    super(null, chnl);
    _continuation = continuation;
  }

  /**
   * Get the continuation for this object (channel read). The continuation is
   * what happens after a message is matched to the object. It is up to the
   * JACOB VPU to determine what is placed here, but it will generally
   * consist of some serialized representation of an appropriate ML object
   * (see {@link ML}.
   *
   * @return byte array representing the serialized form of the continuation
   */
  public ML getContinuation() { return _continuation ;}

  public String toString() {
    StringBuffer buf = new StringBuffer(getChannel().toString());
    buf.append(" ? ");
    buf.append(_continuation.toString());
    return buf.toString();
  }
}
