/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.utils.msg;

public class MessageBundleTestMessages extends MessageBundle {

  public String msgNoParameter() {
    return this.format("No parameter!");
  }

  public String msgWrongParameter(String param) {
    return this.format("Wrong parameter {0}", param);
  }

}
