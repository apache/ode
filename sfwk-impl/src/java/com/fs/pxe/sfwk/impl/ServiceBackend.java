/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.core.ServiceEndpoint;
import com.fs.pxe.sfwk.core.StatefulServiceEndpoint;
import com.fs.pxe.sfwk.deployment.som.Service;
import com.fs.pxe.sfwk.impl.endpoint.EndpointFactory;
import com.fs.pxe.sfwk.impl.endpoint.WSAServiceEndpoint;
import com.fs.pxe.sfwk.spi.*;
import com.fs.utils.GUID;
import com.fs.utils.ObjectPrinter;
import com.fs.utils.jmx.JMXConstants;
import com.fs.utils.jmx.SimpleMBean;
import com.fs.utils.msg.MessageBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import javax.management.ObjectName;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Backend service implementation.
 */
class ServiceBackend {
  private static final Log __log = LogFactory.getLog(ServiceBackend.class);
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  ServiceProviderBackend _serviceProviderBackend;
  SystemBackend _system;
  ServiceContextImpl _service;
  TransactionManager _txm;

  boolean _active = false;

  /** Maps {@link ServicePort}s to a {@link ChannelBackend} */
  private Map<ServicePort, ChannelBackend> _imports = new HashMap<ServicePort, ChannelBackend>();

  /**
   * Constructor.
   *
   * @param dd service deployment descriptor (pre-deployed)
   * @param system backend object for the system in which we are deploying
   * @param spBackend DOCUMENTME
   *
   * @throws LoadException DOCUMENTME
   */
  ServiceBackend(com.fs.pxe.sfwk.deployment.som.Service dd, SystemBackend system,
                 ServiceProviderBackend spBackend)
          throws LoadException {
    _system = system;
    _serviceProviderBackend = spBackend;
    _txm = system.getDomainNode().getTransactionManager();
    _service = new ServiceContextImpl(dd);

    mapPorts(dd.getExportedPorts(),true);
    mapPorts(dd.getImportedPorts(),false);
  }


  private void mapPorts(com.fs.pxe.sfwk.deployment.som.Port[] ports, boolean export)
                    throws LoadException {

    for (int i = 0; i < ports.length; ++i) {
      ChannelBackend channel = _system.getChannelByName(ports[i].getChannelRef());
      if (channel == null) {
        String msg = __msgs.msgChannelReferenceNotFound(ports[i].getChannelRef(), ports[i].getName());
        __log.error(msg);
        throw new LoadException(msg);
      }
      // Link the channel.
      if (export) {
        ServicePort sport = _service.getExport(ports[i].getName());
        channel.setServer(this, sport);
      } else {
        ServicePort sport = _service.getImport(ports[i].getName());
        channel.setClient(this, sport);
        _imports.put(sport, channel);
      }
    }
  }

  String getServiceName() {
    return _service.getServiceName();
  }

  ServiceProviderBackend getServiceProviderBackend() {
    return _serviceProviderBackend;
  }

  boolean isActive() {
    return _active;
  }

  String getDomainUUID() {
    return _system.getDomainNode()
                  .getDomainId();
  }


  String getServiceProviderId() {
    return _serviceProviderBackend.getSpURI();
  }

  SystemUUID getSystemUUID() {
    return _system.getSystemUUID();
  }

  /**
   * Handle an incoming message exchange event.
   * @param serviceEvent incoming message exchange event
   *
   * @throws DomainTaskProcessingException error type
   */
  void onServiceEvent(ServiceEventImpl serviceEvent)
                              throws DomainTaskProcessingException {

    serviceEvent._targetService = this._service;

    try {
      if (__log.isDebugEnabled())
        __log.debug(ObjectPrinter.stringifyMethodEnter("onServiceEvent", new Object[] {"serviceEvent", serviceEvent}));

      _serviceProviderBackend.onServiceEvent(serviceEvent);
    } catch (ServiceProviderException spe) {
      // TODO: This should cause the system to be de-activated.
      String errmsg = "Service Provider Error";
      __log.error(errmsg, spe);
      throw new DomainTaskProcessingException(DomainTaskProcessingException.ACTION_ROLLBACK_AND_RETRY_LATER,
              errmsg, spe);
    } catch (MessageExchangeException e) {
      String errmsg = "Message-Exchange Error";
      __log.error(errmsg, e);
      throw new DomainTaskProcessingException(DomainTaskProcessingException.ACTION_ROLLBACK_AND_RETRY_LATER,
              errmsg, e);
    } catch (Throwable t) {
      String errmsg = "Unexpected Error";
      __log.fatal(errmsg, t);
      // TODO: Add an action that forces the sytem to be deactivated. 
      throw new DomainTaskProcessingException(DomainTaskProcessingException.ACTION_ROLLBACK_AND_RETRY_LATER,
              errmsg, t);

    }
  }


