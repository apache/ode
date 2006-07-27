/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.utils;

import org.apache.ode.utils.msg.MessageBundle;

/**
 * Message bundle for the <code>Version</code> class.
 * 
 * @see org.apache.ode.utils.Version
 */
public class VersionMessages extends MessageBundle {

  public String msgVersionInfo(String versionName, String buildDate) {
    return this.format("FiveSight ODE version {0} (build date: {1})", versionName, buildDate);
  }

  public String msgGetCopyright() {
    return this.format("Copyright (c) 2003-2005 FiveSight Technologies, Inc.");
  }

}
