package com.fs.pxe.sfwk.impl.endpoint;

import com.fs.pxe.sfwk.core.ServiceEndpoint;
import com.fs.utils.Namespaces;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Factory for {@link ServiceEndpoint} implementations.
 */
public class EndpointFactory {

  private static QName WSDL20_ELMT_QNAME = new QName(Namespaces.WSDL_20, "service");
  private static QName WSDL11_ELMT_QNAME = new QName(Namespaces.WSDL_11, "service");
  private static QName WSA_ELMT_QNAME = new QName(Namespaces.WS_ADDRESSING_NS, "EndpointReference");
  private static QName SOAP_ADDR_ELMT_QNAME = new QName(Namespaces.SOAP_NS, "address");

  private static ServiceEndpoint[] ENDPOINTS = new ServiceEndpoint[] {
          new URLServiceEndpoint(), new WSAServiceEndpoint(), new WSDL11ServiceEndpoint(),
          new WSDL20ServiceEndpoint() };

  /**
   * Creates a ServiceEndpoint using the provided Node. The actual endpoint
   * type is detected using the endpoint node (text or element qname).
   * @param endpoint
   * @return the new ServiceEndpoint
   */
  public static ServiceEndpoint createEndpoint(Node endpoint) {
    for (ServiceEndpoint serviceEndpoint : ENDPOINTS) {
      if (serviceEndpoint.accept(endpoint)) {
        ServiceEndpoint se;
        try {
          se = serviceEndpoint.getClass().newInstance();
        } catch (InstantiationException e) {
          throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
        se.set(endpoint);
        return se;
      }
    }
    return null;
  }

  /**
   * Convert an EPR element into another EPR using the provided target type. The target
   * type is actually the qualified name of the root element for the target EPR (i.e
   * wsa:EndpointReference, wsdl:service) or null to convert to a simple URL.
   * @param sourceEndpoint
   * @param targetElmtType QName to convert to
   * @return the converted ServiceEndpoint
   */
  public static ServiceEndpoint convert(Node sourceEndpoint, QName targetElmtType) {
    ServiceEndpoint targetEpr;
    ServiceEndpoint sourceEpr = createEndpoint(sourceEndpoint);
    Map transfoMap = ((MapReducibleEndpoint)sourceEpr).toMap();
    if (targetElmtType == null) {
      targetEpr = new URLServiceEndpoint();
    } else if (targetElmtType.equals(WSDL20_ELMT_QNAME)) {
      targetEpr = new WSDL20ServiceEndpoint();
    } else if (targetElmtType.equals(WSDL11_ELMT_QNAME)) {
      targetEpr = new WSDL11ServiceEndpoint();
    } else if (targetElmtType.equals(WSA_ELMT_QNAME)) {
      targetEpr = new WSAServiceEndpoint();
    } else if (targetElmtType.equals(SOAP_ADDR_ELMT_QNAME)) {
      targetEpr = new URLServiceEndpoint();
    } else {
      // When everything fails, shooting for the most simple EPR format
      targetEpr = new URLServiceEndpoint();
    }

    ((MapReducibleEndpoint)targetEpr).fromMap(transfoMap);
    return targetEpr;
  }
}
