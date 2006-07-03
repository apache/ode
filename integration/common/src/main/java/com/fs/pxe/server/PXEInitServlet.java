package com.fs.pxe.server;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.MBeanServer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.lang.StringUtils;

import com.fs.pxe.kernel.PxeKernel;

public class PXEInitServlet extends HttpServlet {
  private static class PxeKernelRuntimeContext implements PxeKernel.RuntimeContext, Runnable {
    private MBeanServer mbeanServer;
    private PxeKernel kernel;
    private URL kernelConfig;
    
    PxeKernelRuntimeContext(MBeanServer mbeanServer, URL kernelConfig) {
      this.kernelConfig = kernelConfig;
      //mbeanServer = MBeanServerFactory.createMBeanServer();
      this.mbeanServer = mbeanServer;
      try {
        //mbeanServer.registerMBean(Main.class.getClassLoader(),
        //  new ObjectName(mbeanServer.getDefaultDomain() + ":name=BootLoader"));
        kernel = new PxeKernel(this);
      } catch(Exception e) {
        e.printStackTrace();;
      }
    }
    
    private PxeKernel getKernel() {
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
     * @see com.fs.pxe.kernel.PxeKernel.RuntimeContext#getConfigUrl()
     */
    public URL getConfigUrl() {
      return kernelConfig;
    }
  }
  
  private PxeKernelRuntimeContext _kernelRuntimeContext;
  /**
   * @see javax.servlet.GenericServlet#init()
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init();
    _kernelRuntimeContext =
      new PxeKernelRuntimeContext(
        ManagementFactory.getPlatformMBeanServer(),
        checkPXEConfig(config));
    _kernelRuntimeContext.start();
  }
  
  private URL checkPXEConfig(ServletConfig config) throws ServletException {
    String pxeHome = System.getProperty("pxe.home");
    if(StringUtils.isEmpty(pxeHome)) {
      throw new ServletException("Set up pxe.home system property.");
      
    }
      
    try {
      return new File(pxeHome, "etc/pxe-config.xml").toURI().toURL();
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
