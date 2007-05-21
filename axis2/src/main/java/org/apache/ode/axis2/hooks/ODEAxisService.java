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

package org.apache.ode.axis2.hooks;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.OdeFault;
import org.apache.ode.axis2.util.Axis2UriResolver;
import org.apache.ode.axis2.util.Axis2WSDLLocator;
import org.apache.ode.bpel.iapi.ProcessConf;

/**
 * Implementation of Axis Service used by ODE iapi to enlist itself its service. Allows us to build the service using a
 * WSDL definition using our own receiver.
 */
public class ODEAxisService extends AxisService {

    private static final Log LOG = LogFactory.getLog(ODEAxisService.class);

    public static AxisService createService(AxisConfiguration axisConfig, ProcessConf pconf, QName wsdlServiceName,
            String portName) throws AxisFault {
        Definition wsdlDefinition = pconf.getDefinitionForService(wsdlServiceName);
    String serviceName = extractServiceName(wsdlDefinition, wsdlServiceName, portName);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Create AxisService:"
                      +" service="+wsdlServiceName
                      +" port="+portName
                      +" WSDL="+wsdlDefinition.getDocumentBaseURI()
                      +" BPEL="+pconf.getBpelDocument());
        }
        
        try {
            URI baseUri = pconf.getBaseURL().toURI().resolve(wsdlDefinition.getDocumentBaseURI());
            InputStream is = baseUri.toURL().openStream();
            WSDL11ToAxisServiceBuilder serviceBuilder = new WSDL11ToAxisServiceBuilder(is, wsdlServiceName, portName);
            serviceBuilder.setBaseUri(baseUri.toString());
            serviceBuilder.setCustomResolver(new Axis2UriResolver());
            serviceBuilder.setCustomWSLD4JResolver(new Axis2WSDLLocator(baseUri));
    serviceBuilder.setServerSide(true);

    AxisService axisService = serviceBuilder.populateService();
    axisService.setName(serviceName);
    axisService.setWsdlFound(true);
    axisService.setClassLoader(axisConfig.getServiceClassLoader());

    // In doc/lit we need to declare a mapping between operations and message element names
    // to be able to route properly.
    declarePartsElements(wsdlDefinition, wsdlServiceName, serviceName, portName);

    Iterator operations = axisService.getOperations();
    ODEMessageReceiver msgReceiver = new ODEMessageReceiver();
    while (operations.hasNext()) {
        AxisOperation operation = (AxisOperation) operations.next();
        if (operation.getMessageReceiver() == null) {
            operation.setMessageReceiver(msgReceiver);
        }
    }
    return axisService;
        } catch (Exception e) {
            throw new AxisFault(e);
        }
  }

  public static AxisService createService(AxisConfiguration axisConfig, QName serviceQName, String port,
                             String axisName, Definition wsdlDef, MessageReceiver receiver) throws AxisFault {
        WSDL11ToAxisServiceBuilder serviceBuilder = new WSDL11ToAxisServiceBuilder(wsdlDef, serviceQName, port);
    AxisService axisService = serviceBuilder.populateService();
    axisService.setName(axisName);
    axisService.setWsdlFound(true);
    axisService.setClassLoader(axisConfig.getServiceClassLoader());
    Iterator operations = axisService.getOperations();
    while (operations.hasNext()) {
      AxisOperation operation = (AxisOperation) operations.next();
      if (operation.getMessageReceiver() == null) {
        operation.setMessageReceiver(receiver);
      }
    }
    return axisService;
  }

    private static String extractServiceName(Definition wsdlDefinition, QName wsdlServiceName, String portName)
            throws AxisFault {
    String url = null;
    Service service = wsdlDefinition.getService(wsdlServiceName);
    if (service == null) {
            throw new OdeFault("Unable to find service " + wsdlServiceName + " from service WSDL definition "
                    + wsdlDefinition.getDocumentBaseURI());
    }
    Port port = service.getPort(portName);
    for (Object oext : port.getExtensibilityElements()) {
      if (oext instanceof SOAPAddress)
                url = ((SOAPAddress) oext).getLocationURI();
    }
    if (url == null) {
            throw new OdeFault("Could not extract any soap:address from service WSDL definition " + wsdlServiceName
                    + " (necessary to establish the process target address)!");
    }
    String serviceName = parseURLForService(url);
    if (serviceName == null) {
            throw new OdeFault("The soap:address used for service WSDL definition " + wsdlServiceName + " and port "
                    + portName + " should be of the form http://hostname:port/ode/processes/myProcessEndpointName");
    }
    return serviceName;
  }

  /**
     * Obtain the service name from the request URL. The request URL is expected to use the path "/processes/" under
     * which all processes and their services are listed. Returns null if the path does not contain this part.
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

    private static void declarePartsElements(Definition wsdlDefinition, QName wsdlServiceName, String axisServiceName,
            String portName) {
        List wsldOps = wsdlDefinition.getService(wsdlServiceName).getPort(portName).getBinding().getPortType()
                .getOperations();
    for (Object wsldOp : wsldOps) {
      Operation wsdlOp = (Operation) wsldOp;
      Collection parts = wsdlOp.getInput().getMessage().getParts().values();
      // More than one part, it's rpc/enc, no mapping needs to be declared
      if (parts.size() == 1) {
        Part part = (Part) parts.iterator().next();
        // Parts are types, it's rpc/enc, no mapping needs to be declared
        if (part.getElementName() != null)
                    ODEAxisDispatcher.addElmtToOpMapping(axisServiceName, wsdlOp.getName(), part.getElementName()
                            .getLocalPart());
      }
    }
  }

    
}
