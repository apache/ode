package com.fs.pxe.bpel.provider;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.utils.DOMUtils;

/**
 * Very simple URL-based endpoint reference implementation.
 */
public class URLEndpointReferenceImpl implements EndpointReference {
  private static final QName SOAP_ADDR_EL = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address");
  private URL _url;


  public URLEndpointReferenceImpl(Element epr) {
    Element el = DOMUtils.findChildByName(epr,SOAP_ADDR_EL);
    if (el == null)
      throw new IllegalArgumentException("Malformed EPR: missing <soap:address> element");
    String loc = el.getAttribute("location");
    if (loc == null)
      throw new IllegalArgumentException("Malformed EPR: missing location attribute");
    try {
      _url = new URL(loc);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Malformed EPR: invalid URL.");
    }
  }

  public URLEndpointReferenceImpl(URL url) {
    _url = url;
  }
  
  
  public Document toXML() {
    Document doc = DOMUtils.newDocument();
    Element serviceRef =
            doc.createElementNS(SERVICE_REF_QNAME.getNamespaceURI(), SERVICE_REF_QNAME.getLocalPart());
    doc.appendChild(serviceRef);
    Element soapAddress = doc.createElementNS(SOAP_ADDR_EL.getNamespaceURI(), SOAP_ADDR_EL.getLocalPart());
    soapAddress.setAttribute("location", _url.toExternalForm());
    doc.appendChild(serviceRef);
    serviceRef.appendChild(soapAddress);
    return doc;
  }

}