  boolean start() {
    if (_serviceProviderBackend.start(this)) {
      _active = true;
    }
    return _active;
  }

  void stop() {
    _serviceProviderBackend.stop(this);
    _active = false;
  }



  ServiceContext getService() {
    return _service;
  }


  public DomainNodeImpl getDomainNode() {
    return _system.getDomainNode();
  }


  private class ServiceContextImpl extends ServiceConfigImpl implements ServiceContext {
    private String[] _namePrefix;

    ServiceContextImpl(Service service) throws LoadException {
      super(service, _system.getSystemUUID(), _system.getSAR());
      _namePrefix  = new String[] {
        "domain", getDomainNode().getDomainId(),
        "node", getDomainNode().getNodeId(),
        "system", getSystemName(),
        "service", getServiceName()
      };
    }


    void destroy() {
      _valid = false;
    }

   
    public ObjectName createLocalObjectName(String name[]) {
      if (name.length % 2 != 0) {
        throw new IllegalArgumentException("Invalid number of name-value entries.");
      }

      String[] fnames = new String[_namePrefix.length + name.length];
      System.arraycopy(_namePrefix,0, fnames,0,_namePrefix.length);
      System.arraycopy(name, 0, fnames, _namePrefix.length, name.length);
      return SimpleMBean.createObjectName(JMXConstants.JMX_DOMAIN, fnames);
    }


    public MessageExchange createMessageExchange(ServicePort servicePort, ServiceEndpoint sourceEndpoint,
                                                 ServiceEndpoint destinationEndpoint, String operationName)
            throws MessageExchangeException {
      return createMessageExchange(servicePort, sourceEndpoint, destinationEndpoint, operationName, (byte[]) null);
    }

    public MessageExchange createMessageExchange(ServicePort servicePort, ServiceEndpoint sourceEndpoint,
                                                 ServiceEndpoint destinationEndpoint, String operationName, String correlationId)
            throws MessageExchangeException {
      try {
        return createMessageExchange(servicePort, sourceEndpoint, destinationEndpoint, operationName, correlationId.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw new AssertionError("UTF-8 not supported! Contact Technical Support?");
      }
    }

    public MessageExchange createMessageExchange(ServicePort servicePort, ServiceEndpoint sourceEpr,
                                                 ServiceEndpoint destEpr, String operationName, byte[] correlationIdBytes)
            throws NoSuchOperationException, MessageExchangeException {
      ChannelBackend channel = _imports.get(servicePort);
      if (channel == null) {
        throw new NoSuchPortException(servicePort.getPortName());
      }
      return channel.createMessageExchange(sourceEpr, destEpr, operationName, correlationIdBytes);
    }

    public ServiceEndpoint checkMyEndpoint(Node epr) {
      ServiceEndpoint se = createServiceEndpoint(epr);
      if (!(se instanceof StatefulServiceEndpoint) || ((StatefulServiceEndpoint)se).getSessionId() == null) {
        // My endpoint comes with a session id
        GUID guid = new GUID();
        return new WSAServiceEndpoint(se.getUrl(), guid.toString());
      } else return null;
    }

    public ServiceEndpoint checkPartnerEndpoint(Node partnerEpr, Node originalEpr) {
      ServiceEndpoint partnerSe = createServiceEndpoint(partnerEpr);
      ServiceEndpoint originalSe = createServiceEndpoint(partnerEpr);
      // EPR given by partner has no session
      if (!(partnerSe instanceof StatefulServiceEndpoint)) return originalSe;

      // EPR given by partner has a session but might not have an url (only session)
      StatefulServiceEndpoint partnerSession = (StatefulServiceEndpoint) partnerSe;
      if (partnerSession.getUrl() == null) partnerSession.setUrl(originalSe.getUrl());
      return partnerSession;
    }

    public ServiceEndpoint createServiceEndpoint(Node node) {
      if (node == null) return null;
      else return EndpointFactory.createEndpoint(node);
    }

    public ServiceEndpoint convert(Node sourceEndpoint, QName targetElmtType) {
      if (sourceEndpoint == null) return null;
      else return EndpointFactory.convert(sourceEndpoint, targetElmtType);
    }

  }

}
