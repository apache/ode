/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */

package com.fs.pxe.kernel.modjetty;

import com.fs.utils.msg.MessageBundle;

public class Messages extends MessageBundle {

  public String msgInvalidJettyConfigURL(String configURL) {
    return this.format("The Jetty configuration URL \"{0}\" is malformed.", configURL);
  }

  public String msgErrorReadingJettyConfig(String config) {
    return this.format("The Jetty configuration file at \"{0}\" could not be read.", config);
  }

  public String msgErrorStartingJettyServer(String config) {
    return this.format("Error starting Jetty server configured by \"{0}\".", config);
  }

  public String msgStartedJettyServer(String listenerString) {
    return this.format("Successfully started Jetty server; listening on ports \"{0}\".",
        listenerString);
  }

}
