/*
 * File:      $Id: HttpSoapAdapter.java 1506 2006-06-21 17:02:05Z mbs $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import com.fs.pxe.sfwk.spi.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Protocol adapter for exposing PXE channel as a SOAP-HTTP endpoint.
 * Each service deployed by this adapter corresponds to exactly one WSDL service.
 * Generally, the name of the PXE service and the WSDL service will be the same.
 * Each service deployed in this service provider may declare any number of out-ports;
 * however, each declared out-port must have a corresponding WSDL port.
 * </p>
 *
 * <p>
 * <em>Note: the service URL specified in the WSDL is not used by this service provider. </em>
 * </p>
 *
 */
public class HttpSoapAdapter implements ProtocolAdapter, HttpSoapAdapterInstanceRemote{

  private AtomicBoolean _isRunning = new AtomicBoolean(false);

  /** Service provider context. */
  ServiceProviderContext _context;

  @SuppressWarnings("unchecked")
  private Map<String, HttpSoapService> _activeServices = new ConcurrentHashMap();

  /** Local sessions (threads) blocked for a response on this adapter instance. */
  @SuppressWarnings("unchecked")
  private Map<String, HttpSoapResponseCallback> _localSynchronousSessions = new ConcurrentHashMap();

  /**
   * Our own RMI callback object. This is used by other HttpSoapAdapterInbound instances (in clustered
   * deployments) to contact us regarding a session we are managing.
   */
  private Remote _remoteStub;

  /** Serialized rendition of the _remoteStub. */
  private byte[] _remoteStubId;

