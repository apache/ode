package com.fs.pxe.axis.hooks;

import com.fs.pxe.axis.PXEServer;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.axis2.AxisFault;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Overrides standard AxisServlet to handle our service configurations and
 * deployment ourselves.
 */
public class PXEAxisServlet extends AxisServlet {

  private PXEServer _pxeServer;

  /**
   * Initialize the Axis configuration context
   *
   * @param config Servlet configuration
   * @throws ServletException
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    _pxeServer = new PXEServer();
    _pxeServer.init(config, axisConfiguration);
  }

  public void stop() throws AxisFault {
    super.stop();
    _pxeServer.shutDown();
  }

}
