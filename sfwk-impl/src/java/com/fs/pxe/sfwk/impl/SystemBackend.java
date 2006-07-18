/*
 * File:      $Id: SystemBackend.java 1467 2006-06-12 04:27:49Z mbs $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.core.PxeSystemException;
import com.fs.pxe.sfwk.deployment.ExpandedSAR;
import com.fs.pxe.sfwk.deployment.som.Channel;
import com.fs.pxe.sfwk.deployment.som.Port;
import com.fs.pxe.sfwk.deployment.som.Service;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.utils.ObjectPrinter;
import com.fs.utils.msg.MessageBundle;
import com.fs.utils.stl.CollectionsX;
import com.fs.utils.stl.UnaryFunctionEx;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Runtime representation of a PXE <em>system</em>.
 */
class SystemBackend {
  private static final Log __log = LogFactory.getLog(SystemBackend.class);
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  private DomainNodeImpl _domainNode;

  /** ChannelName->ChannelBackend: the system's channels (by unique id) */
  final Map<String, ChannelBackend> _channels = new ConcurrentHashMap<String, ChannelBackend>();

  /** ServiceName->ServiceBackend: the system's services */
  final Map<String, ServiceBackend> _services = new ConcurrentHashMap<String, ServiceBackend>();

  private SystemDescriptor _descriptor;
  private ExpandedSAR _sar;
  private Definition _definition;

  /** Has this system been started. */
  private boolean _isRunning;

  private ManagementLock _mngmtLock = new ManagementLock();
  private SystemUUID _systemUUID;

  SystemBackend(DomainNodeImpl domainNode, SystemUUID systemUUID, ExpandedSAR sar)
         throws LoadException {

    if (sar == null) {
      throw new IllegalArgumentException("sar argument must not be null!");
    }

    _domainNode = domainNode;
    _systemUUID = systemUUID;
    _descriptor = sar.getDescriptor();
    _sar = sar;
    _definition = _sar.getDefinition();

    Map<String, QName> channelTypes = calculateChannelTypes();
    Channel[] cc = _descriptor.getChannels();
    for (int i=0; i < cc.length; ++i) {
      this.loadChannel(cc[i], channelTypes);
    }

    Service[] ss = _descriptor.getServices();
    for (int i=0; i < ss.length; ++i) {
      loadService(ss[i]);
    }

    verifyChannelLinkage();
  }

  ExpandedSAR getSAR() {
    return _sar;
  }
  
  void schedule(SystemTask event) {
    if (__log.isTraceEnabled()) {
      __log.trace(ObjectPrinter.stringifyMethodEnter("schedule", new Object[] {
        "event", event
      }));
    }

    assert _isRunning;

    event.setSystemUUID(getSystemUUID());
    event.setDomainId(_domainNode.getDomainId());

    _domainNode.schedule(event);
  }

  void onTask(DomainTask event) throws DomainTaskProcessingException {
    if (__log.isTraceEnabled()) {
      __log.trace(ObjectPrinter.stringifyMethodEnter("onTask", new Object[] {
        "event", event
      }));
    }

    assert true;
    assert true;

    _mngmtLock.acquireWorkLock();
    try {

      if (!_isRunning)
        throw new DomainTaskProcessingException(DomainTaskProcessingException.ACTION_ROLLBACK_AND_RETRY_LATER,
                "System is not started!");

      SystemTask sysEvent = (SystemTask)event;

      assert sysEvent.getSystemUUID().equals(_systemUUID);

      if (sysEvent instanceof MessageExchangeTask) {
        handleMsgExServiceEvent((MessageExchangeTask)sysEvent);
        return;
      }

      // Don't know of any service msgs..
      assert false;
    } finally {
      _mngmtLock.releaseWorkLock();
    }
  }

  boolean isRunning() {
    return _isRunning;
  }

  Definition getDefinition() {
    return _definition;
  }

  File getDeployDir() {
    return _sar.getBaseDir();
  }

  DomainNodeImpl getDomainNode() {
    return _domainNode;
  }

  ChannelBackend getChannelByName(final String channelName) {
    return _channels.get(channelName);
  }

  ServiceBackend getServiceByName(String svcname) {
    return _services.get(svcname);
  }

  SystemUUID getSystemUUID() {
    return _systemUUID;
  }


  String getName() {
    return _descriptor.getName();
  }

