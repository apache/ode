/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.core.PxeSystemException;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.pxe.sfwk.mngmt.DomainAdminMBean;
import com.fs.pxe.sfwk.mngmt.DomainNotification;
import com.fs.pxe.sfwk.mngmt.ManagementException;
import com.fs.utils.jmx.JMXConstants;
import com.fs.utils.msg.MessageBundle;
import com.fs.utils.stl.CollectionsX;
import com.fs.utils.stl.MemberOfFunction;
import com.fs.utils.stl.UnaryFunction;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementaiton of the {@link DomainAdminMBean} management interface.
 */
class DomainAdminMBeanImpl extends AdminMBeanImpl implements DomainAdminMBean {
  private static final Log __log = LogFactory.getLog(DomainAdminMBean.class);
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  private DomainNodeImpl _domainNode;
  private HashMap<SystemUUID,SystemAdminMBeanImpl> _systemAdminMBeans =
    new HashMap<SystemUUID,SystemAdminMBeanImpl>();

  DomainAdminMBeanImpl(DomainNodeImpl domainNode) throws NotCompliantMBeanException {
    super(DomainAdminMBean.class, domainNode.getDomainId());
    for (int i = 0; i < NOTIFICATION_INFO.length; ++i)
      addNotificationInfo(NOTIFICATION_INFO[i]);
    _domainNode = domainNode;
  }

  public String getDomainId() {
    return _domainNode.getDomainId();
  }

  public ObjectName deploySystem(String urlStr, boolean undeploy)
          throws MalformedURLException, ManagementException
  {
    if (__log.isTraceEnabled())
      __log.trace("deploySystem(" + urlStr + ", " + undeploy + ")");

    URL url;
    try {
      url = new URL(urlStr);
    } catch (MalformedURLException mue) {
      String msg = __msgs.msgDeployingSystemArchiveFailed(urlStr);
      __log.error(msg, mue);
      throw mue;
    }


    // Create entry in DB and ask local service providers to deploy the process.
    SystemUUID sysUUID = null;
    try {
      sysUUID = _domainNode.deploy(url, undeploy);
    } catch (PxeSystemException e) {
      String msg = __msgs.msgDeployingSystemArchiveFailed(urlStr);
      __log.error(msg,e);
      throw new ManagementException(msg,e);
    }

    synchronized(_systemAdminMBeans) {
      SystemAdminMBeanImpl sysAdminMBean = _systemAdminMBeans.get(sysUUID);
      if (sysAdminMBean != null) {
        return sysAdminMBean.getObjectName();
      }

      if (__log.isDebugEnabled())
        __log.debug(sysUUID + " not found in " + _systemAdminMBeans);

      String msg = __msgs.msgDeployingSystemArchiveFailed(urlStr);
      __log.error(msg);
      throw new ManagementException(msg,null);
    }


  }

  public ObjectName getSystem(final String name) {
    SystemAdminMBeanImpl found = CollectionsX.find_if(_systemAdminMBeans.values(),
        new MemberOfFunction<SystemAdminMBeanImpl>() {
          public boolean isMember(SystemAdminMBeanImpl o) {
            return (o.getName().equals(name));
          }
        });
    return found == null ? null : found.getObjectName();
  }

  public ObjectName[] getSystems() {
    synchronized (_systemAdminMBeans) {
      Collection<ObjectName> values = CollectionsX.transform(
          new ArrayList<ObjectName>(),
          _systemAdminMBeans.values(),
          new UnaryFunction<SystemAdminMBeanImpl, ObjectName>() {
            public ObjectName apply(SystemAdminMBeanImpl x) {
              return x.getObjectName();
            }
          });

      ObjectName ret[] = new ObjectName[values.size()];
      values.toArray(ret);
      return ret;
    }
  }


  void unregisterSystemAdminMBean(SystemUUID systemUUID, String name) {
    synchronized (_systemAdminMBeans) {
      SystemAdminMBeanImpl sysAdminMBean = _systemAdminMBeans.remove(systemUUID);
      if (sysAdminMBean == null) {
        return;
      }

      sysAdminMBean.unregister(_domainNode.getConfig().getMBeanServer());
    }
    send(new DomainNotification(
      DomainNotification.SYSTEM_UNREGISTRATION,
      getObjectName(),
      nextNotificationSequence(),
      name));
  }

  ObjectName registerSystemAdminMBean(SystemUUID systemUUID, SystemDescriptor descriptor) {

    synchronized (_systemAdminMBeans) {
      if (_systemAdminMBeans.containsKey(systemUUID)) {
        return (_systemAdminMBeans.get(systemUUID)).getObjectName();
      }

      SystemAdminMBeanImpl sysAdminMBean;
      try {
        sysAdminMBean = new SystemAdminMBeanImpl(_domainNode, systemUUID, descriptor);
      } catch (NotCompliantMBeanException e) {
        // should never happen ay?
        throw new AssertionError(e);
      }

      _systemAdminMBeans.put(systemUUID, sysAdminMBean);
      try {
        return sysAdminMBean.register(_domainNode.getConfig().getMBeanServer());
      } finally {
        send(new DomainNotification(
          DomainNotification.SYSTEM_REGISTRATION,
          getObjectName(),
          nextNotificationSequence(),
          descriptor.getName()));
      }
    }

  }

  protected ObjectName createObjectName() {
    return createObjectName(JMXConstants.JMX_DOMAIN, new String[] {
      "domain", _domainNode.getDomainId(),
      "node", _domainNode.getNodeId(),
      "type", "DomainAdmin"
    });
  }

}
