/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.httpsoap;

import com.fs.pxe.sfwk.rr.RepositoryWsdlLocator;
import com.fs.pxe.sfwk.spi.ServiceContext;
import com.fs.pxe.sfwk.spi.ServicePort;
import com.fs.pxe.sfwk.spi.ServiceProviderException;
import com.fs.pxe.soap.mapping.SoapBindingException;
import com.fs.pxe.soap.mapping.SoapBindingModel;
import com.fs.utils.msg.MessageBundle;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SoapServiceInfo {

  /**
   * Location of fully configured wsdl (e.g. definition with service endpoints)
   */
  public static final String CONCRETE_WSDL_URL = "concreteWsdlUrl";

  /** WSDL Namespace of our Service (optional). */
  public static final String SERVICE_NAMESPACE = "serviceWsdlNamespace";

  /** WSDL Name of our Service (optional). */
  public static final String SERVICE_WSDLNAME = "serviceWsdlName";

  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  private static final Log __log = LogFactory.getLog(SoapServiceInfo.class);

  public final QName _serviceQName;
  public final String _concreteWsdlUrl;
  public final Definition _definition;
  private final Map<String, PortInfo> _inputPorts = new HashMap<String, PortInfo>();
  private final Map<String, PortInfo> _outputPorts = new HashMap<String, PortInfo>();
  public final javax.wsdl.Service _serviceDef;
  private ServiceContext _service;

  public SoapServiceInfo(ServiceContext service) throws ServiceProviderException {
    _service = service;
    _concreteWsdlUrl = service.getDeploymentProperty(CONCRETE_WSDL_URL);

    if (_concreteWsdlUrl == null) {
      __log.warn(__msgs.msgSystemDoesNotSpecifyConcreteWsdlUrl(service.getServiceName(),
          CONCRETE_WSDL_URL));
    }

    if (_concreteWsdlUrl != null) {
      try {
        RepositoryWsdlLocator wsdlLocator = new RepositoryWsdlLocator(service
            .getSystemResourceRepository(), new URI(_concreteWsdlUrl));
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        _definition = reader.readWSDL(wsdlLocator);
      }
      catch (WSDLException wsdlEx) {
        String msg = __msgs.msgErrorParsingConcreteWSDL(service.getServiceName());
        __log.error(msg, wsdlEx);
        throw new ServiceProviderException(msg, wsdlEx);
      }
      catch (URISyntaxException e) {
        String msg = __msgs.msgMalformedWsdlURI(_concreteWsdlUrl);
        __log.error(msg, e);
        throw new ServiceProviderException(msg, e);
      }
    }
    else {
      _definition = service.getSystemWSDL();
    }

    String wsdlNamespace = service.getDeploymentProperty(SERVICE_NAMESPACE);
    if (wsdlNamespace == null) {
      wsdlNamespace = _definition.getTargetNamespace();
      __log.warn(__msgs.msgServiceWsdlNamespaceNotSpecified(service.getServiceName(),
          SERVICE_NAMESPACE, wsdlNamespace));
    }

    String serviceWsdlName = service.getDeploymentProperty(SERVICE_WSDLNAME);

    if (serviceWsdlName == null) {
      serviceWsdlName = service.getServiceName();
      __log.warn(__msgs.msgServiceWsdlServiceNameNotSpecified(service.getServiceName(),
          SERVICE_WSDLNAME));
    }

    try {
      _serviceQName = new QName(wsdlNamespace, serviceWsdlName);
    }
    catch (IllegalArgumentException iae) {
      String msg = __msgs.msgInvalidWsdlServiceName(service.getServiceName(), wsdlNamespace,
          serviceWsdlName);
      __log.error(msg, iae);
      throw new ServiceProviderException(msg, iae);
    }

    _serviceDef = _definition.getService(_serviceQName);
    if (_serviceDef == null) {
      String msg = __msgs.msgServiceNotInWSDL(service.getServiceName(), _serviceQName);
      __log.error(msg);
      throw new ServiceProviderException(msg);
    }

    createPortInfos(service, _inputPorts, service.getExports());
    createPortInfos(service, _outputPorts, service.getImports());
  }

  public PortInfo[] getConnectorPorts() {
    PortInfo[] infos = new PortInfo[_inputPorts.size()];
    return _inputPorts.values().toArray(infos);
  }

  public PortInfo[] getAdapterPorts() {
    PortInfo[] infos = new PortInfo[_outputPorts.size()];
    return _outputPorts.values().toArray(infos);
  }

  public PortInfo getConnectorPort(String port) {
    return _inputPorts.get(port);
  }

  public PortInfo getAdapterPort(String port) {
    return _outputPorts.get(port);
  }

  public SoapBindingModel getConnectorMapper(String port) {
    return _inputPorts.get(port)._soapBindingModel;
  }

  public SoapBindingModel getAdapterMapper(String port) {
    return _outputPorts.get(port)._soapBindingModel;
  }

  public ServiceContext getService() {
    return _service;
  }

  private void createPortInfos(ServiceContext service, Map<String, PortInfo> portMap,
      ServicePort[] ports) throws ServiceProviderException {
    for (int i = 0; i < ports.length; ++i) {
      ServicePort port = ports[i];
      portMap.put(port.getPortName(), createPortInfo(service, port));
      if (__log.isDebugEnabled())
        __log.debug("Mapped " + port.getPortName() + " port on " + service.getServiceName());
    }

  }

  protected PortInfo createPortInfo(ServiceContext service, ServicePort port) throws ServiceProviderException {
    return new PortInfo(service, port);
  }

  /** Information about a particular port. */
  public class PortInfo {

    private SoapBindingModel _soapBindingModel;
    private ServicePort _port;
    private Port _wsdlPort;
    private Binding _wsdlBinding;
    private SOAPBinding _wsdlSoapBinding;
    private SOAPAddress _wsdlSoapAddress;
    private URL _wsdlSoapUrl;

    protected PortInfo(ServiceContext service, ServicePort port)
        throws ServiceProviderException {
      _port = port;
      _wsdlPort = _serviceDef.getPort(port.getPortName());
      if (_wsdlPort == null) {
        String msg = __msgs.msgPortNotInWsdl(service.getServiceName(), port.getPortName());
        __log.error(msg);
        throw new ServiceProviderException(msg);
      }
      _wsdlBinding = _wsdlPort.getBinding();
      assert _wsdlBinding != null;
      for (Iterator i = _wsdlBinding.getExtensibilityElements().iterator(); i.hasNext();) {
        ExtensibilityElement extEl = (ExtensibilityElement)i.next();
        if (extEl instanceof SOAPBinding) {
          _wsdlSoapBinding = (SOAPBinding)extEl;
        }
        else {
          // Perhaps we should complain.
          __log.debug("Skipping unknown element (on binding): " + extEl);
        }
      }

      if (_wsdlSoapBinding == null) {
        String msg = __msgs.msgBindingMissingSoapExtensibilityElement(
            service.getServiceName(), port.getPortName());
        __log.error(msg);
        throw new ServiceProviderException(msg);
      }

      for (Iterator i = _wsdlPort.getExtensibilityElements().iterator(); i.hasNext();) {
        ExtensibilityElement extEl = (ExtensibilityElement)i.next();
        if (extEl instanceof SOAPAddress) _wsdlSoapAddress = (SOAPAddress)extEl;
      }

      if (_wsdlSoapAddress == null) {
        String msg = __msgs.msgPortMissingSoapAddressExtensibilityElement(service
            .getServiceName(), port.getPortName());
        __log.error(msg);
        throw new ServiceProviderException(msg);
      }

      String urlStr = _wsdlSoapAddress.getLocationURI();

      if (urlStr == null || urlStr.equals("")) {
        String msg = __msgs.msgInvalidSoapAddressLocationURI(port.getPortName(), urlStr);
        __log.error(msg);
        throw new ServiceProviderException(msg);
      }

      try {
        _wsdlSoapUrl = new URL(urlStr);
      }
      catch (MalformedURLException e) {
        String msg = __msgs.msgInvalidSoapAddressLocationURI(port.getPortName(), urlStr);
        __log.error(msg);
        throw new ServiceProviderException(msg);
      }

      try {
        _soapBindingModel = new SoapBindingModel(_wsdlPort);
      }
      catch (SoapBindingException e) {
        String msg = __msgs.msgSoapBindingError(service.getServiceName(), port.getPortName(),
            e.getLocalizedMessage());
        __log.error(msg, e);
        throw new ServiceProviderException(e);
      }
    }

    public URL getSoapURL() {
      return _wsdlSoapUrl;
    }

    public SoapBindingModel getSoapBindingModel() {
      return _soapBindingModel;
    }

    public SoapServiceInfo getSoapServiceInfo() {
      return SoapServiceInfo.this;
    }

    public ServicePort getPort() {
      return _port;
    }
  }

}
