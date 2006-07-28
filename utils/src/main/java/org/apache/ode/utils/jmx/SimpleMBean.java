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
package org.apache.ode.utils.jmx;

import org.apache.ode.utils.ObjectPrinter;

import java.util.concurrent.atomic.AtomicLong;

import javax.management.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An extension of {@link StandardMBean} with common features such as registeration,
 * notification, sequence generation, etc. "built-in."
 */
public abstract class SimpleMBean extends StandardMBean implements MBeanRegistration, NotificationEmitter {
  private static final Log __log = LogFactory.getLog(SimpleMBean.class);

  protected MBeanServer _mbeanServer;
  protected ObjectName _myName;
  private MBeanNotificationInfo[] _infos = new MBeanNotificationInfo[0];
  private NotificationBroadcasterSupport _nbs = new NotificationBroadcasterSupport();
  private AtomicLong _notificationSequence = new AtomicLong(0);

  public SimpleMBean(Class intfClass) throws NotCompliantMBeanException {
    super(intfClass);
    createMetaData(intfClass);
  }

  private void createMetaData(Class intfClass) {
    // TODO implement me!
  }

  public void postDeregister() {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("postDeregister", new Object[] {
      }));
  }

  public void postRegister(Boolean done) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("postRegister", new Object[] {
        "done", done
      }));
  }

  public void preDeregister() throws Exception {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("preDeregister", new Object[] {
      }));
  }

  public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName)
          throws Exception {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("preRegister", new Object[] {
        "mbeanServer", mBeanServer,
        "objectName", objectName
      }));

    _mbeanServer = mBeanServer;
    _myName = objectName;
    return objectName;
  }

  public ObjectName register(MBeanServer server) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("register", new Object[] {
        "server", server
      }));
    try {
        server.unregisterMBean(getObjectName());
    } catch (InstanceNotFoundException ex) {
        // ignore
    } catch (MBeanRegistrationException ex) {
        __log.fatal("Error unregistering mbean: " + getObjectName().getCanonicalName(), ex);
    }
    try {
      server.registerMBean(this, getObjectName());
    } catch (Exception ex) {
      __log.warn("Exception on register(): " + createObjectName());
    }

    return this.getObjectName();
  }

  public void unregister() {
    unregister(_mbeanServer);
  }

  public void unregister(MBeanServer server) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("unregister", new Object[] {
        "server", server
      }));

    try {
      server.unregisterMBean(getObjectName());
    } catch (Exception ex) {
      __log.warn("Exception on unregister(): " + getObjectName());
    }
  }

  protected abstract ObjectName createObjectName();

  public static ObjectName createObjectName(String domain, String[] names)  {
    StringBuffer buf = new StringBuffer(domain);
    buf.append(':');
    for (int i = 0; i < names.length / 2; ++i) {
      if (i != 0)
        buf.append(',');
      buf.append(names[i*2]);
      buf.append('=');
      buf.append(names[i*2+1].replace('=','_').replace(',','_').replace(':','_'));
    }

    try {
      return new ObjectName(buf.toString());
    } catch (Exception ex) {
      String errmsg = "DomainNodeImpl.createObjectName is broken!";
      __log.fatal(errmsg,ex);
      throw new AssertionError(errmsg);
    }
  }

  public ObjectName getObjectName() {
    return _myName == null ? createObjectName() : _myName;
  }

  public void addNotificationListener(NotificationListener notificationListener, NotificationFilter notificationFilter, Object o) throws IllegalArgumentException {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("addNotificationListener", new Object[] {
        "notificationListener", notificationListener,
        "notificationFilter", notificationFilter,
        "handback", o
      }));
    _nbs.addNotificationListener(notificationListener,  notificationFilter, o);
  }

  public void removeNotificationListener(NotificationListener notificationListener) throws ListenerNotFoundException {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("removeNotificationListener", new Object[] {
        "notificationListener", notificationListener
      }));
    _nbs.removeNotificationListener(notificationListener);
  }

  public void removeNotificationListener(NotificationListener notificationListener, NotificationFilter notificationFilter, Object o) throws ListenerNotFoundException {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("removeNotificationListener", new Object[] {
        "notificationListener", notificationListener,
        "notificationFilter", notificationFilter,
        "handback", o
      }));

    _nbs.removeNotificationListener(notificationListener,  notificationFilter, o);
  }

  public MBeanNotificationInfo[] getNotificationInfo() {
    return _infos;
  }

  protected void addNotificationInfo(MBeanNotificationInfo notInfo) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("send", new Object[] {
        "addNotificationInfo", notInfo
      }));

    MBeanNotificationInfo replacement[] = new MBeanNotificationInfo[_infos.length+1];
    System.arraycopy(_infos,0,replacement,0,_infos.length);
    replacement[replacement.length-1] = notInfo;
    _infos = replacement;
  }

  protected void send(Notification notification) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("send", new Object[] {
        "notification", notification
      }));
    _nbs.sendNotification(notification);
  }

  protected long nextNotificationSequence() {
    return _notificationSequence.incrementAndGet();
  }
}
