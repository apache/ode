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

package org.apache.ode.axis2.hooks;

import org.apache.ode.axis2.ODEServer;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.axis2.AxisFault;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Overrides standard AxisServlet to handle our service configurations and
 * deployment ourselves.
 */
public class ODEAxisServlet extends AxisServlet {

  private ODEServer _odeServer;

  /**
   * Initialize the Axis configuration context
   *
   * @param config Servlet configuration
   * @throws ServletException
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    _odeServer = new ODEServer();
    _odeServer.init(config, axisConfiguration);
  }

  public void stop() throws AxisFault {
    super.stop();
    _odeServer.shutDown();
  }

}
