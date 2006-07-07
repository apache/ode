package com.fs.pxe.axis;

import com.fs.utils.stl.CollectionsX;

import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPBinding;
import java.util.Collection;

import org.w3c.dom.Element;

/**
 * Utility class to handle SOAP messages wrapping/unwrapping depending
 * on binding style Document vs. RPC.
 */
public class SOAPUtils {

//  public void wrap(Element msg, Port port) {
//
//  }
//
//  private boolean isRPC(Port wsdlPort) {
//    if (wsdlPort == null)
//      throw new NullPointerException("null wsdlPort");
//
//    Binding binding = wsdlPort.getBinding();
//
//    if (binding == null)
//      throw new SoapBindingException(wsdlPort + " is missing <wsdl:binding>",
//              "port", wsdlPort.getName(),
//              __msgs.msgNoBindingForPort());
//
//    Collection soapBindings = CollectionsX.filter(binding.getExtensibilityElements(), SOAPBinding.class);
//    if (soapBindings.isEmpty()) {
//      throw new SoapBindingException(wsdlPort + " is missing <soapbind:binding>",
//              "port", wsdlPort.getName(),
//              __msgs.msgNoSoapBindingForPort());
//    }
//
//    else if (soapBindings.size() > 1) {
//      throw new SoapBindingException(wsdlPort + " has multiple <soapbind:binding> elements!",
//              "port", wsdlPort.getName(),
//              __msgs.msgMultipleSoapBindingsForPort());
//    }
//
//    SOAPBinding soapBinding = (SOAPBinding) soapBindings.iterator().next();
//    String style  = soapBinding.getStyle();
//    return style != null && style.equals("rpc");
//  }
}
