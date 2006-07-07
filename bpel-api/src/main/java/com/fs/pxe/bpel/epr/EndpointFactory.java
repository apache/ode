package com.fs.pxe.bpel.epr;

import com.fs.utils.Namespaces;
import com.fs.utils.DOMUtils;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Factory for {@link com.fs.pxe.bpel.iapi.EndpointReference} implementations.
 */
public class EndpointFactory {

  private static final Log __log = LogFactory.getLog(EndpointFactory.class);

  private static QName WSDL20_ELMT_QNAME = new QName(Namespaces.WSDL_20, "service");
  private static QName WSDL11_ELMT_QNAME = new QName(Namespaces.WSDL_11, "service");
  private static QName WSA_ELMT_QNAME = new QName(Namespaces.WS_ADDRESSING_NS, "EndpointReference");
  private static QName SOAP_ADDR_ELMT_QNAME = new QName(Namespaces.SOAP_NS, "address");

  private static MutableEndpoint[] ENDPOINTS = new MutableEndpoint[] {
          new URLEndpoint(), new WSAEndpoint(), new WSDL11Endpoint(), new WSDL20Endpoint() };

  /**
   * Creates a ServiceEndpoint using the provided Node. The actual endpoint
   * type is detected using the endpoint node (text or element qname).
   * @param endpointElmt
   * @return the new ServiceEndpoint
   */
  public static MutableEndpoint createEndpoint(Element endpointElmt) {
    for (MutableEndpoint endpoint : EndpointFactory.ENDPOINTS) {
      // Eliminating the service-ref element for accept
      if (endpoint.accept(endpointElmt)) {
        MutableEndpoint se;
        try {
          se = endpoint.getClass().newInstance();
        } catch (InstantiationException e) {
          throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
        se.set(endpointElmt);
        return se;
      }
    }
    __log.warn("Couldnt create any endpoint for element " + DOMUtils.domToString(endpointElmt));
    return null;
  }

  /**
   * Convert an EPR element into another EPR using the provided target type. The target
   * type is actually the qualified name of the root element for the target EPR (i.e
   * wsa:MutableEndpoint, wsdl:service) or null to convert to a simple URL.
   * @param targetElmtType QName to convert to
   * @param sourceEndpoint
   * @return the converted MutableEndpoint
   */
  public static MutableEndpoint convert(QName targetElmtType, Element sourceEndpoint) {
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
    if (__log.isDebugEnabled()) {
      __log.debug("Converted endpoint to type " + targetElmtType);
      __log.debug("Source endpoint " + DOMUtils.domToString(sourceEndpoint));
      __log.debug("Destination endpoint " + DOMUtils.domToString(targetEpr.toXML()));
    }
    return targetEpr;
  }
}