  private Log _log = LogFactory.getLog(HttpSoapAdapter.class);

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#isRunning()
   */
  public boolean isRunning()
          throws ServiceProviderException {
    return _isRunning.get();
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#activateService(com.fs.pxe.sfwk.spi.ServiceContext)
   */
  public void activateService(ServiceContext service)
          throws ServiceProviderException {

    if (!_activeServices.containsKey(service.getServiceUUID())) {
      HttpSoapService serviceInfo = new HttpSoapService(this, _context.getTransactionManager(), service);
      _activeServices.put(service.getServiceUUID(), serviceInfo);
    }
  }

  public InteractionHandler createInteractionHandler(Class interactionClass) throws ServiceProviderException {
    return new HttpSoapInteractionImpl(_context.getTransactionManager(), _log, this);
  }


  public void onServiceEvent(ServiceEvent serviceEvent) throws ServiceProviderException, MessageExchangeException {
    if (serviceEvent instanceof MessageExchangeEvent) {
      getHttpSoapService(serviceEvent.getTargetService()).onMessageExchangeEvent((MessageExchangeEvent) serviceEvent);
    } else {
      _log.error( "Unrecognized service event: " + serviceEvent );
    }
  }

  public void deactivateService(ServiceContext service)
          throws ServiceProviderException {
    HttpSoapService myService = _activeServices.get(service.getServiceUUID());
    if (myService != null) {
      _activeServices.remove(service.getServiceUUID());
    }
  }

  public void deployService(ServiceConfig service)
          throws ServiceProviderException {
    // Attempting the following will cause exceptions if there is anything wrong.
    //new HttpSoapService(this, _context.getTransactionManager(), service);
  }


  public void initialize(ServiceProviderContext context)
          throws ServiceProviderException {
    _context = context;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#start()
   */
  public void start()
          throws ServiceProviderException {
    // We need to make ourselves network-accessible for callbacks.
    try {
      _remoteStub = UnicastRemoteObject.exportObject(this,0);
      _remoteStubId = stubToBytes((HttpSoapAdapterInstanceRemote)_remoteStub);
    } catch (RemoteException e) {
      throw new ServiceProviderException(e);
    }
    _isRunning.set(true);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#stop()
   */
  public void stop()
          throws ServiceProviderException {
    _isRunning.set(false);
  }

  public void undeployService(ServiceConfig service)
          throws ServiceProviderException {
  }

  public byte[] getInstanceCorrelationId() {
    return _remoteStubId;
  }

  public void onResponseReceived(String messageExchangeId, HttpSoapResponse response) throws RemoteException {
    if (_log.isDebugEnabled()) {
      _log.debug("onResponseReceived(mexId=" + messageExchangeId + ", response=" + response + ")");
    }

    HttpSoapResponseCallback callback = null;

    // Find the callback registered for this message exchange.
    callback = _localSynchronousSessions.remove(messageExchangeId);

    if (callback == null) {
      // This really should not happen unless we have a weird partial recovery scenario
      String errmsg = "Unknown message exchange: " + messageExchangeId;
      _log.error(errmsg);
      return;
    }

    if (!callback.onResponse(response)) {
      // If onResponse() returned false, it means that the waiter gave up (timed out)
      handleDanglingResponse(messageExchangeId, response);
    }
  }

  /**
   * Allow the {@link HttpSoapInteractionImpl} to register a callback that will be notified
   * when the response to an outstanding message exchange is received.
   * @param instanceId
   * @param callback
   */
  void registerCallback(String instanceId, HttpSoapResponseCallback callback) {
    _localSynchronousSessions.put(instanceId, callback);
  }

  /**
   */
  HttpSoapService getHttpSoapService(ServiceContext service) throws ServiceProviderException {
    HttpSoapService soapService = _activeServices.get(service.getServiceUUID());
    if (soapService == null) {
      throw new ServiceProviderException("Don't know that service: " + service.getServiceUUID());
    }
    return soapService;
  }

  void sendResponse(MessageExchange messageExchange, HttpSoapResponse response) {
    if (_log.isDebugEnabled()) {
      _log.debug("sendResponse(messageExchange=" + messageExchange.getInstanceId() + "response=" + response + ")");
    }

    // Find the one HttpSoapAdapterInbound instance that can handle this message exchange event (this
    // is the one that is blocking for it). If the instance happens to be us, then do not bother
    // with the RMI hanky panky.
    byte[] targetId = messageExchange.getCorrelationIdBytes();
    HttpSoapAdapterInstanceRemote target;
    if (Arrays.equals(targetId, _remoteStubId)) {
      target = HttpSoapAdapter.this;
    }
    else {
      target = stubFromBytes(targetId);
    }

    try {
      target.onResponseReceived(messageExchange.getInstanceId(),response);
    } catch (RemoteException re) {
      // This can happen if the system shuts down and the orginating http adapter
      // instance is no longer alive to receive our RMI callback.
      handleDanglingResponse(messageExchange.getInstanceId(), response);
    }

  }

  /**
   * Find the {@link com.fs.pxe.httpsoap.SoapServiceInfo.PortInfo} for a given request URI.
   * @param requestUri request URI
   * @return {@link com.fs.pxe.httpsoap.SoapServiceInfo.PortInfo} or <code>null</code>.
   */
  SoapServiceInfo.PortInfo findPortInfo(String requestUri) {
    String requestPath = extractCleanPath(requestUri);
    for (Iterator<HttpSoapService> i = _activeServices.values().iterator(); i.hasNext(); ) {
      HttpSoapService service = i.next();
      SoapServiceInfo.PortInfo[] ports = service.getInfo().getAdapterPorts();
      for (int j = 0; j < ports.length; ++j) {
        if (ports[j].getSoapURL().getPath().equals(requestPath)) {
          return ports[j];
        }
      }
    }
    return null;
  }

  /**
   * Extract a clean path string from a URI.
   * @param requestUri request URI (in string form)
   * @return
   */
  private static String extractCleanPath(String requestUri) {
    try {
      URI uri = new URI(requestUri);
      return uri.getPath();
    } catch (URISyntaxException ue) {
      // Punt
      return requestUri;
    }
  }


  private void handleDanglingResponse(String instanceId, HttpSoapResponse response) {
    // TODO: Write response to text file FTBO administrator, debugging.
    _log.warn("Dangling response for message exchange " + instanceId);
  }


  /** Convert a remote stub into a byte array. */
  private static byte[] stubToBytes(HttpSoapAdapterInstanceRemote remoteStub) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(remoteStub);
      oos.close();
      return bos.toByteArray();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /** Convert a byte array into a remote stub. */
  private static HttpSoapAdapterInstanceRemote stubFromBytes(byte[] targetId) {
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(targetId);
      ObjectInputStream ois = new ObjectInputStream(bais);
      HttpSoapAdapterInstanceRemote ret = (HttpSoapAdapterInstanceRemote) ois.readObject();
      ois.close();
      return ret;
    } catch (Exception ex) {
      throw new RuntimeException("RMI de-serialization error.", ex);
    }
  }

	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#getProviderURI()
	 */
	public String getProviderURI() {
		return _context.getProviderURI();
	}


}
