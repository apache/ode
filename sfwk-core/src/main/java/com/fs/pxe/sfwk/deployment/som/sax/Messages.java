/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.sfwk.deployment.som.sax;

import com.fs.utils.msg.MessageBundle;

public class Messages extends MessageBundle {

  /**
   * Expected root element {0} instead of {1}.
   */
  public String incorrectRootElement(String good, String bad) {
    return this.format("Expected root element {0} instead of {1}.", good, bad);
  }

  /**
   * Expected namespace {0} instead of {1}.
   */
  public String incorrectNamespace(String good, String bad) {
    return this.format("Expected namespace {0} instead of {1}.", good, bad);
  }

}
