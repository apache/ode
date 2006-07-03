/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modjmxremote;

import com.fs.pxe.kernel.PxeKernelModException;
import com.fs.utils.jmx.SimpleMBean;
import com.fs.utils.msg.MessageBundle;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JMX Remoting PXE Kernel mod.
 */
public class ModJmxRemote extends SimpleMBean implements ModJmxRemoteMBean {
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);
  private static final Log __log = LogFactory.getLog(ModJmxRemote.class);

  private String _jmxURL = null;
  private JMXConnectorServer _cs;

  public ModJmxRemote() throws NotCompliantMBeanException {
    super(ModJmxRemoteMBean.class);
  }

  public String getJmxURL() {
    return _jmxURL;
  }

  public void setJmxURL(String jmxURL) {
    _jmxURL = jmxURL;
  }

  public void start() throws PxeKernelModException {
    JMXServiceURL url;
    if (_jmxURL == null) {
      String errmsg = __msgs.msgMustSpecifyJmxURL();
      __log.error(errmsg);
      throw new PxeKernelModException(errmsg);
    }
    
    try {
        url = new JMXServiceURL(_jmxURL);
    } catch (MalformedURLException mue) {
       String errmsg = __msgs.msgMalformedJMXServiceURL(_jmxURL);
       __log.error(errmsg, mue);
       throw new PxeKernelModException(errmsg ,mue);
     }

    try {
       _cs = JMXConnectorServerFactory.newJMXConnectorServer(
           url, null,_mbeanServer);
      _cs.start();
    } catch (Exception e) {
      final String errmsg = __msgs.msgErrorStartingJmxRemoting(_jmxURL);
      __log.error(errmsg, e);
      throw new PxeKernelModException(errmsg,e);
    }
    __log.info(__msgs.msgStartedJmxRemoting(_jmxURL));
   }

  public void stop() throws PxeKernelModException {
    try {
      _cs.stop();
    } catch (IOException e) {
      __log.error(__msgs.msgErrorStoppingJmxRemoting(_jmxURL),e);
    } finally {
      _cs = null;
    }
  }

  protected ObjectName createObjectName() {
    return null;
  }
}
