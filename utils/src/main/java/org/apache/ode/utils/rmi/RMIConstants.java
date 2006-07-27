/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.utils.rmi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Constant related to naming of ODE objects in JMX.
 * @deprecated DO NO USE: Find your own way to get this URL.
 */
public final class RMIConstants {
  private static final Log __log = LogFactory.getLog(RMIConstants.class);


  /**
   * same constant defined in BootLoader but we cannot have dependencies on it
   * @deprecated DO NOT USE, will soon be eliminated.
   */
  public static final String DEFAULT_RMI_CONNECTION_URL =
    "rmi://localhost:2099/ode";

  /**
   * same constant defined in BootLoader but we cannot have dependencies on it
   */
  public static final String PROP_RMIURL = "ode.url";


  /**
   * @deprecated DO NOT USE, will soon be eliminated.
   */
  public static String getConnectionURL() {
    String url = System.getProperty(PROP_RMIURL);
    if(null == url) {
      url = DEFAULT_RMI_CONNECTION_URL;
      __log.warn("Cannot find value system property " + PROP_RMIURL + " so returning " +
        "default value for url " + DEFAULT_RMI_CONNECTION_URL);
    }
    return url;
  }
}
