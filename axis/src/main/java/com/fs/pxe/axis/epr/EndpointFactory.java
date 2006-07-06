package com.fs.pxe.axis.epr;

import com.fs.utils.Namespaces;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Factory for {@link com.fs.pxe.sfwk.core.ServiceEndpoint} implementations.
 */
public class EndpointFactory {

  private static QName WSDL20_ELMT_QNAME = new QName(Namespaces.WSDL_20, "service");
  private static QName WSDL11_ELMT_QNAME = new QName(Namespaces.WSDL_11, "service");
  private static QName WSA_ELMT_QNAME = new QName(Namespaces.WS_ADDRESSING_NS, "EndpointReference");
  private static QName SOAP_ADDR_ELMT_QNAME = new QName(Namespaces.SOAP_NS, "address");

  private static MutableEndpoint[] ENDPOINTS = new MutableEndpoint[] {
          new URLEndpoint(), new WSAEndpoint(), new WSDL11Endpoint(), new WSDL20Endpoint() };

  /**
   * Creates a ServiceEndpoint using the provided Node. The actual endpoint
   * type is detected using the endpoint node (text or element qname).
   * @param endpointNode
   * @return the new ServiceEndpoint
   */
  public static MutableEndpoint createEndpoint(Node endpointNode) {
    for (MutableEndpoint endpoint : EndpointFactory.ENDPOINTS) {
      if (endpoint.accept(endpointNode)) {
        MutableEndpoint se;
        try {
          se = endpoint.getClass().newInstance();
        } catch (InstantiationException e) {
          throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
        se.set(endpointNode);
        return se;
      }
    }
    return null;
  }

  /**
   * Convert an EPR element into another EPR using the provided target type. The target
   * type is actually the qualified name of the root element for the target EPR (i.e
   * wsa:MutableEndpoint, wsdl:service) or null to convert to a simple URL.
   * @param sourceEndpoint
   * @param targetElmtType QName to convert to
   * @return the converted MutableEndpoint
   */
  public static MutableEndpoint convert(Node sourceEndpoint, QName targetElmtType) {
    MutableEndpoint targetEpr;
    MutableEndpoint sourceEpr = EndpointFactory.createEndpoint(sourceEndpoint);
    Map transfoMap = sourceEpr.toMap();
    if (targetElmtType == null) {
      targetEpr = new URLEndpoint();
    } else if (targetElmtType.equals(EndpointFactory.WSDL20_ELMT_QNAME)) {
      targetEpr = new WSDL20Endpoint();
    } else if (targetElmtType.equals(EndpointFactory.WSDL11_ELMT_QNAME)) {
      targetEpr = new WSDL11Endpoint();
    } else if (targetElmtType.equals(EndpointFactory.WSA_ELMT_QNAME)) {
      targetEpr = new WSAEndpoint();
    } else if (targetElmtType.equals(EndpointFactory.SOAP_ADDR_ELMT_QNAME)) {
      targetEpr = new URLEndpoint();
    } else {
      // When everything fails, shooting for the most simple EPR format
      targetEpr = new URLEndpoint();
    }

    targetEpr.fromMap(transfoMap);
    return targetEpr;
  }
}
