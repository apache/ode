/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */

package org.apache.ode.tools;

import org.apache.ode.utils.msg.MessageBundle;

public class ToolMessages extends MessageBundle {

  public String msgBadUrl(String url, String message) {
    return this.format("{0} does not appear to be a valid URL: {1}", url, message);
  }

  public String msgSoapErrorOnSend(String msg) {
    return this.format("Unable to send message due to SOAP-related error: {0}", msg);
  }

  public String msgIoErrorOnSend(String msg) {
    return this.format("Unable to send message due to I/O-related error: {0}", msg);
  }

}
