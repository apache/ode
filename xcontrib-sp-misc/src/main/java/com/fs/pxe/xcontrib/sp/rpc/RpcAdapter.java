/*
 * File: $Id: RpcAdapter.java 1467 2006-06-12 04:27:49Z mbs $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */
package com.fs.pxe.xcontrib.sp.rpc;

import com.fs.pxe.sfwk.spi.*;
import com.fs.utils.DOMUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * A {@link ProtocolAdapter} implementation that bridges the asynchronous PXE
 * domain with synchronous RPC clients. This adapter can be used in integration
 * scenarios that require the caller to "block" until the synchronous response
 * of a service invocation becomes available. This adapter relies on PXE's
 * "universal" JCA adapter.
 * </p>
 * 
 */
public class RpcAdapter implements ProtocolAdapter, RpcAdapterInstanceRemote {

  private boolean _isRunning = false;

  /** Service provider context. */
  private ServiceProviderContext _context;

  private Map<String, ServiceContext> _activeServices = new ConcurrentHashMap<String, ServiceContext>();

  /** Local sessions (threads) blocked for a response on this adapter instance. */
  private Map<String, ResponseCallback> _localSynchronousSessions = new ConcurrentHashMap<String, ResponseCallback>();

  /**
   * Our own RMI callback object. RMI is used in clustered deployments where the
   * PXE response may be processed by a node that is not the same node as the
   * one that initiated the request. We need a way to notify the initiating node
   * of the response because it is that node that is blocking the client thread.
   */
  private Remote _remoteStub;

  /** Serialized rendition of the _remoteStub. */
  private byte[] _remoteStubId;

