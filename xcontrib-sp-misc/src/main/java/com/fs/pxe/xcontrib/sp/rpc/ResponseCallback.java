/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.xcontrib.sp.rpc;


/**
 * Callback object for synchronizing asynchronous response events.
 */
class ResponseCallback {
  private Response _response;
  private boolean _timedout;

  synchronized boolean onResponse(Response response) {
    if (_timedout) return false;
    _response = response;
    this.notify();
    return true;
  }

  synchronized Response getResponse(long timeout) {
    long etime = timeout == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;
    long ctime;
    try {
      while (_response == null && (ctime = System.currentTimeMillis()) < etime)
        this.wait(etime-ctime);
    } catch (InterruptedException ie) {
      // ignore
    }
    _timedout = _response == null;
    return _response;
  }
}
