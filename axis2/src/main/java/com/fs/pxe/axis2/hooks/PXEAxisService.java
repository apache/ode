package com.fs.pxe.axis2.hooks;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2AxisServiceBuilder;
import org.apache.axis2.engine.AxisConfiguration;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Implementation of Axis Service used by PXE iapi to enlist itself
 * its service. Allows us to build the service using a WSDL definition
 * using our own receiver.
 */
public class PXEAxisService extends AxisService {

  public static AxisService createService(AxisConfiguration axisConfig, Definition wsdlDefinition,
                                          QName wsdlServiceName, String portName) throws AxisFault {
    String serviceName = extractServiceName(wsdlDefinition, wsdlServiceName, portName);

    WSDL2AxisServiceBuilder serviceBuilder =
            new WSDL2AxisServiceBuilder(wsdlDefinition, wsdlServiceName, portName);
    serviceBuilder.setServerSide(true);
    AxisService axisService = serviceBuilder.populateService();
    axisService.setName(serviceName);
    axisService.setWsdlfound(true);
    axisService.setClassLoader(axisConfig.getServiceClassLoader());

    Iterator operations = axisService.getOperations();
    PXEMessageReceiver msgReceiver = new PXEMessageReceiver();
    while (operations.hasNext()) {
        AxisOperation operation = (AxisOperation) operations.next();
        if (operation.getMessageReceiver() == null) {
            operation.setMessageReceiver(msgReceiver);
        }
    }
    return axisService;
  }

  private static String extractServiceName(Definition wsdlDefinition, QName wsdlServiceName, String portName) throws AxisFault {
    String url = null;
    Port port = wsdlDefinition.getService(wsdlServiceName).getPort(portName);
    for (Object oext : port.getExtensibilityElements()) {
      if (oext instanceof SOAPAddress)
        url = ((SOAPAddress)oext).getLocationURI();
    }
    if (url == null) {
      throw new AxisFault("Could not extract any soap:address from service WSDL definition " + wsdlServiceName +
              " (necessary to establish the process target address)!");
    }
    String serviceName = parseURLForService(url);
    if (serviceName == null) {
      throw new AxisFault("The soap:address used for service WSDL definition " + wsdlServiceName +
              " and port " + portName + " should be of the form http://hostname:port/pxe/processes/myProcessEndpointName");
    }
    return serviceName;
  }

  /**
   * Obtain the service name from the request URL. The request URL is
   * expected to use the path "/processes/" under which all processes
   * and their services are listed. Returns null if the path does not
   * contain this part.
   */
  protected static String parseURLForService(String path) {
      int index = path.indexOf("/processes/");
      if (-1 != index) {
          String service;

          int serviceStart = index + "/processes/".length();
          if (path.length() > serviceStart + 1) {
              service = path.substring(serviceStart);
              // Path may contain query string, not interesting for us.
              int queryIndex = service.indexOf('?');
              if (queryIndex > 0) {
                  service = service.substring(0, queryIndex);
              }
              return service;
          }
      }
      return null;
  }

}
