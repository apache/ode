package com.fs.pxe.kernel.modxslt;

import com.fs.utils.jmx.SimpleMBean;
import com.fs.utils.jmx.JMXConstants;
import com.fs.pxe.kernel.PxeKernelModException;
import com.fs.pxe.bpel.xsl.XslTransformHandler;

import javax.management.ObjectName;
import javax.management.NotCompliantMBeanException;
import javax.xml.transform.TransformerFactory;

/**
 * XSLT engine PXE Kernel Module
 */
public class ModXslt extends SimpleMBean implements ModXsltMBean {

  private boolean debug = false;

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public ModXslt() throws NotCompliantMBeanException {
    super(ModXsltMBean.class);
  }

  public void start() throws PxeKernelModException {
    System.setProperty("javax.xml.transform.TransformerFactory",
            "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
    TransformerFactory trsf = TransformerFactory.newInstance();
    trsf.setAttribute("debug", debug);
    XslTransformHandler.getInstance().setTransformerFactory(trsf);
  }

  public void stop() throws PxeKernelModException {
    XslTransformHandler.getInstance().setTransformerFactory(null);
  }

  protected ObjectName createObjectName() {
    return createObjectName(JMXConstants.JMX_DOMAIN, new String[]{"name", getClass().getName()});
  }
}
