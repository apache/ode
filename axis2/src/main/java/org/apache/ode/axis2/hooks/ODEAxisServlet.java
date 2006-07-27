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
