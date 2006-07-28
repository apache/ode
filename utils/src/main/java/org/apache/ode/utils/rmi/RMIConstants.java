/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
