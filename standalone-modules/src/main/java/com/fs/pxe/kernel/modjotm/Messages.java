/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */

package com.fs.pxe.kernel.modjotm;

import com.fs.utils.msg.MessageBundle;

public class Messages extends MessageBundle {

  public String msgErrorStartingJOTM() {
    return this.format("Error starting transaction manager.");
  }

  public String msgStartedJOTM(String jndiName) {
    return this.format("Transaction manager started and bound in JNDI as \"{0}\".", jndiName);
  }

}