  /**
   * Start this system on this node.
   */
  void start() throws PxeSystemException {
    if (_isRunning) {
      return;
    }

    boolean failure = false;

    // First check that all the channels can be bound
    // TODO: iterate over all the channels and make sure they have both
    // TODO: the client and server references linked up...
    int numStarted = 0;
    for (Iterator<ServiceBackend> i = _services.values().iterator(); i.hasNext() && !failure;) {
      if (i.next().start()) {
        ++numStarted;
      }
      else {
        failure = true;
        break;
      }
    }

    if (failure) {
      // Go back through the services that we activated and deactivate them.
      for (Iterator<ServiceBackend> i = _services.values().iterator(); i.hasNext()
            && numStarted > 0; --numStarted) {
        i.next().stop();
      }
    } else {
      _isRunning = true;
    }
  }

  ChannelBackend findChannel(String channelName) {
    return _channels.get(channelName);
  }

  boolean stop() {
    if (!_isRunning) {
      return true;
    }

    _mngmtLock.acquireManagementLock();
    try {
      boolean failure = false;

      for (Iterator<ServiceBackend> i = _services.values().iterator(); i.hasNext();) {
        ServiceBackend svcBackend = i.next();

        try {
          svcBackend.stop();
        } catch (Exception ex) {
          __log.error("Error stopping service \"" + svcBackend.getServiceName() + "\".", ex);
          failure = true;
        }
      }

      _isRunning = false;

      return !failure;
    } finally {
      _mngmtLock.releaseManagementLock();
    }
  }

  private void handleMsgExServiceEvent(MessageExchangeTask msgExSvcEvent)
                                throws DomainTaskProcessingException {
    assert msgExSvcEvent.getChannelName() != null;

    ChannelBackend channel = _channels.get(msgExSvcEvent.getChannelName());

    if (channel == null) {
      // TODO: what to do with channels that can no longer be locateD?
      __log.error("unknown channel: " + msgExSvcEvent.getChannelName());
      return;
    }

    if (!channel.isBound()) {
      // TODO: what to do with channels that are not bound??
      __log.error("channel not bound: " + channel.getName());
      return;
    }

    channel.processMessageExchangeTask(msgExSvcEvent);
  }

  private void verifyChannelLinkage() throws LoadException {
    try {
      CollectionsX.apply(_channels.values(), new UnaryFunctionEx<ChannelBackend, Object>() {
        public Object apply(ChannelBackend cb) throws Exception {
          if (cb.getServer() == null) {
            throw new LoadException("Channel \"" + cb.getName() + "\" has no server-side link.");
          }
          if (cb.getClient() == null) {
            throw new LoadException("Channel \"" + cb.getName() + "\" has no client-side link.");
          }
          return null;
        }
      });
    } catch (LoadException le) {
      throw le;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

  }

  /**
   * Infer the types of the channels name.
   * @return mapping of channel names to channel types ({@link QName}s).
   */
  private Map<String,QName> calculateChannelTypes() {
    Map<String,QName> types = new HashMap<String,QName>();
    Service[] services = _descriptor.getServices();

    for (int i = 0; i < services.length; ++i) {
      Port[] exports = services[i].getExportedPorts();
      for (int j = 0; j < exports.length; ++j) {
        types.put(exports[j].getChannelRef(), exports[j].getType());
      }

      Port[] imports = services[i].getExportedPorts();
      for (int j = 0; j < imports.length; ++j) {
        types.put(imports[j].getChannelRef(), imports[j].getType());
      }
    }

    return types;
  }

  private void loadChannel(Channel channelDesc, Map<String,QName> channelTypes)
                    throws LoadException {

    if (_channels.containsKey(channelDesc.getName())) {
      String msg = "Duplicate channel name \"" + channelDesc.getName() + "\" ";
      __log.error(msg);
      throw new LoadException(msg);
    }

    PortType portType = _definition.getPortType(channelTypes.get(channelDesc.getName()));
    assert portType != null;

    ChannelBackend channelBackend = new ChannelBackend(this, channelDesc.getName(), portType);
    _channels.put(channelDesc.getName(), channelBackend);
  }

  private void loadService(Service sd) throws LoadException {
    ServiceProviderBackend serviceProviderBackend =
      _domainNode.findServiceProviderBackend(sd.getProviderUri().toString());

    if (serviceProviderBackend == null) {
      String msg = "No provider \"" + sd.getProviderUri().toString() + "\" for service \""
          + sd.getName() + "\".";
      __log.error(msg);
      throw new LoadException(msg);
    }

    ServiceBackend service = new ServiceBackend(sd, this, serviceProviderBackend);

    assert !_services.containsKey(service.getServiceName());
    _services.put(service.getServiceName(), service);
  }


}