package com.fs.pxe.axis2.hooks;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2AxisServiceBuilder;
import org.apache.axis2.engine.AxisConfiguration;

import javax.wsdl.Definition;
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
    WSDL2AxisServiceBuilder serviceBuilder =
            new WSDL2AxisServiceBuilder(wsdlDefinition, wsdlServiceName, portName);
    serviceBuilder.setServerSide(true);
    AxisService axisService = serviceBuilder.populateService();
    axisService.setName(wsdlServiceName.getLocalPart());
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
}
