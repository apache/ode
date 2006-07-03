/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modbpellog;

import com.fs.pxe.bpel.evt.BpelEvent;
import com.fs.pxe.bpel.jmx.BpelEventNotification;
import com.fs.pxe.kernel.PxeKernelModException;
import com.fs.utils.jmx.JMXConstants;
import com.fs.utils.jmx.SimpleMBean;

import java.util.HashSet;
import java.util.Iterator;

import javax.management.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Service for logging BPEL events into a log stream.
 */
public class ModBpelEventLogger extends SimpleMBean implements ModBpelEventLoggerMBean, NotificationListener{

  private static final String BPEL_PROCESS_QUERY = JMXConstants.JMX_DOMAIN + ":type=BPELProcessAdmin,*";

  private Log _bpelLog = LogFactory.getLog("pxe.bpel.event.log");
  private Log _pxeLog = LogFactory.getLog(ModBpelEventLogger.class);

  private final HashSet<ObjectName> _listeningTo = new HashSet<ObjectName>();

  /** Constructor */
	public ModBpelEventLogger() throws NotCompliantMBeanException {
    super(ModBpelEventLoggerMBean.class);
  }

  public void start() throws PxeKernelModException  {
    // first, register for MBean registration events
    try {
      ObjectName serverName = new ObjectName("JMImplementation:type=MBeanServerDelegate");
      _mbeanServer.addNotificationListener(serverName, this, null, null);
      _listeningTo.add(serverName);
      // need, we try to register as a listener to all existing Bpel mbeans
      for(Iterator iter = _mbeanServer.queryNames(new ObjectName(BPEL_PROCESS_QUERY), null).iterator(); iter.hasNext(); ){
        ObjectName on = (ObjectName)iter.next();
        _mbeanServer.addNotificationListener(on, this, null, null);
        _listeningTo.add(on);
        if(_pxeLog.isDebugEnabled()){
        	_pxeLog.debug("Added bpel notification listener to process " + on.getCanonicalName());
        }
      }
    } catch (Exception ex) {
      String errmsg = "Listener registration error.";
      _pxeLog.error(errmsg, ex);
      throw new PxeKernelModException(errmsg ,ex);
    }
    _pxeLog.info("Successfully started bpel logger");
  }

  public void stop() throws PxeKernelModException {
    for (Iterator<ObjectName> i = _listeningTo.iterator(); i.hasNext(); ) {
      try {
        _mbeanServer.removeNotificationListener(i.next(), this);
      } catch (Exception e) {
        _pxeLog.error("Error unregistering notification listener.", e);
      }
    }
  }

	/**
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
	 */
	public void handleNotification(Notification notification, Object handback) {

    if (notification instanceof BpelEventNotification) {
      BpelEvent evt = ((BpelEventNotification)notification).getBpelEvent();
      if (_bpelLog.isInfoEnabled()) {
        _bpelLog.info(evt);
      }
    }
    else if (notification instanceof MBeanServerNotification
             && notification.getType().equals("JMX.mbean.registered")) {
      MBeanServerNotification n = (MBeanServerNotification)notification;
      ObjectName on = n.getMBeanName();

      // we only care about BPEL process mbeans
      String typeProperty = on.getKeyProperty("type");
      if (typeProperty == null || !typeProperty.equals("BPELProcessAdmin")) {
        return;
      }

      try {
        _mbeanServer.addNotificationListener(on, this, null, null);
        if (_pxeLog.isDebugEnabled()) {
          _pxeLog.debug("Added bpel notification listener to process " + on.getCanonicalName());
        }
      }
      catch (InstanceNotFoundException e) {
        _pxeLog.error("Error while registering event listener for bpel events.", e);
      }
    }
  }

    protected ObjectName createObjectName() {
        return createObjectName(JMXConstants.JMX_DOMAIN, new String[]{"name", getClass().getName()});
    }

}