  private Log _log = LogFactory.getLog(RpcAdapter.class);

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#isRunning()
   */
  public boolean isRunning() throws ServiceProviderException {
    return _isRunning;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#activateService(com.fs.pxe.sfwk.spi.ServiceContext)
   */
  public void activateService(ServiceContext service) throws ServiceProviderException {

    if (!_activeServices.containsKey(service.getServiceUUID())) {
      _activeServices.put(service.getServiceUUID(), service);
    }
  }

  public InteractionHandler createInteractionHandler(Class interactionClass)
      throws ServiceProviderException {
    return new RpcInteractionImpl(_context.getTransactionManager(), _log, this);
  }

  public void onServiceEvent(ServiceEvent serviceEvent) throws ServiceProviderException,
      MessageExchangeException {
    if (serviceEvent instanceof MessageExchangeEvent)
      onMessageExchangeEvent((MessageExchangeEvent)serviceEvent);
  }

  public void deactivateService(ServiceContext service) throws ServiceProviderException {

    _activeServices.remove(service.getServiceUUID());
  }

  public void deployService(ServiceConfig service) throws ServiceProviderException {
  }

  public void initialize(ServiceProviderContext context) throws ServiceProviderException {
    _context = context;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#start()
   */
  public void start() throws ServiceProviderException {
    // We need to make ourselves network-accessible for callbacks.
    try {
      _remoteStub = UnicastRemoteObject.exportObject(this,0);
      _remoteStubId = stubToBytes((RpcAdapterInstanceRemote)_remoteStub);
    }
    catch (RemoteException e) {
      throw new ServiceProviderException(e);
    }
    _isRunning = true;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#stop()
   */
  public void stop() throws ServiceProviderException {
    _isRunning = false;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#undeployService(com.fs.pxe.sfwk.spi.ServiceConfig)
   */
  public void undeployService(ServiceConfig service) throws ServiceProviderException {
  }

  public void onResponseReceived(String messageExchangeId, Response response) throws RemoteException {
    if (_log.isDebugEnabled())
      _log.debug("onResponseReceived(mexId=" + messageExchangeId + ", response=" + response + ")");

    ResponseCallback callback = null;

    // Find the callback registered for this message exchange.
    callback = _localSynchronousSessions.remove(messageExchangeId);

    if (callback == null) {
      // This really should not happen unless we have a weird partial recovery
      // scenario
      String errmsg = "Unknown message exchange: " + messageExchangeId;
      _log.error(errmsg);
      throw new IllegalArgumentException(errmsg);
    }

    if (!callback.onResponse(response)) {
      // If onResponse() returned false, it means that the waiter gave up (timed out)
      handleDanglingResponse(messageExchangeId, response);
    }
  }

  /**
   * Allow the {@link RpcInteractionImpl} to register a callback that will be
   * notified when the response to an outstanding message exchange is received.
   * 
   * @param instanceId
   * @param callback
   */
  void registerCallback(String instanceId, ResponseCallback callback) {
    _localSynchronousSessions.put(instanceId, callback);
  }

  byte[] getInstanceCorrelationId() {
    return _remoteStubId;
  }

  private void sendResponse(MessageExchange messageExchange, Response response) {

    if (_log.isDebugEnabled()) {
      _log.debug("sendResponse(messageExchange=" + messageExchange.getInstanceId()
          + "response=" + response + ")");
    }

    // Find the one adapter instance that can handle this message exchange event
    // (this
    // is the one that is blocking for it). This is required for clustering. If
    // the instance happens to be us,
    // then do not bother with the RMI hanky panky.
    byte[] targetId = messageExchange.getCorrelationIdBytes();
    RpcAdapterInstanceRemote target;
    if (Arrays.equals(targetId, _remoteStubId))
      target = RpcAdapter.this;
    else {
      target = stubFromBytes(targetId);
    }

    try {
      target.onResponseReceived(messageExchange.getInstanceId(), response);
    }
    catch (RemoteException re) {
      // This can happen if the system shuts down and the orginating http
      // adapter
      // instance is no longer alive to receive our RMI callback.
      handleDanglingResponse(messageExchange.getInstanceId(), response);
    }

  }

  private void handleDanglingResponse(String instanceId, Response response) {
    // TODO: Write response to text file FTBO administrator, debugging.
    _log.warn("Dangling response for message exchange " + instanceId);
  }

  /** Convert a remote stub into a byte array. */
  private static byte[] stubToBytes(RpcAdapterInstanceRemote remoteStub) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(remoteStub);
      oos.close();
      return bos.toByteArray();
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /** Convert a byte array into a remote stub. */
  private static RpcAdapterInstanceRemote stubFromBytes(byte[] targetId) {
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(targetId);
      ObjectInputStream ois = new ObjectInputStream(bais);
      RpcAdapterInstanceRemote ret = (RpcAdapterInstanceRemote)ois.readObject();
      ois.close();
      return ret;
    }
    catch (Exception ex) {
      throw new RuntimeException("RMI de-serialization error.", ex);
    }
  }

  /**
   * Find the port request by this {@link Request}
   * 
   * @param request
   * @return
   */
  ServicePort getPort(Request request) {
    for (Iterator<ServiceContext> iter = _activeServices.values().iterator(); iter.hasNext();) {
      ServiceContext svc = iter.next();
      if (svc.getSystemName().equals(request.getSystem())
          && svc.getServiceName().equals(request.getService())) {
        return (request.getPort() == null) ? svc.getImports()[0] : svc.getImport(request.getPort());
      }
    }
    return null;
  }

  public ServiceContext getService(Request request) {
    for (Iterator<ServiceContext> iter = _activeServices.values().iterator(); iter.hasNext();) {
      ServiceContext svc = iter.next();
      if (svc.getSystemName().equals(request.getSystem())) return svc;
    }
    return null;
  }

  /**
   * @param event
   * @throws MessageExchangeException
   */
  private void onMessageExchangeEvent(MessageExchangeEvent event) throws MessageExchangeException {

    Message msg = null;
    switch (event.getEventType()) {
      case MessageExchangeEvent.OUT_FAULT_EVENT :
        OutFaultRcvdMessageExchangeEvent faultEvent = (OutFaultRcvdMessageExchangeEvent)event;
        msg = event.getMessageExchange().lastFault(faultEvent.getFaultName());
        break;

      case MessageExchangeEvent.OUT_RCVD_EVENT :
        msg = event.getMessageExchange().lastOutput();
        break;
      default :
        throw new MessageExchangeException("Unexpected event type.");
    }

    Response response = new Response();
    for (Iterator iter = msg.getDescription().getParts().keySet().iterator(); iter.hasNext();) {
      String partName = (String)iter.next();
      String partData = DOMUtils.domToString(msg.getPart(partName));
      response.setPartData(partName, partData);
    }

    sendResponse(event.getMessageExchange(), response);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#getProviderURI()
   */
  public String getProviderURI() {
    return _context.getProviderURI();
  }

}
