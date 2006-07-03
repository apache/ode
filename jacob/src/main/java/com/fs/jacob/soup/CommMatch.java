/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.soup;

/**
 * 
 * DOCUMENTME.
 * 
 * <p>
 * Created on Feb 18, 2004 at 2:02:18 PM.
 * </p>
 * 
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 * 
 */

public class CommMatch {
  private CommSend _send;
  private CommRecv _recv;

  public CommMatch(CommRecv recv, CommSend send) {
    _send = send;
    _recv = recv;
  }

  public CommSend getSendSide() {
    return _send;
  }

  public CommRecv getRecvSide() {
    return _recv;
  }

}
