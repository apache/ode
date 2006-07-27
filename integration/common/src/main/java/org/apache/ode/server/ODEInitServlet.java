package org.apache.ode.server;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.MBeanServer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.lang.StringUtils;

import org.apache.ode.kernel.OdeKernel;

public class ODEInitServlet extends HttpServlet {
  private static class OdeKernelRuntimeContext implements OdeKernel.RuntimeContext, Runnable {
    private MBeanServer mbeanServer;
    private OdeKernel kernel;
    private URL kernelConfig;
    
    OdeKernelRuntimeContext(MBeanServer mbeanServer, URL kernelConfig) {
      this.kernelConfig = kernelConfig;
      //mbeanServer = MBeanServerFactory.createMBeanServer();
      this.mbeanServer = mbeanServer;
      try {
        //mbeanServer.registerMBean(Main.class.getClassLoader(),
        //  new ObjectName(mbeanServer.getDefaultDomain() + ":name=BootLoader"));
        kernel = new OdeKernel(this);
      } catch(Exception e) {
        e.printStackTrace();;
      }
    }
    
    private OdeKernel getKernel() {
      return kernel;
    }
    
    public void run() {
      stop();
    }
    
    public void stop() {
      if(getKernel()!=null) {
        try {
          getKernel().stop();
          mbeanServer.unregisterMBean(getKernel().getObjectName()); // TODO: necessary?
        } catch(Exception e) {
          e.printStackTrace();
        }
      }      
    }
    public boolean start() {
      if(null == getKernel())
        return false;
      try {
        mbeanServer.registerMBean(getKernel(), getKernel().getObjectName());
        getKernel().start();
      } catch(Exception e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }
    
    /**
     * exits the VM realizing the shutdown hooks
     */
    public boolean handleShutdown() {
      Thread exitDispatch = new Thread(new Runnable() {
        public void run() {
          try {
            final int waitTime = 200;
            Thread.sleep(waitTime);
          } catch(Exception e) {
            e.printStackTrace();
          }
        }
      });
      exitDispatch.setPriority(Thread.MIN_PRIORITY);
      exitDispatch.start();
      return true;
    }
    
    /**
     * @see org.apache.ode.kernel.OdeKernel.RuntimeContext#getConfigUrl()
     */
    public URL getConfigUrl() {
      return kernelConfig;
    }
  }
  
  private OdeKernelRuntimeContext _kernelRuntimeContext;
  /**
   * @see javax.servlet.GenericServlet#init()
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init();
    _kernelRuntimeContext =
      new OdeKernelRuntimeContext(
        ManagementFactory.getPlatformMBeanServer(),
        checkODEConfig(config));
    _kernelRuntimeContext.start();
  }
  
  private URL checkODEConfig(ServletConfig config) throws ServletException {
    String odeHome = System.getProperty("ode.home");
    if(StringUtils.isEmpty(odeHome)) {
      throw new ServletException("Set up ode.home system property.");
      
    }
      
    try {
      return new File(odeHome, "etc/ode-config.xml").toURI().toURL();
    }catch(MalformedURLException ex) {
      //Nothing here
      throw new ServletException(ex.getMessage(), ex);
    }
  }
  /**
   * @see javax.servlet.GenericServlet#destroy()
   */
  @Override
  public void destroy() {
    _kernelRuntimeContext.stop();
    super.destroy();
  }
}
