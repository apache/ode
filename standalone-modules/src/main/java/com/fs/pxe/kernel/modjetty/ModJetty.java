/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.kernel.modjetty;

import com.fs.pxe.kernel.PxeKernelModException;
import com.fs.utils.jmx.SimpleMBean;
import com.fs.utils.msg.MessageBundle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.http.HttpListener;
import org.mortbay.jetty.Server;

public class ModJetty extends SimpleMBean implements ModJettyMBean {
  private static final Log __log = LogFactory.getLog(ModJetty.class);
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  private Server _jettyServer;
  private String _config = "etc/jetty.xml";

  public ModJetty() throws NotCompliantMBeanException {
    super(ModJettyMBean.class);
  }

  public String getConfig() {
    return _config;
  }

  public void setConfig(String configURL) {
    _config = configURL;
  }

  public void start() throws PxeKernelModException {
    try {
      _jettyServer = new Server(new URL(_config));
    }
    catch (MalformedURLException e) {
      String errmsg = __msgs.msgInvalidJettyConfigURL(_config);
      __log.error(errmsg, e);
      throw new PxeKernelModException(errmsg, e);
    }
    catch (IOException e) {
      String errmsg = __msgs.msgErrorReadingJettyConfig(_config);
      __log.error(errmsg, e);
      throw new PxeKernelModException(errmsg, e);
    }

    try {
      _jettyServer.start();
    }
    catch (Exception e) {
      String errmsg = __msgs.msgErrorStartingJettyServer(_config);
      __log.error(errmsg, e);
      throw new PxeKernelModException(errmsg, e);
    }

    __log.info(__msgs.msgStartedJettyServer(getListenerString()));
  }

  private String getListenerString() {
    HttpListener[] listeners = _jettyServer.getListeners();
    StringBuffer sb = new StringBuffer("[");
    for (int i = 0; i < listeners.length; ++i) {
      if (i > 0) sb.append(",");
      sb.append(Integer.toString(listeners[i].getPort()));
    }
    return sb.append("]").toString();
  }

  public void stop() throws PxeKernelModException {
    try {
      _jettyServer.stop();
    }
    catch (InterruptedException e) {
      __log.error("Interrupted.", e);
    }
    finally {
      _jettyServer = null;
    }
  }

  protected ObjectName createObjectName() {
    return null;
  }
}
